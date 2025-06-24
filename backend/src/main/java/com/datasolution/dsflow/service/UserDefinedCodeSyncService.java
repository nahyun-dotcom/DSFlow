package com.datasolution.dsflow.service;

import com.datasolution.dsflow.entity.CodeCategory;
import com.datasolution.dsflow.entity.CodeSyncJob;
import com.datasolution.dsflow.entity.CodeValue;
import com.datasolution.dsflow.repository.CodeSyncJobRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
// import com.jayway.jsonpath.JsonPath; // JSONPath 라이브러리 대신 간단한 파싱 사용
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 사용자 정의 코드 동기화 서비스
 * 사용자가 등록한 외부 API를 통해 코드 데이터를 자동 동기화
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserDefinedCodeSyncService {

    private final CodeSyncJobRepository codeSyncJobRepository;
    private final CodeManagementService codeManagementService;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    /**
     * 모든 활성화된 동기화 작업 조회
     */
    public List<CodeSyncJob> getAllActiveSyncJobs() {
        return codeSyncJobRepository.findByIsActiveTrueOrderByCreatedAtDesc();
    }

    /**
     * 동기화 작업 생성
     */
    @Transactional
    public CodeSyncJob createSyncJob(CodeSyncJob syncJob) {
        // 중복 체크
        if (codeSyncJobRepository.existsBySyncJobCode(syncJob.getSyncJobCode())) {
            throw new IllegalArgumentException("이미 존재하는 동기화 작업 코드입니다: " + syncJob.getSyncJobCode());
        }

        // 대상 카테고리 존재 확인
        if (!codeManagementService.categoryExists(syncJob.getTargetCategoryCode())) {
            throw new IllegalArgumentException("존재하지 않는 카테고리입니다: " + syncJob.getTargetCategoryCode());
        }

        CodeSyncJob saved = codeSyncJobRepository.save(syncJob);
        log.info("새로운 코드 동기화 작업 생성: {} -> {}", 
                syncJob.getSyncJobCode(), syncJob.getTargetCategoryCode());
        
        return saved;
    }

    /**
     * 동기화 작업 수정
     */
    @Transactional
    public CodeSyncJob updateSyncJob(String syncJobCode, CodeSyncJob updatedJob) {
        CodeSyncJob existingJob = codeSyncJobRepository.findBySyncJobCodeAndIsActiveTrue(syncJobCode)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 동기화 작업입니다: " + syncJobCode));

        // 필드 업데이트
        existingJob.setSyncJobName(updatedJob.getSyncJobName());
        existingJob.setApiUrl(updatedJob.getApiUrl());
        existingJob.setHttpMethod(updatedJob.getHttpMethod());
        existingJob.setRequestHeaders(updatedJob.getRequestHeaders());
        existingJob.setRequestParameters(updatedJob.getRequestParameters());
        existingJob.setRequestBody(updatedJob.getRequestBody());
        existingJob.setCodeValueJsonPath(updatedJob.getCodeValueJsonPath());
        existingJob.setCodeNameJsonPath(updatedJob.getCodeNameJsonPath());
        existingJob.setMetadataJsonPath(updatedJob.getMetadataJsonPath());
        existingJob.setParentCodeJsonPath(updatedJob.getParentCodeJsonPath());
        existingJob.setCronExpression(updatedJob.getCronExpression());
        existingJob.setAutoSync(updatedJob.getAutoSync());
        existingJob.setTimeoutSeconds(updatedJob.getTimeoutSeconds());
        existingJob.setRetryCount(updatedJob.getRetryCount());
        existingJob.setDescription(updatedJob.getDescription());

        CodeSyncJob saved = codeSyncJobRepository.save(existingJob);
        log.info("코드 동기화 작업 수정: {}", syncJobCode);
        
        return saved;
    }

    /**
     * 동기화 작업 삭제 (논리 삭제)
     */
    @Transactional
    public void deleteSyncJob(String syncJobCode) {
        CodeSyncJob syncJob = codeSyncJobRepository.findBySyncJobCodeAndIsActiveTrue(syncJobCode)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 동기화 작업입니다: " + syncJobCode));

        syncJob.setIsActive(false);
        codeSyncJobRepository.save(syncJob);
        
        log.info("코드 동기화 작업 삭제: {}", syncJobCode);
    }

    /**
     * 특정 동기화 작업 실행
     */
    @Async
    @Transactional
    public CompletableFuture<String> executeSyncJob(String syncJobCode) {
        log.info("코드 동기화 작업 실행 시작: {}", syncJobCode);
        
        try {
            CodeSyncJob syncJob = codeSyncJobRepository.findBySyncJobCodeAndIsActiveTrue(syncJobCode)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 동기화 작업입니다: " + syncJobCode));

            String result = performSync(syncJob);
            
            // 동기화 결과 업데이트
            syncJob.setLastSyncTime(LocalDateTime.now());
            syncJob.setLastSyncResult(result);
            codeSyncJobRepository.save(syncJob);
            
            log.info("코드 동기화 작업 완료: {} - {}", syncJobCode, result);
            return CompletableFuture.completedFuture(result);
            
        } catch (Exception e) {
            log.error("코드 동기화 작업 실패: {}", syncJobCode, e);
            return CompletableFuture.completedFuture("FAILED: " + e.getMessage());
        }
    }

    /**
     * 실제 동기화 수행
     */
    private String performSync(CodeSyncJob syncJob) throws Exception {
        // 1. API 호출
        String response = callExternalApi(syncJob);
        
        // 2. 응답 데이터 파싱
        List<Map<String, Object>> codeDataList = parseApiResponse(response, syncJob);
        
        // 3. 코드 데이터 업데이트
        int syncCount = 0;
        for (Map<String, Object> codeData : codeDataList) {
            if (updateCodeValue(syncJob, codeData)) {
                syncCount++;
            }
        }
        
        // 동기화된 코드 수 업데이트
        syncJob.setLastSyncCount(syncCount);
        
        return String.format("SUCCESS: %d개 코드 동기화 완료", syncCount);
    }

    /**
     * 외부 API 호출
     */
    private String callExternalApi(CodeSyncJob syncJob) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        
        // 사용자 정의 헤더 추가
        if (syncJob.getRequestHeaders() != null && !syncJob.getRequestHeaders().trim().isEmpty()) {
            Map<String, String> customHeaders = objectMapper.readValue(
                    syncJob.getRequestHeaders(), new TypeReference<Map<String, String>>() {});
            customHeaders.forEach(headers::add);
        }

        HttpEntity<String> entity;
        String url = syncJob.getApiUrl();
        
        if ("POST".equalsIgnoreCase(syncJob.getHttpMethod())) {
            entity = new HttpEntity<>(syncJob.getRequestBody(), headers);
        } else {
            entity = new HttpEntity<>(headers);
            
            // GET 요청인 경우 파라미터를 URL에 추가
            if (syncJob.getRequestParameters() != null && !syncJob.getRequestParameters().trim().isEmpty()) {
                Map<String, String> params = objectMapper.readValue(
                        syncJob.getRequestParameters(), new TypeReference<Map<String, String>>() {});
                
                StringBuilder urlBuilder = new StringBuilder(url);
                if (!url.contains("?")) {
                    urlBuilder.append("?");
                } else {
                    urlBuilder.append("&");
                }
                
                params.forEach((key, value) -> 
                    urlBuilder.append(key).append("=").append(value).append("&"));
                
                url = urlBuilder.toString().replaceAll("&$", "");
            }
        }

        HttpMethod method = HttpMethod.valueOf(syncJob.getHttpMethod().toUpperCase());
        ResponseEntity<String> response = restTemplate.exchange(url, method, entity, String.class);
        
        return response.getBody();
    }

    /**
     * API 응답 파싱 (간단한 JSON 파싱)
     */
    private List<Map<String, Object>> parseApiResponse(String response, CodeSyncJob syncJob) throws Exception {
        JsonNode jsonNode = objectMapper.readTree(response);
        
        // 간단한 JSON 배열 추출 (실제로는 더 복잡한 JSONPath 구현 필요)
        if (jsonNode.isArray()) {
            return objectMapper.readValue(response, new TypeReference<List<Map<String, Object>>>() {});
        } else if (jsonNode.has("data") && jsonNode.get("data").isArray()) {
            return objectMapper.readValue(jsonNode.get("data").toString(), new TypeReference<List<Map<String, Object>>>() {});
        } else if (jsonNode.has("result") && jsonNode.get("result").isArray()) {
            return objectMapper.readValue(jsonNode.get("result").toString(), new TypeReference<List<Map<String, Object>>>() {});
        } else {
            // 단일 객체인 경우 리스트로 감싸서 반환
            Map<String, Object> singleItem = objectMapper.readValue(response, new TypeReference<Map<String, Object>>() {});
            return List.of(singleItem);
        }
    }

    /**
     * 코드 값 업데이트
     */
    private boolean updateCodeValue(CodeSyncJob syncJob, Map<String, Object> apiData) {
        try {
            // JSONPath로 각 필드 추출
            String codeValue = extractValueByJsonPath(apiData, syncJob.getCodeValueJsonPath());
            String codeName = extractValueByJsonPath(apiData, syncJob.getCodeNameJsonPath());
            String metadata = extractValueByJsonPath(apiData, syncJob.getMetadataJsonPath());
            String parentCode = extractValueByJsonPath(apiData, syncJob.getParentCodeJsonPath());

            if (codeValue == null || codeName == null) {
                log.warn("필수 필드 누락 - codeValue: {}, codeName: {}", codeValue, codeName);
                return false;
            }

            // 메타데이터가 없으면 원본 데이터를 JSON으로 저장
            if (metadata == null) {
                metadata = objectMapper.writeValueAsString(apiData);
            }

            // 코드 존재 여부 확인 후 생성/업데이트
            if (!codeManagementService.codeValueExists(syncJob.getTargetCategoryCode(), codeValue)) {
                codeManagementService.createCodeValue(
                        syncJob.getTargetCategoryCode(), codeValue, codeName, parentCode, metadata);
                return true;
            } else {
                // 기존 코드 업데이트 로직 (필요시 구현)
                return false;
            }
            
        } catch (Exception e) {
            log.warn("코드 값 업데이트 실패: {}", e.getMessage());
            return false;
        }
    }

    /**
     * JSONPath로 값 추출
     */
    private String extractValueByJsonPath(Map<String, Object> data, String jsonPath) {
        if (jsonPath == null || jsonPath.trim().isEmpty()) {
            return null;
        }
        
        try {
            // 간단한 JSONPath 처리 (실제로는 더 복잡한 라이브러리 사용 권장)
            String[] pathParts = jsonPath.replace("$.", "").split("\\.");
            Object value = data;
            
            for (String part : pathParts) {
                if (value instanceof Map) {
                    value = ((Map<String, Object>) value).get(part);
                } else {
                    return null;
                }
            }
            
            return value != null ? value.toString() : null;
        } catch (Exception e) {
            log.warn("JSONPath 추출 실패: {} - {}", jsonPath, e.getMessage());
            return null;
        }
    }

    /**
     * 스케줄링된 자동 동기화 (매 시간마다 체크)
     */
    @Scheduled(cron = "0 0 * * * ?")
    @Transactional
    public void scheduledAutoSync() {
        log.info("스케줄링된 자동 동기화 시작");
        
        List<CodeSyncJob> autoSyncJobs = codeSyncJobRepository.findByIsActiveTrueAndAutoSyncTrueOrderByCreatedAtDesc();
        
        for (CodeSyncJob syncJob : autoSyncJobs) {
            try {
                // Cron 표현식 체크 로직 (실제로는 더 정교한 구현 필요)
                if (shouldRunNow(syncJob)) {
                    executeSyncJob(syncJob.getSyncJobCode());
                }
            } catch (Exception e) {
                log.error("자동 동기화 실패: {}", syncJob.getSyncJobCode(), e);
            }
        }
    }

    /**
     * 현재 시점에 실행해야 하는지 확인 (간단한 구현)
     */
    private boolean shouldRunNow(CodeSyncJob syncJob) {
        // 실제로는 Cron 표현식을 정확히 파싱해서 판단해야 함
        // 여기서는 간단히 마지막 동기화 시간을 기준으로 판단
        if (syncJob.getLastSyncTime() == null) {
            return true;
        }
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastSync = syncJob.getLastSyncTime();
        
        // 24시간 이상 지났으면 실행
        return now.isAfter(lastSync.plusHours(24));
    }
} 