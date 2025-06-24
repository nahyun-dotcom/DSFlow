package com.datasolution.dsflow.entity;

import com.datasolution.dsflow.entity.enums.JobStatus;
import com.datasolution.dsflow.entity.enums.MethodType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "job_definitions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String jobCode;

    @Column(nullable = false, length = 100)
    private String jobName;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MethodType methodType;

    @Column(nullable = false, length = 1000)
    private String resourceUrl;

    @Column(columnDefinition = "TEXT")
    private String parameters;

    @Column(nullable = false, length = 20)
    private String cronExpression;

    @Column(nullable = false)
    @Builder.Default
    private Integer resourceWeight = 1;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private JobStatus status = JobStatus.ACTIVE;

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