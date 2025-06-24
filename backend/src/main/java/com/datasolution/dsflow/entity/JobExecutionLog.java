package com.datasolution.dsflow.entity;

import com.datasolution.dsflow.entity.enums.ExecutionStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "job_execution_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JobExecutionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "job_definition_id", nullable = false)
    private JobDefinition jobDefinition;

    @Column(nullable = false)
    private Long batchJobInstanceId;

    @Column(nullable = false)
    private Long batchJobExecutionId;

    @Column(nullable = false)
    private LocalDate baseDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExecutionStatus status;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime startTime;

    private LocalDateTime endTime;

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @Column(columnDefinition = "TEXT")
    private String resultData;

    @Column(nullable = false)
    @Builder.Default
    private Integer processedCount = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer successCount = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer failCount = 0;

    @Column(columnDefinition = "TEXT")
    private String parameters;

    public void markAsCompleted() {
        this.status = ExecutionStatus.COMPLETED;
        this.endTime = LocalDateTime.now();
    }

    public void markAsFailed(String errorMessage) {
        this.status = ExecutionStatus.FAILED;
        this.endTime = LocalDateTime.now();
        this.errorMessage = errorMessage;
    }

    public void updateCounts(int processed, int success, int fail) {
        this.processedCount = processed;
        this.successCount = success;
        this.failCount = fail;
    }
} 