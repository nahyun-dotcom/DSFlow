package com.datasolution.dsflow.service;

import com.datasolution.dsflow.entity.JobDefinition;
import com.datasolution.dsflow.entity.enums.JobParameterType;
import com.datasolution.dsflow.repository.RegionCodeRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ParameterCombinationService {

    private final RegionCodeRepository regionCodeRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Job 정의에 따라 파라미터 조합 목록을 생성합니다.
     */
    public List<Map<String, Object>> generateParameterCombinations(JobDefinition jobDefinition) {
        List<Map<String, Object>> combinations = new ArrayList<>();

        try {
            JsonNode baseParams = objectMapper.readTree(jobDefinition.getParameters());

            switch (jobDefinition.getParameterType()) {
                case SINGLE:
                    combinations.add(objectMapper.convertValue(baseParams, Map.class));
                    break;

                case MULTI_REGION:
                    combinations.addAll(generateRegionCombinations(baseParams));
                    break;

                case MULTI_DATE:
                    combinations.addAll(generateDateCombinations(baseParams, jobDefinition.getDateRangeMonths()));
                    break;

                case MATRIX:
                    combinations.addAll(generateMatrixCombinations(baseParams, jobDefinition.getDateRangeMonths()));
                    break;

                default:
                    log.warn("알 수 없는 파라미터 타입: {}", jobDefinition.getParameterType());
                    combinations.add(objectMapper.convertValue(baseParams, Map.class));
            }

        } catch (JsonProcessingException e) {
            log.error("파라미터 파싱 실패: {}", e.getMessage());
            throw new RuntimeException("파라미터 조합 생성 실패", e);
        }

        log.info("총 {} 개의 파라미터 조합이 생성되었습니다.", combinations.size());
        return combinations;
    }

    /**
     * 지역코드별 파라미터 조합 생성
     */
    private List<Map<String, Object>> generateRegionCombinations(JsonNode baseParams) {
        List<Map<String, Object>> combinations = new ArrayList<>();
        List<String> regionCodes = regionCodeRepository.findActiveLawdCdList();

        for (String lawdCd : regionCodes) {
            ObjectNode paramNode = baseParams.deepCopy();
            paramNode.put("LAWD_CD", lawdCd);
            combinations.add(objectMapper.convertValue(paramNode, Map.class));
        }

        return combinations;
    }

    /**
     * 날짜별 파라미터 조합 생성
     */
    private List<Map<String, Object>> generateDateCombinations(JsonNode baseParams, Integer months) {
        List<Map<String, Object>> combinations = new ArrayList<>();
        LocalDate currentDate = LocalDate.now();

        for (int i = 0; i < months; i++) {
            LocalDate targetDate = currentDate.minusMonths(i);
            String dealYmd = targetDate.format(DateTimeFormatter.ofPattern("yyyyMM"));

            ObjectNode paramNode = baseParams.deepCopy();
            paramNode.put("DEAL_YMD", dealYmd);
            combinations.add(objectMapper.convertValue(paramNode, Map.class));
        }

        return combinations;
    }

    /**
     * 지역코드 x 날짜 매트릭스 파라미터 조합 생성
     */
    private List<Map<String, Object>> generateMatrixCombinations(JsonNode baseParams, Integer months) {
        List<Map<String, Object>> combinations = new ArrayList<>();
        List<String> regionCodes = regionCodeRepository.findActiveLawdCdList();
        LocalDate currentDate = LocalDate.now();

        for (String lawdCd : regionCodes) {
            for (int i = 0; i < months; i++) {
                LocalDate targetDate = currentDate.minusMonths(i);
                String dealYmd = targetDate.format(DateTimeFormatter.ofPattern("yyyyMM"));

                ObjectNode paramNode = baseParams.deepCopy();
                paramNode.put("LAWD_CD", lawdCd);
                paramNode.put("DEAL_YMD", dealYmd);
                combinations.add(objectMapper.convertValue(paramNode, Map.class));
            }
        }

        return combinations;
    }

    /**
     * 파라미터 조합을 배치 크기에 따라 그룹화합니다.
     */
    public List<List<Map<String, Object>>> groupParameterCombinations(
            List<Map<String, Object>> combinations, Integer batchSize) {
        
        List<List<Map<String, Object>>> batches = new ArrayList<>();
        
        for (int i = 0; i < combinations.size(); i += batchSize) {
            int endIndex = Math.min(i + batchSize, combinations.size());
            batches.add(combinations.subList(i, endIndex));
        }
        
        log.info("총 {} 개의 배치로 그룹화되었습니다. (배치 크기: {})", batches.size(), batchSize);
        return batches;
    }
} 