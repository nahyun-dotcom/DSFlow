package com.datasolution.dsflow.entity.enums;

public enum ValueSourceType {
    DB_QUERY,       // 데이터베이스 쿼리 결과
    STATIC_LIST,    // 정적 값 목록 (JSON 배열)
    DATE_RANGE,     // 날짜 범위 (시작일, 종료일, 간격)
    API_CALL,       // 외부 API 호출 결과
    FILE_LIST       // 파일에서 읽어온 값 목록
} 