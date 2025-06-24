package com.datasolution.dsflow.entity.enums;

public enum MethodType {
    API_GET("API GET 호출"),
    API_POST("API POST 호출"),
    FILE_DOWNLOAD("파일 다운로드"),
    FILE_PROCESS("파일 처리");

    private final String description;

    MethodType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
} 