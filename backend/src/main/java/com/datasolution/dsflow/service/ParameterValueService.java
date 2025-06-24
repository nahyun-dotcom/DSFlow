package com.datasolution.dsflow.service;

import com.datasolution.dsflow.entity.JobParameterConfig;
import com.datasolution.dsflow.entity.enums.ValueSourceType;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ParameterValueService {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final WebClient webClient = WebClient.builder().build();

    /**
     * 파라미터 설정에 따라 값 목록을 생성합니다.
     */
    public List<String> generateParameterValues(JobParameterConfig config) {
        try {
            ValueSourceType sourceType = ValueSourceType.valueOf(config.getValueSourceType());
            
            switch (sourceType) {
                case DB_QUERY:
                    return generateFromDbQuery(config.getValueSource());
                    
                case STATIC_LIST:
                    return generateFromStaticList(config.getValueSource());
                    
                case DATE_RANGE:
                    return generateFromDateRange(config.getValueSource());
                    
                case API_CALL:
                    return generateFromApiCall(config.getValueSource());
                    
                case FILE_LIST:
                    return generateFromFileList(config.getValueSource());
                    
                default:
                    log.warn("알 수 없는 값 소스 타입: {}", sourceType);
                    return new ArrayList<>();
            }
        } catch (Exception e) {
            log.error("파라미터 값 생성 실패: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 데이터베이스 쿼리에서 값 목록 생성
     */
    private List<String> generateFromDbQuery(String query) {
        try {
            return jdbcTemplate.queryForList(query, String.class);
        } catch (Exception e) {
            log.error("DB 쿼리 실행 실패: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 정적 목록에서 값 목록 생성
     */
    private List<String> generateFromStaticList(String jsonArray) {
        try {
            return objectMapper.readValue(jsonArray, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            log.error("정적 목록 파싱 실패: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 날짜 범위에서 값 목록 생성
     */
    private List<String> generateFromDateRange(String dateRangeConfig) {
        try {
            JsonNode config = objectMapper.readTree(dateRangeConfig);
            
            String startDate = config.get("startDate").asText();
            String endDate = config.get("endDate").asText();
            String format = config.has("format") ? config.get("format").asText() : "yyyyMM";
            String interval = config.has("interval") ? config.get("interval").asText() : "MONTH";
            
            List<String> dates = new ArrayList<>();
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
            
            LocalDate current = start;
            while (!current.isAfter(end)) {
                dates.add(current.format(formatter));
                
                switch (interval.toUpperCase()) {
                    case "DAY":
                        current = current.plusDays(1);
                        break;
                    case "WEEK":
                        current = current.plusWeeks(1);
                        break;
                    case "MONTH":
                        current = current.plusMonths(1);
                        break;
                    case "YEAR":
                        current = current.plusYears(1);
                        break;
                    default:
                        current = current.plusMonths(1);
                }
            }
            
            return dates;
        } catch (Exception e) {
            log.error("날짜 범위 생성 실패: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * API 호출에서 값 목록 생성
     */
    private List<String> generateFromApiCall(String apiConfig) {
        try {
            JsonNode config = objectMapper.readTree(apiConfig);
            
            String url = config.get("url").asText();
            String method = config.has("method") ? config.get("method").asText() : "GET";
            String jsonPath = config.has("jsonPath") ? config.get("jsonPath").asText() : "";
            
            String response = webClient
                    .method(org.springframework.http.HttpMethod.valueOf(method))
                    .uri(url)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            
            // JSON 응답에서 값 추출 (간단한 구현)
            if (jsonPath.isEmpty()) {
                return objectMapper.readValue(response, new TypeReference<List<String>>() {});
            } else {
                // JSONPath 라이브러리를 사용하여 더 복잡한 경로 처리 가능
                JsonNode responseNode = objectMapper.readTree(response);
                return extractValuesFromJsonPath(responseNode, jsonPath);
            }
        } catch (Exception e) {
            log.error("API 호출 실패: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 파일에서 값 목록 생성
     */
    private List<String> generateFromFileList(String filePath) {
        try {
            // 파일 읽기 구현 (CSV, TXT 등)
            // 실제 구현에서는 파일 경로에서 읽어와야 함
            log.warn("파일 목록 기능은 아직 구현되지 않았습니다: {}", filePath);
            return new ArrayList<>();
        } catch (Exception e) {
            log.error("파일 읽기 실패: {}", e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * JSON 경로에서 값 추출 (간단한 구현)
     */
    private List<String> extractValuesFromJsonPath(JsonNode node, String jsonPath) {
        List<String> values = new ArrayList<>();
        
        // 간단한 JSON 경로 처리 (예: data.items[].code)
        // 실제로는 JSONPath 라이브러리 사용 권장
        String[] pathParts = jsonPath.split("\\.");
        JsonNode current = node;
        
        for (String part : pathParts) {
            if (part.endsWith("[]")) {
                String fieldName = part.substring(0, part.length() - 2);
                current = current.get(fieldName);
                if (current.isArray()) {
                    for (JsonNode item : current) {
                        if (item.isTextual()) {
                            values.add(item.asText());
                        }
                    }
                    return values;
                }
            } else {
                current = current.get(part);
            }
        }
        
        if (current != null && current.isTextual()) {
            values.add(current.asText());
        }
        
        return values;
    }
} 