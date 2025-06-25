package com.datasolution.dsflow.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "job_parameter_configs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobParameterConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long jobDefinitionId;

    @Column(nullable = false, length = 50)
    private String parameterName; // 파라미터 이름 (예: LAWD_CD, DEAL_YMD, CATEGORY_CD 등)

    @Column(nullable = false, length = 20)
    private String valueSourceType; // 값 소스 타입: DB_QUERY, STATIC_LIST, DATE_RANGE, API_CALL

    @Column(columnDefinition = "TEXT")
    private String valueSource; // 값 소스 (SQL 쿼리, JSON 배열, 날짜 범위 설정 등)

    @Column(length = 500)
    private String description; // 파라미터 설명

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(nullable = false)
    @Builder.Default
    private Integer sortOrder = 0; // 파라미터 순서

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // 추가 getter 메서드들
    public String getValueSourceType() {
        return valueSourceType;
    }

    public String getValueSource() {
        return valueSource;
    }
} 