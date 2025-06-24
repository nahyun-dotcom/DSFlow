package com.datasolution.dsflow.entity.enums;

public enum JobParameterType {
    SINGLE,         // 단일 파라미터 작업
    MULTI_REGION,   // 다중 지역 코드 작업
    MULTI_DATE,     // 다중 날짜 작업
    MATRIX          // 지역코드 x 날짜 매트릭스 작업
} 