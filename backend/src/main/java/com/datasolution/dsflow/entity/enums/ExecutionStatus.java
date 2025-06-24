package com.datasolution.dsflow.entity.enums;

public enum ExecutionStatus {
    STARTED("시작됨"),
    RUNNING("실행 중"),
    COMPLETED("완료"),
    FAILED("실패"),
    STOPPED("중단됨");

    private final String description;

    ExecutionStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
} 