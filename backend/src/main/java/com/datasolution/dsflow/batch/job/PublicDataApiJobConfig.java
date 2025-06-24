package com.datasolution.dsflow.batch.job;

import com.datasolution.dsflow.entity.JobDefinition;
import com.datasolution.dsflow.service.JobDefinitionService;
import com.datasolution.dsflow.service.ParameterCombinationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class PublicDataApiJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final JobDefinitionService jobDefinitionService;
    private final ParameterCombinationService parameterCombinationService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Bean
    public Job publicDataApiJob() {
        return new JobBuilder("PUBLIC_DATA_API_JOB", jobRepository)
                .start(loadJobDefinitionStep())
                .next(generateParametersStep())
                .next(executeApiCallsStep())
                .next(finalizeJobStep())
                .build();
    }

    @Bean
    public Step loadJobDefinitionStep() {
        return new StepBuilder("loadJobDefinitionStep", jobRepository)
                .tasklet(loadJobDefinitionTasklet(null), transactionManager)
                .build();
    }

    @Bean
    public Step generateParametersStep() {
        return new StepBuilder("generateParametersStep", jobRepository)
                .tasklet(generateParametersTasklet(), transactionManager)
                .build();
    }

    @Bean
    public Step executeApiCallsStep() {
        return new StepBuilder("executeApiCallsStep", jobRepository)
                .tasklet(executeApiCallsTasklet(), transactionManager)
                .build();
    }

    @Bean
    public Step finalizeJobStep() {
        return new StepBuilder("finalizeJobStep", jobRepository)
                .tasklet(finalizeJobTasklet(), transactionManager)
                .build();
    }

    @Bean
    @StepScope
    public Tasklet loadJobDefinitionTasklet(@Value("#{jobParameters['jobCode']}") String jobCode) {
        return (contribution, chunkContext) -> {
            log.info("Job 정의 로딩 시작: {}", jobCode);
            
            JobDefinition jobDefinition = jobDefinitionService.findByJobCode(jobCode);
            if (jobDefinition == null) {
                throw new IllegalArgumentException("Job 정의를 찾을 수 없습니다: " + jobCode);
            }
            
            // Job Execution Context에 Job 정의 저장
            chunkContext.getStepContext()
                    .getStepExecution()
                    .getJobExecution()
                    .getExecutionContext()
                    .put("jobDefinition", jobDefinition);
            
            log.info("Job 정의 로딩 완료: {} (파라미터 타입: {})", 
                    jobDefinition.getJobName(), jobDefinition.getParameterType());
            
            return RepeatStatus.FINISHED;
        };
    }

    @Bean
    public Tasklet generateParametersTasklet() {
        return (contribution, chunkContext) -> {
            JobDefinition jobDefinition = (JobDefinition) chunkContext.getStepContext()
                    .getStepExecution()
                    .getJobExecution()
                    .getExecutionContext()
                    .get("jobDefinition");
            
            log.info("파라미터 조합 생성 시작");
            
            // 파라미터 조합 생성
            List<Map<String, Object>> combinations = 
                    parameterCombinationService.generateParameterCombinations(jobDefinition);
            
            // 배치 크기에 따라 그룹화
            List<List<Map<String, Object>>> batches = 
                    parameterCombinationService.groupParameterCombinations(
                            combinations, jobDefinition.getBatchSize());
            
            // Job Execution Context에 저장
            chunkContext.getStepContext()
                    .getStepExecution()
                    .getJobExecution()
                    .getExecutionContext()
                    .put("parameterBatches", batches);
            
            log.info("파라미터 조합 생성 완료: 총 {} 개 조합, {} 개 배치", 
                    combinations.size(), batches.size());
            
            return RepeatStatus.FINISHED;
        };
    }

    @Bean
    public Tasklet executeApiCallsTasklet() {
        return (contribution, chunkContext) -> {
            JobDefinition jobDefinition = (JobDefinition) chunkContext.getStepContext()
                    .getStepExecution()
                    .getJobExecution()
                    .getExecutionContext()
                    .get("jobDefinition");
            
            @SuppressWarnings("unchecked")
            List<List<Map<String, Object>>> batches = (List<List<Map<String, Object>>>) 
                    chunkContext.getStepContext()
                    .getStepExecution()
                    .getJobExecution()
                    .getExecutionContext()
                    .get("parameterBatches");
            
            log.info("API 호출 배치 실행 시작: {} 개 배치", batches.size());
            
            WebClient webClient = WebClient.builder().build();
            int successCount = 0;
            int failCount = 0;
            
            for (int batchIndex = 0; batchIndex < batches.size(); batchIndex++) {
                List<Map<String, Object>> batch = batches.get(batchIndex);
                log.info("배치 {}/{} 처리 시작 ({} 개 API 호출)", 
                        batchIndex + 1, batches.size(), batch.size());
                
                for (Map<String, Object> params : batch) {
                    try {
                        // API 호출
                        String response = callPublicDataApi(webClient, jobDefinition, params);
                        
                        // 응답 처리 (실제로는 DB 저장 등의 로직 추가)
                        processApiResponse(response, params);
                        successCount++;
                        
                        // 지연 시간 적용
                        if (jobDefinition.getDelaySeconds() > 0) {
                            TimeUnit.SECONDS.sleep(jobDefinition.getDelaySeconds());
                        }
                        
                    } catch (Exception e) {
                        log.error("API 호출 실패: {}, 파라미터: {}", e.getMessage(), params);
                        failCount++;
                    }
                }
                
                log.info("배치 {}/{} 처리 완료", batchIndex + 1, batches.size());
            }
            
            // 결과 저장
            chunkContext.getStepContext()
                    .getStepExecution()
                    .getJobExecution()
                    .getExecutionContext()
                    .put("successCount", successCount);
            chunkContext.getStepContext()
                    .getStepExecution()
                    .getJobExecution()
                    .getExecutionContext()
                    .put("failCount", failCount);
            
            log.info("API 호출 배치 실행 완료: 성공 {}, 실패 {}", successCount, failCount);
            
            return RepeatStatus.FINISHED;
        };
    }

    @Bean
    public Tasklet finalizeJobTasklet() {
        return (contribution, chunkContext) -> {
            Integer successCount = (Integer) chunkContext.getStepContext()
                    .getStepExecution()
                    .getJobExecution()
                    .getExecutionContext()
                    .get("successCount");
            
            Integer failCount = (Integer) chunkContext.getStepContext()
                    .getStepExecution()
                    .getJobExecution()
                    .getExecutionContext()
                    .get("failCount");
            
            log.info("Job 실행 완료 - 총 성공: {}, 총 실패: {}", successCount, failCount);
            
            // 실행 결과에 따른 후처리 로직 (알림, 리포트 생성 등)
            if (failCount > 0) {
                log.warn("일부 API 호출이 실패했습니다. 실패 건수: {}", failCount);
            }
            
            return RepeatStatus.FINISHED;
        };
    }

    /**
     * 공공데이터 API 호출
     */
    private String callPublicDataApi(WebClient webClient, JobDefinition jobDefinition, 
                                   Map<String, Object> params) throws Exception {
        
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromHttpUrl(jobDefinition.getResourceUrl());
        
        // 파라미터 추가
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            uriBuilder.queryParam(entry.getKey(), entry.getValue());
        }
        
        String uri = uriBuilder.toUriString();
        log.debug("API 호출 URL: {}", uri);
        
        String response = webClient
                .method(HttpMethod.valueOf(jobDefinition.getMethodType().name().substring(4))) // API_GET -> GET
                .uri(uri)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        
        log.debug("API 응답 수신 완료");
        return response;
    }

    /**
     * API 응답 처리
     */
    private void processApiResponse(String response, Map<String, Object> params) {
        // 실제로는 여기서 응답 데이터를 파싱하고 DB에 저장하는 로직을 구현
        log.debug("API 응답 처리 완료: 파라미터 {}", params);
        
        // TODO: 응답 데이터 파싱 및 저장 로직 구현
        // 1. XML/JSON 응답 파싱
        // 2. 데이터 검증
        // 3. DB 저장
        // 4. 오류 처리
    }
} 