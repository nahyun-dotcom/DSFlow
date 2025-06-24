package com.datasolution.dsflow.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Cron 표현식의 유효성을 검증하는 유틸리티 클래스
 * Quartz Cron 표현식 형식을 지원합니다.
 * 
 * Cron 표현식 형식: {초} {분} {시} {일} {월} {요일} [{년}]
 * 
 * 각 필드의 허용 값:
 * - 초: 0-59
 * - 분: 0-59
 * - 시: 0-23
 * - 일: 1-31
 * - 월: 1-12 또는 JAN-DEC
 * - 요일: 1-7 또는 SUN-SAT (1=일요일)
 * - 년: 1970-2099 (선택사항)
 * 
 * 특수 문자:
 * - * : 모든 값
 * - ? : 일 또는 요일 필드에서 사용 (값 없음)
 * - - : 범위 (예: 1-5)
 * - , : 목록 (예: 1,3,5)
 * - / : 간격 (예: 0/15 = 0부터 15분 간격)
 * - L : 마지막 (일 또는 요일 필드)
 * - W : 평일 (일 필드)
 * - # : n번째 요일 (요일 필드, 예: 2#1 = 첫 번째 월요일)
 */
@Component
@Slf4j
public class CronExpressionValidator {

    // 기본 숫자 패턴
    private static final String SECOND_PATTERN = "([0-5]?[0-9])";
    private static final String MINUTE_PATTERN = "([0-5]?[0-9])";
    private static final String HOUR_PATTERN = "([01]?[0-9]|2[0-3])";
    private static final String DAY_PATTERN = "([1-9]|[12][0-9]|3[01])";
    private static final String MONTH_PATTERN = "([1-9]|1[0-2]|JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC)";
    private static final String DOW_PATTERN = "([1-7]|SUN|MON|TUE|WED|THU|FRI|SAT)";
    private static final String YEAR_PATTERN = "(19[7-9][0-9]|20[0-9][0-9])";

    // 특수 문자가 포함된 패턴
    private static final String SPECIAL_CHARS = "[*?]";
    private static final String RANGE_PATTERN = "([0-9]+)-([0-9]+)";
    private static final String LIST_PATTERN = "([0-9]+,)*[0-9]+";
    private static final String STEP_PATTERN = "([0-9]+|\\*)/([0-9]+)";
    private static final String LAST_DAY_PATTERN = "L";
    private static final String WEEKDAY_PATTERN = "([0-9]+)W";
    private static final String NTH_WEEKDAY_PATTERN = "([1-7]|SUN|MON|TUE|WED|THU|FRI|SAT)#([1-5])";

    /**
     * Cron 표현식의 유효성을 검증합니다.
     * 
     * @param cronExpression 검증할 cron 표현식
     * @return 유효한 경우 true, 그렇지 않으면 false
     */
    public boolean isValidExpression(String cronExpression) {
        if (cronExpression == null || cronExpression.trim().isEmpty()) {
            log.warn("Cron 표현식이 null이거나 비어있습니다.");
            return false;
        }

        try {
            log.info("Cron 표현식 검증 시작: {}", cronExpression);
            String[] fields = cronExpression.trim().split("\\s+");
            
            // 6개 또는 7개 필드 지원 (초 분 시 일 월 요일 [년])
            if (fields.length != 6 && fields.length != 7) {
                log.warn("Cron 표현식의 필드 수가 잘못되었습니다. 예상: 6 또는 7, 실제: {}", fields.length);
                return false;
            }

            log.info("필드 분석: 초={}, 분={}, 시={}, 일={}, 월={}, 요일={}", 
                fields[0], fields[1], fields[2], fields[3], fields[4], fields[5]);

            // 각 필드 검증
            boolean secondValid = validateSecond(fields[0]);
            log.info("초 필드 검증 결과: {}", secondValid);
            
            boolean minuteValid = validateMinute(fields[1]);
            log.info("분 필드 검증 결과: {}", minuteValid);
            
            boolean hourValid = validateHour(fields[2]);
            log.info("시 필드 검증 결과: {}", hourValid);
            
            boolean dayValid = validateDay(fields[3]);
            log.info("일 필드 검증 결과: {}", dayValid);
            
            boolean monthValid = validateMonth(fields[4]);
            log.info("월 필드 검증 결과: {}", monthValid);
            
            boolean dowValid = validateDayOfWeek(fields[5]);
            log.info("요일 필드 검증 결과: {}", dowValid);

            boolean isValid = secondValid && minuteValid && hourValid && dayValid && monthValid && dowValid;

            // 년도 필드가 있는 경우 검증
            if (fields.length == 7) {
                boolean yearValid = validateYear(fields[6]);
                log.info("년 필드 검증 결과: {}", yearValid);
                isValid = isValid && yearValid;
            }

            // 일과 요일 필드의 상호 배타성 검증
            if (isValid) {
                boolean mutualExclusionValid = validateDayAndDayOfWeekMutualExclusion(fields[3], fields[5]);
                log.info("일/요일 상호배타성 검증 결과: {}", mutualExclusionValid);
                isValid = mutualExclusionValid;
            }

            log.info("최종 Cron 표현식 검증 결과: {}", isValid);
            if (!isValid) {
                log.warn("Cron 표현식이 유효하지 않습니다: {}", cronExpression);
            }

            return isValid;

        } catch (Exception e) {
            log.error("Cron 표현식 검증 중 오류 발생: {}", cronExpression, e);
            return false;
        }
    }

    /**
     * 자주 사용되는 Cron 표현식 패턴인지 확인합니다.
     */
    public boolean isCommonPattern(String cronExpression) {
        if (!isValidExpression(cronExpression)) {
            return false;
        }

        // 자주 사용되는 패턴들
        String[] commonPatterns = {
            "0 0 \\* \\* \\* \\?",           // 매시 정각
            "0 \\* \\* \\* \\* \\?",        // 매분
            "0 0 0 \\* \\* \\?",            // 매일 자정
            "0 0 0 \\* \\* MON-FRI",        // 평일 자정
            "0 0 0 1 \\* \\?",              // 매월 1일 자정
            "0 0 0 1 1 \\?",                // 매년 1월 1일 자정
        };

        for (String pattern : commonPatterns) {
            if (cronExpression.matches(pattern)) {
                return true;
            }
        }

        return false;
    }

    private boolean validateSecond(String field) {
        return validateTimeField(field, 0, 59, "초");
    }

    private boolean validateMinute(String field) {
        return validateTimeField(field, 0, 59, "분");
    }

    private boolean validateHour(String field) {
        return validateTimeField(field, 0, 23, "시");
    }

    private boolean validateDay(String field) {
        if ("?".equals(field)) {
            return true;
        }
        if (field.matches(LAST_DAY_PATTERN)) {
            return true;
        }
        if (field.matches(WEEKDAY_PATTERN)) {
            return validateWeekdayPattern(field);
        }
        return validateNumericField(field, 1, 31, "일");
    }

    private boolean validateMonth(String field) {
        if (field.matches(MONTH_PATTERN)) {
            return true;
        }
        return validateNumericField(field, 1, 12, "월");
    }

    private boolean validateDayOfWeek(String field) {
        if ("?".equals(field)) {
            return true;
        }
        if (field.matches(NTH_WEEKDAY_PATTERN)) {
            return validateNthWeekdayPattern(field);
        }
        if (field.matches(DOW_PATTERN)) {
            return true;
        }
        return validateNumericField(field, 1, 7, "요일");
    }

    private boolean validateYear(String field) {
        return validateNumericField(field, 1970, 2099, "년");
    }

    private boolean validateTimeField(String field, int min, int max, String fieldName) {
        return validateNumericField(field, min, max, fieldName);
    }

    private boolean validateNumericField(String field, int min, int max, String fieldName) {
        if ("*".equals(field)) {
            return true;
        }

        // 범위 패턴 (예: 1-5)
        if (field.contains("-")) {
            return validateRangePattern(field, min, max, fieldName);
        }

        // 목록 패턴 (예: 1,3,5)
        if (field.contains(",")) {
            return validateListPattern(field, min, max, fieldName);
        }

        // 스텝 패턴 (예: 0/15, */5)
        if (field.contains("/")) {
            return validateStepPattern(field, min, max, fieldName);
        }

        // 단일 숫자 값
        try {
            int value = Integer.parseInt(field);
            return value >= min && value <= max;
        } catch (NumberFormatException e) {
            log.warn("{} 필드에 잘못된 숫자 값: {}", fieldName, field);
            return false;
        }
    }

    private boolean validateRangePattern(String field, int min, int max, String fieldName) {
        String[] parts = field.split("-");
        if (parts.length != 2) {
            log.warn("{} 필드의 범위 패턴이 잘못되었습니다: {}", fieldName, field);
            return false;
        }

        try {
            int start = Integer.parseInt(parts[0]);
            int end = Integer.parseInt(parts[1]);
            
            if (start >= min && start <= max && end >= min && end <= max && start <= end) {
                return true;
            }
            
            log.warn("{} 필드의 범위가 유효하지 않습니다: {} (허용 범위: {}-{})", fieldName, field, min, max);
            return false;
        } catch (NumberFormatException e) {
            log.warn("{} 필드의 범위 패턴에 잘못된 숫자: {}", fieldName, field);
            return false;
        }
    }

    private boolean validateListPattern(String field, int min, int max, String fieldName) {
        String[] parts = field.split(",");
        
        for (String part : parts) {
            try {
                int value = Integer.parseInt(part.trim());
                if (value < min || value > max) {
                    log.warn("{} 필드의 목록 값이 범위를 벗어났습니다: {} (허용 범위: {}-{})", fieldName, value, min, max);
                    return false;
                }
            } catch (NumberFormatException e) {
                log.warn("{} 필드의 목록에 잘못된 숫자: {}", fieldName, part);
                return false;
            }
        }
        
        return true;
    }

    private boolean validateStepPattern(String field, int min, int max, String fieldName) {
        String[] parts = field.split("/");
        if (parts.length != 2) {
            log.warn("{} 필드의 스텝 패턴이 잘못되었습니다: {}", fieldName, field);
            return false;
        }

        String base = parts[0];
        String step = parts[1];

        // 스텝 값 검증
        try {
            int stepValue = Integer.parseInt(step);
            if (stepValue <= 0) {
                log.warn("{} 필드의 스텝 값이 양수가 아닙니다: {}", fieldName, stepValue);
                return false;
            }
        } catch (NumberFormatException e) {
            log.warn("{} 필드의 스텝 값이 잘못되었습니다: {}", fieldName, step);
            return false;
        }

        // 베이스 값 검증
        if ("*".equals(base)) {
            return true;
        }

        try {
            int baseValue = Integer.parseInt(base);
            return baseValue >= min && baseValue <= max;
        } catch (NumberFormatException e) {
            log.warn("{} 필드의 베이스 값이 잘못되었습니다: {}", fieldName, base);
            return false;
        }
    }

    private boolean validateWeekdayPattern(String field) {
        // nW 패턴 (예: 15W = 15일에 가장 가까운 평일)
        if (field.endsWith("W")) {
            try {
                String dayStr = field.substring(0, field.length() - 1);
                int day = Integer.parseInt(dayStr);
                return day >= 1 && day <= 31;
            } catch (NumberFormatException e) {
                log.warn("평일 패턴이 잘못되었습니다: {}", field);
                return false;
            }
        }
        
        return false;
    }

    private boolean validateNthWeekdayPattern(String field) {
        // DOW#n 패턴 (예: FRI#2 = 둘째주 금요일)
        if (field.contains("#")) {
            String[] parts = field.split("#");
            if (parts.length != 2) {
                return false;
            }

            String dow = parts[0];
            String nth = parts[1];

            // 요일 검증
            boolean validDow = dow.matches(DOW_PATTERN);

            // n번째 검증 (1-5)
            try {
                int nthValue = Integer.parseInt(nth);
                boolean validNth = nthValue >= 1 && nthValue <= 5;
                
                return validDow && validNth;
            } catch (NumberFormatException e) {
                log.warn("n번째 요일 패턴이 잘못되었습니다: {}", field);
                return false;
            }
        }
        
        return false;
    }

    private boolean validateDayAndDayOfWeekMutualExclusion(String dayField, String dowField) {
        // 일과 요일 중 하나는 반드시 '?' 이어야 함
        boolean dayIsQuestion = "?".equals(dayField);
        boolean dowIsQuestion = "?".equals(dowField);
        
        if (dayIsQuestion && dowIsQuestion) {
            log.warn("일과 요일 필드가 모두 '?'일 수 없습니다.");
            return false;
        }
        
        if (!dayIsQuestion && !dowIsQuestion) {
            log.warn("일과 요일 필드 중 하나는 반드시 '?'이어야 합니다.");
            return false;
        }
        
        return true;
    }
} 