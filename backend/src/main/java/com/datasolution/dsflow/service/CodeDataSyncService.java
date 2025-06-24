package com.datasolution.dsflow.service;

import com.datasolution.dsflow.entity.CodeCategory;
import com.datasolution.dsflow.entity.CodeValue;
import com.datasolution.dsflow.entity.JobDefinition;
import com.datasolution.dsflow.entity.enums.JobStatus;
import com.datasolution.dsflow.entity.enums.MethodType;
import com.datasolution.dsflow.repository.JobDefinitionRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 코드 데이터 동기화 및 자동 피딩 서비스
 * 
 * 주요 기능:
 * 1. 공공데이터 API에서 최신 코드 정보 자동 수집
 * 2. 스케줄링 기반 정기 업데이트
 * 3. 데이터 변경 감지 및 알림
 * 4. 코드 데이터 품질 검증
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CodeDataSyncService {

    private final CodeManagementService codeManagementService;
    private final JobDefinitionRepository jobDefinitionRepository;
    private final ObjectMapper objectMapper;
    private final WebClient webClient;

    /**
     * 매일 새벽 2시에 모든 코드 카테고리 동기화
     */
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void scheduledCodeSync() {
        log.info("=== 스케줄링된 코드 데이터 동기화 시작 ===");
        
        try {
            // 1. 지역코드 동기화 (행정표준코드 API)
            syncRegionCodes();
            
            // 2. 업종코드 동기화 (통계청 API)
            syncIndustryCodes();
            
            // 3. 기타 코드 동기화
            syncOtherCodes();
            
            log.info("=== 스케줄링된 코드 데이터 동기화 완료 ===");
        } catch (Exception e) {
            log.error("코드 데이터 동기화 중 오류 발생", e);
        }
    }

    /**
     * 지역코드 동기화 (행정표준코드관리시스템 API)
     */
    @Async
    @Transactional
    public CompletableFuture<String> syncRegionCodes() {
        log.info("지역코드 동기화 시작");
        
        try {
            // 행정표준코드관리시스템 API 호출
            String apiUrl = "https://www.code.go.kr/api/getCodeListAll.do";
            Map<String, Object> params = Map.of(
                "codeId", "법정동코드",
                "format", "json"
            );
            
            String response = callExternalApi(apiUrl, params);
            List<Map<String, Object>> regionData = parseApiResponse(response, "$.data[*]");
            
            CodeCategory regionCategory = getOrCreateCategory("REGION", "지역코드", "법정동코드 및 행정구역 정보");
            
            int syncCount = 0;
            for (Map<String, Object> item : regionData) {
                String codeValue = (String) item.get("codeValue");
                String codeName = (String) item.get("codeName");
                String metadata = createRegionMetadata(item);
                
                if (syncCodeValue(regionCategory, codeValue, codeName, metadata)) {
                    syncCount++;
                }
            }
            
            log.info("지역코드 동기화 완료: {}개 업데이트", syncCount);
            return CompletableFuture.completedFuture("SUCCESS");
            
        } catch (Exception e) {
            log.error("지역코드 동기화 실패", e);
            return CompletableFuture.completedFuture("FAILED: " + e.getMessage());
        }
    }

    /**
     * 업종코드 동기화 (통계청 KOSIS API)
     */
    @Async
    @Transactional
    public CompletableFuture<String> syncIndustryCodes() {
        log.info("업종코드 동기화 시작");
        
        try {
            // 통계청 KOSIS API 호출
            String apiUrl = "https://kosis.kr/openapi/Param/statisticsParameterData.do";
            Map<String, Object> params = Map.of(
                "method", "getList",
                "apiKey", "${kosis.api.key:YOUR_KOSIS_API_KEY}",
                "format", "json",
                "jsonVD", "Y",
                "userStatsId", "업종분류"
            );
            
            String response = callExternalApi(apiUrl, params);
            List<Map<String, Object>> industryData = parseApiResponse(response, "$.result[*]");
            
            CodeCategory industryCategory = getOrCreateCategory("INDUSTRY", "업종코드", "한국표준산업분류 코드");
            
            int syncCount = 0;
            for (Map<String, Object> item : industryData) {
                String codeValue = (String) item.get("C1");
                String codeName = (String) item.get("C1_NM");
                String metadata = createIndustryMetadata(item);
                
                if (syncCodeValue(industryCategory, codeValue, codeName, metadata)) {
                    syncCount++;
                }
            }
            
            log.info("업종코드 동기화 완료: {}개 업데이트", syncCount);
            return CompletableFuture.completedFuture("SUCCESS");
            
        } catch (Exception e) {
            log.error("업종코드 동기화 실패", e);
            return CompletableFuture.completedFuture("FAILED: " + e.getMessage());
        }
    }

    /**
     * 기타 코드 동기화 (사업체유형, 차량유형 등)
     */
    @Transactional
    public void syncOtherCodes() {
        log.info("기타 코드 동기화 시작");
        
        // 사업체유형은 상대적으로 변화가 적으므로 정적 데이터로 관리
        syncStaticBusinessTypeCodes();
        
        // 차량유형은 외부 API나 파일에서 가져올 수 있음
        syncVehicleTypeCodes();
        
        log.info("기타 코드 동기화 완료");
    }

    /**
     * 수동 코드 동기화 (특정 카테고리)
     */
    @Transactional
    public String syncCodesByCategory(String categoryCode) {
        log.info("카테고리별 코드 동기화 시작: {}", categoryCode);
        
        try {
            switch (categoryCode.toUpperCase()) {
                case "REGION":
                    return syncRegionCodes().get();
                case "INDUSTRY":
                    return syncIndustryCodes().get();
                default:
                    return "지원하지 않는 카테고리입니다: " + categoryCode;
            }
        } catch (Exception e) {
            log.error("카테고리별 코드 동기화 실패: {}", categoryCode, e);
            return "FAILED: " + e.getMessage();
        }
    }

    /**
     * 코드 데이터 검증 및 품질 체크
     */
    public Map<String, Object> validateCodeData(String categoryCode) {
        log.info("코드 데이터 검증 시작: {}", categoryCode);
        
        List<CodeValue> codes = codeManagementService.getCodeValueDetailsByCategory(categoryCode);
        
        int totalCount = codes.size();
        int activeCount = (int) codes.stream().filter(CodeValue::getIsActive).count();
        int inactiveCount = totalCount - activeCount;
        
        // 중복 코드 체크
        long duplicateCount = codes.stream()
                .collect(java.util.stream.Collectors.groupingBy(CodeValue::getCodeValue))
                .values().stream()
                .mapToLong(list -> list.size() > 1 ? list.size() - 1 : 0)
                .sum();
        
        // 메타데이터 누락 체크
        long missingMetadataCount = codes.stream()
                .filter(code -> code.getMetadata() == null || code.getMetadata().trim().isEmpty())
                .count();
        
        return Map.of(
                "categoryCode", categoryCode,
                "totalCount", totalCount,
                "activeCount", activeCount,
                "inactiveCount", inactiveCount,
                "duplicateCount", duplicateCount,
                "missingMetadataCount", missingMetadataCount,
                "lastUpdated", codes.stream()
                        .map(CodeValue::getUpdatedAt)
                        .max(LocalDateTime::compareTo)
                        .orElse(null)
        );
    }

    /**
     * 코드 데이터 백업 및 복원
     */
    @Transactional
    public String backupCodeData(String categoryCode) {
        log.info("코드 데이터 백업 시작: {}", categoryCode);
        
        try {
            List<CodeValue> codes = codeManagementService.getCodeValueDetailsByCategory(categoryCode);
            String backupData = objectMapper.writeValueAsString(codes);
            
            // 실제로는 파일 시스템이나 별도 백업 테이블에 저장
            String backupKey = categoryCode + "_" + LocalDateTime.now().toString();
            log.info("코드 데이터 백업 완료: {} ({}개 항목)", backupKey, codes.size());
            
            return backupKey;
        } catch (Exception e) {
            log.error("코드 데이터 백업 실패: {}", categoryCode, e);
            return "FAILED: " + e.getMessage();
        }
    }

    // ================= 내부 헬퍼 메소드들 =================

    private String callExternalApi(String url, Map<String, Object> params) {
        // 실제 외부 API 호출 구현
        // WebClient나 RestTemplate 사용
        return "{}"; // 임시 응답
    }

    private List<Map<String, Object>> parseApiResponse(String response, String jsonPath) {
        // JSON 응답 파싱 및 JsonPath로 데이터 추출
        return List.of(); // 임시 응답
    }

    private CodeCategory getOrCreateCategory(String categoryCode, String categoryName, String description) {
        if (!codeManagementService.categoryExists(categoryCode)) {
            return codeManagementService.createCategory(categoryCode, categoryName, description);
        }
        return codeManagementService.getAllActiveCategories().stream()
                .filter(cat -> cat.getCategoryCode().equals(categoryCode))
                .findFirst()
                .orElseThrow();
    }

    private boolean syncCodeValue(CodeCategory category, String codeValue, String codeName, String metadata) {
        try {
            if (!codeManagementService.codeValueExists(category.getCategoryCode(), codeValue)) {
                codeManagementService.createCodeValue(
                        category.getCategoryCode(), codeValue, codeName, null, metadata);
                return true;
            } else {
                // 기존 코드 업데이트 로직 (변경 감지)
                return updateExistingCodeIfChanged(category.getCategoryCode(), codeValue, codeName, metadata);
            }
        } catch (Exception e) {
            log.warn("코드 동기화 실패: {} - {}", codeValue, e.getMessage());
            return false;
        }
    }

    private boolean updateExistingCodeIfChanged(String categoryCode, String codeValue, String codeName, String metadata) {
        // 기존 코드와 비교하여 변경 사항이 있으면 업데이트
        // 실제 구현에서는 더 세밀한 변경 감지 로직 필요
        return false;
    }

    private String createRegionMetadata(Map<String, Object> regionData) {
        // 지역코드 메타데이터 생성
        return String.format("{\"sido\":\"%s\",\"gugun\":\"%s\",\"level\":\"%s\"}",
                regionData.get("sido"), regionData.get("gugun"), regionData.get("level"));
    }

    private String createIndustryMetadata(Map<String, Object> industryData) {
        // 업종코드 메타데이터 생성
        return String.format("{\"section\":\"%s\",\"level\":\"%s\",\"parent\":\"%s\"}",
                industryData.get("section"), industryData.get("level"), industryData.get("parent"));
    }

    private void syncStaticBusinessTypeCodes() {
        CodeCategory category = getOrCreateCategory("BUSINESS_TYPE", "사업체유형", "기업 규모별 분류 코드");
        
        Map<String, String> businessTypes = Map.of(
                "INDIVIDUAL", "개인사업자",
                "CORPORATION", "법인사업자", 
                "GOVERNMENT", "정부기관",
                "NON_PROFIT", "비영리단체"
        );
        
        businessTypes.forEach((code, name) -> {
            String metadata = String.format("{\"type\":\"%s\",\"tax_type\":\"%s\"}", 
                    code.toLowerCase(), getTaxTypeByBusinessType(code));
            syncCodeValue(category, code, name, metadata);
        });
    }

    private void syncVehicleTypeCodes() {
        // 차량유형 코드 동기화 (교통안전공단 API 등에서 가져올 수 있음)
        CodeCategory category = getOrCreateCategory("VEHICLE_TYPE", "차량유형", "자동차 분류 코드");
        
        // 임시 정적 데이터
        Map<String, String> vehicleTypes = Map.of(
                "PASSENGER", "승용차",
                "COMMERCIAL", "상용차",
                "SEDAN", "승용차-세단",
                "SUV", "승용차-SUV"
        );
        
        vehicleTypes.forEach((code, name) -> {
            String metadata = String.format("{\"category\":\"%s\"}", code.toLowerCase());
            syncCodeValue(category, code, name, metadata);
        });
    }

    private String getTaxTypeByBusinessType(String businessType) {
        return switch (businessType) {
            case "INDIVIDUAL" -> "소득세";
            case "CORPORATION" -> "법인세";
            case "GOVERNMENT", "NON_PROFIT" -> "비과세";
            default -> "기타";
        };
    }
} 