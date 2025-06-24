package com.datasolution.dsflow.entity.enums;

public enum JobParameterType {
    SINGLE,         // 단일 파라미터 작업
    MULTI_PARAM,    // 하나의 파라미터에 대해 여러 값들을 순차 처리
    MATRIX          // 여러 파라미터들의 조합을 매트릭스 형태로 처리
} 