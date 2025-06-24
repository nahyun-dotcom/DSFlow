package com.datasolution.dsflow.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "code_categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CodeCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String categoryCode; // 카테고리 코드 (REGION, INDUSTRY, BUSINESS_TYPE 등)

    @Column(nullable = false, length = 100)
    private String categoryName; // 카테고리 표시명

    @Column(length = 500)
    private String description; // 카테고리 설명

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

    // 카테고리에 속한 코드 값들
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CodeValue> codeValues;
} 