package com.datasolution.dsflow.service;

import com.datasolution.dsflow.entity.JobDefinition;
import com.datasolution.dsflow.entity.JobParameterConfig;
import com.datasolution.dsflow.entity.enums.JobParameterType;
import com.datasolution.dsflow.repository.JobParameterConfigRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ParameterCombinationService {

    private final JobParameterConfigRepository parameterConfigRepository;
    private final ParameterValueService parameterValueService;
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

                case MULTI_PARAM:
                    combinations.addAll(generateMultiParameterCombinations(jobDefinition, baseParams));
                    break;

                case MATRIX:
                    combinations.addAll(generateMatrixCombinations(jobDefinition, baseParams));
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
     * 다중 파라미터 조합 생성 (각 파라미터별로 순차 처리)
     */
    private List<Map<String, Object>> generateMultiParameterCombinations(JobDefinition jobDefinition, JsonNode baseParams) {
        List<Map<String, Object>> combinations = new ArrayList<>();
        
        // Job에 설정된 파라미터 설정들 조회
        List<JobParameterConfig> paramConfigs = parameterConfigRepository
                .findByJobDefinitionIdAndIsActiveTrueOrderBySortOrder(jobDefinition.getId());
        
        if (paramConfigs.isEmpty()) {
            log.warn("Job {}에 설정된 파라미터 설정이 없습니다.", jobDefinition.getJobCode());
            return combinations;
        }

        // 첫 번째 파라미터에 대해서만 여러 값 생성 (MULTI_PARAM는 하나의 파라미터만 변경)
        JobParameterConfig firstParam = paramConfigs.get(0);
        List<String> values = parameterValueService.generateParameterValues(firstParam);
        
        for (String value : values) {
            ObjectNode paramNode = baseParams.deepCopy();
            paramNode.put(firstParam.getParameterName(), value);
            combinations.add(objectMapper.convertValue(paramNode, Map.class));
        }

        return combinations;
    }

    /**
     * 매트릭스 파라미터 조합 생성 (모든 파라미터들의 데카르트 곱)
     */
    private List<Map<String, Object>> generateMatrixCombinations(JobDefinition jobDefinition, JsonNode baseParams) {
        List<Map<String, Object>> combinations = new ArrayList<>();
        
        // Job에 설정된 파라미터 설정들 조회
        List<JobParameterConfig> paramConfigs = parameterConfigRepository
                .findByJobDefinitionIdAndIsActiveTrueOrderBySortOrder(jobDefinition.getId());
        
        if (paramConfigs.isEmpty()) {
            log.warn("Job {}에 설정된 파라미터 설정이 없습니다.", jobDefinition.getJobCode());
            return combinations;
        }

        // 각 파라미터별 값 목록 생성
        Map<String, List<String>> parameterValues = new HashMap<>();
        for (JobParameterConfig config : paramConfigs) {
            List<String> values = parameterValueService.generateParameterValues(config);
            parameterValues.put(config.getParameterName(), values);
            log.info("파라미터 {} : {} 개 값 생성", config.getParameterName(), values.size());
        }

        // 데카르트 곱 생성
        combinations = generateCartesianProduct(baseParams, parameterValues, paramConfigs);

        return combinations;
    }

    /**
     * 데카르트 곱을 이용하여 모든 파라미터 조합 생성
     */
    private List<Map<String, Object>> generateCartesianProduct(JsonNode baseParams, 
                                                              Map<String, List<String>> parameterValues,
                                                              List<JobParameterConfig> paramConfigs) {
        List<Map<String, Object>> result = new ArrayList<>();
        
        if (paramConfigs.isEmpty()) {
            result.add(objectMapper.convertValue(baseParams, Map.class));
            return result;
        }

        generateCartesianProductRecursive(baseParams, parameterValues, paramConfigs, 0, 
                                        new HashMap<>(), result);
        
        return result;
    }

    /**
     * 재귀적으로 데카르트 곱 생성
     */
    private void generateCartesianProductRecursive(JsonNode baseParams,
                                                 Map<String, List<String>> parameterValues,
                                                 List<JobParameterConfig> paramConfigs,
                                                 int paramIndex,
                                                 Map<String, String> currentCombination,
                                                 List<Map<String, Object>> result) {
        
        if (paramIndex >= paramConfigs.size()) {
            // 모든 파라미터 조합 완성
            ObjectNode paramNode = baseParams.deepCopy();
            for (Map.Entry<String, String> entry : currentCombination.entrySet()) {
                paramNode.put(entry.getKey(), entry.getValue());
            }
            result.add(objectMapper.convertValue(paramNode, Map.class));
            return;
        }

        JobParameterConfig currentParam = paramConfigs.get(paramIndex);
        List<String> values = parameterValues.get(currentParam.getParameterName());
        
        for (String value : values) {
            currentCombination.put(currentParam.getParameterName(), value);
            generateCartesianProductRecursive(baseParams, parameterValues, paramConfigs, 
                                            paramIndex + 1, currentCombination, result);
            currentCombination.remove(currentParam.getParameterName());
        }
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