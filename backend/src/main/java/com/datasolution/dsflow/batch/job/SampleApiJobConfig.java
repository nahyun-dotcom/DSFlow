package com.datasolution.dsflow.batch.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class SampleApiJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    @Bean
    public Job sampleApiJob() {
        return new JobBuilder("SAMPLE_API_JOB", jobRepository)
                .start(calculateBaseDateStep())
                .next(callApiStep())
                .next(saveDataStep())
                .build();
    }

    @Bean
    public Step calculateBaseDateStep() {
        return new StepBuilder("calculateBaseDateStep", jobRepository)
                .tasklet(calculateBaseDateTasklet(), transactionManager)
                .build();
    }

    @Bean
    public Step callApiStep() {
        return new StepBuilder("callApiStep", jobRepository)
                .tasklet(callApiTasklet(), transactionManager)
                .build();
    }

    @Bean
    public Step saveDataStep() {
        return new StepBuilder("saveDataStep", jobRepository)
                .tasklet(saveDataTasklet(), transactionManager)
                .build();
    }

    @Bean
    public Tasklet calculateBaseDateTasklet() {
        return (contribution, chunkContext) -> {
            String baseDateParam = chunkContext.getStepContext()
                    .getJobParameters()
                    .get("baseDate")
                    .toString();
            
            LocalDate baseDate = LocalDate.parse(baseDateParam, DateTimeFormatter.ISO_LOCAL_DATE);
            log.info("기준일 계산 완료: {}", baseDate);
            
            // Job Execution Context에 기준일 저장
            chunkContext.getStepContext()
                    .getStepExecution()
                    .getJobExecution()
                    .getExecutionContext()
                    .put("calculatedBaseDate", baseDate.toString());
            
            return RepeatStatus.FINISHED;
        };
    }

    @Bean
    public Tasklet callApiTasklet() {
        return (contribution, chunkContext) -> {
            String baseDate = chunkContext.getStepContext()
                    .getStepExecution()
                    .getJobExecution()
                    .getExecutionContext()
                    .getString("calculatedBaseDate");
            
            log.info("API 호출 시작 - 기준일: {}", baseDate);
            
            // 예시: 기상청 API 호출 (실제로는 JobDefinition에서 가져온 URL 사용)
            try {
                WebClient webClient = WebClient.builder().build();
                String response = webClient.get()
                        .uri("https://jsonplaceholder.typicode.com/posts/1")
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();
                
                log.info("API 호출 성공: {}", response);
                
                // 결과를 Job Execution Context에 저장
                chunkContext.getStepContext()
                        .getStepExecution()
                        .getJobExecution()
                        .getExecutionContext()
                        .put("apiResponse", response);
                
            } catch (Exception e) {
                log.error("API 호출 실패: {}", e.getMessage());
                throw e;
            }
            
            return RepeatStatus.FINISHED;
        };
    }

    @Bean
    public Tasklet saveDataTasklet() {
        return (contribution, chunkContext) -> {
            String apiResponse = chunkContext.getStepContext()
                    .getStepExecution()
                    .getJobExecution()
                    .getExecutionContext()
                    .getString("apiResponse");
            
            log.info("데이터 저장 시작");
            
            // 실제 데이터 저장 로직 (DB, 파일 등)
            // 여기서는 로그만 출력
            log.info("API 응답 데이터 처리 완료: {}", apiResponse != null ? "성공" : "실패");
            
            return RepeatStatus.FINISHED;
        };
    }
} 