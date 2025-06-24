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
@Table(name = "code_values")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CodeValue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private CodeCategory category;

    @Column(nullable = false, length = 100)
    private String codeValue; // 실제 코드 값 (11110, A, SMALL 등)

    @Column(nullable = false, length = 200)
    private String codeName; // 코드 표시명

    @Column(length = 100)
    private String parentCodeValue; // 상위 코드 (계층구조 지원)

    @Column(columnDefinition = "TEXT")
    private String metadata; // 추가 메타데이터 (JSON 문자열)

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true; // 활성화 여부

    @Column(nullable = false)
    @Builder.Default
    private Integer sortOrder = 0; // 정렬 순서

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
} 