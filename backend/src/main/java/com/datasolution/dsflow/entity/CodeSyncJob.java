package com.datasolution.dsflow.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 사용자 정의 코드 동기화 API 정보
 * 사용자가 직접 외부 API를 등록하여 코드 데이터를 자동 수집할 수 있도록 함
 */
@Entity
@Table(name = "code_sync_jobs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CodeSyncJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 100)
    private String syncJobCode; // 고유 동기화 작업 코드

    @Column(nullable = false, length = 200)
    private String syncJobName; // 동기화 작업명

    @Column(nullable = false, length = 100)
    private String targetCategoryCode; // 대상 코드 카테고리

    @Column(nullable = false, length = 1000)
    private String apiUrl; // 외부 API URL

    @Column(length = 50)
    private String httpMethod; // GET, POST 등

    @Column(columnDefinition = "TEXT")
    private String requestHeaders; // 요청 헤더 (JSON 형태)

    @Column(columnDefinition = "TEXT")
    private String requestParameters; // 요청 파라미터 (JSON 형태)

    @Column(columnDefinition = "TEXT")
    private String requestBody; // 요청 본문 (POST용, JSON 형태)

    @Column(length = 200)
    private String codeValueJsonPath; // 코드 값 추출용 JSONPath

    @Column(length = 200)
    private String codeNameJsonPath; // 코드 명 추출용 JSONPath

    @Column(length = 200)
    private String metadataJsonPath; // 메타데이터 추출용 JSONPath

    @Column(length = 200)
    private String parentCodeJsonPath; // 부모 코드 추출용 JSONPath

    @Column(length = 100)
    private String cronExpression; // 동기화 주기 (Cron 표현식)

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true; // 활성화 여부

    @Column(nullable = false)
    @Builder.Default
    private Boolean autoSync = true; // 자동 동기화 여부

    @Column(nullable = false)
    @Builder.Default
    private Integer timeoutSeconds = 30; // API 타임아웃 (초)

    @Column(nullable = false)
    @Builder.Default
    private Integer retryCount = 3; // 재시도 횟수

    @Column(length = 2000)
    private String description; // 설명

    @Column(length = 500)
    private String lastSyncResult; // 마지막 동기화 결과

    @Column
    private LocalDateTime lastSyncTime; // 마지막 동기화 시간

    @Column
    private Integer lastSyncCount; // 마지막 동기화된 코드 수

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(length = 100)
    private String createdBy;

    @Column(length = 100)
    private String updatedBy;

    // 대상 코드 카테고리와의 연관관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private CodeCategory category;

    // 추가 getter 메서드들
    public String getSyncJobCode() {
        return syncJobCode;
    }

    public String getSyncJobName() {
        return syncJobName;
    }

    public String getTargetCategoryCode() {
        return targetCategoryCode;
    }

    public String getApiUrl() {
        return apiUrl;
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public String getRequestHeaders() {
        return requestHeaders;
    }

    public String getRequestParameters() {
        return requestParameters;
    }

    public String getRequestBody() {
        return requestBody;
    }

    public String getCodeValueJsonPath() {
        return codeValueJsonPath;
    }

    public String getCodeNameJsonPath() {
        return codeNameJsonPath;
    }

    public String getMetadataJsonPath() {
        return metadataJsonPath;
    }

    public String getParentCodeJsonPath() {
        return parentCodeJsonPath;
    }

    public String getCronExpression() {
        return cronExpression;
    }

    public Boolean getAutoSync() {
        return autoSync;
    }

    public Integer getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public String getDescription() {
        return description;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    public void setLastSyncTime(LocalDateTime lastSyncTime) {
        this.lastSyncTime = lastSyncTime;
    }

    public void setLastSyncResult(String lastSyncResult) {
        this.lastSyncResult = lastSyncResult;
    }

    public void setLastSyncCount(int lastSyncCount) {
        this.lastSyncCount = lastSyncCount;
    }
} 