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
@Table(name = "region_codes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegionCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 5)
    private String lawdCd; // 법정동코드 5자리

    @Column(nullable = false, length = 100)
    private String regionName; // 지역명

    @Column(length = 100)
    private String sidoName; // 시도명

    @Column(length = 100)
    private String gugunName; // 구군명

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true; // 활성화 여부

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
} 