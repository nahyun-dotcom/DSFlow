package com.datasolution.dsflow.dto;

import com.datasolution.dsflow.entity.enums.ExecutionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Job 실행 로그 DTO")
public class JobExecutionLogDto {

    @Schema(description = "로그 ID", example = "1")
    private Long id;

    @Schema(description = "Job 코드", example = "WEATHER_API_JOB")
    private String jobCode;

    @Schema(description = "Job 명", example = "날씨 API 데이터 수집")
    private String jobName;

    @Schema(description = "Batch Job Instance ID", example = "1")
    private Long batchJobInstanceId;

    @Schema(description = "Batch Job Execution ID", example = "1")
    private Long batchJobExecutionId;

    @Schema(description = "기준일", example = "2023-12-01")
    private LocalDate baseDate;

    @Schema(description = "실행 상태", example = "COMPLETED")
    private ExecutionStatus status;

    @Schema(description = "시작 시간", example = "2023-12-01T09:00:00")
    private LocalDateTime startTime;

    @Schema(description = "종료 시간", example = "2023-12-01T09:05:30")
    private LocalDateTime endTime;

    @Schema(description = "오류 메시지")
    private String errorMessage;

    @Schema(description = "결과 데이터")
    private String resultData;

    @Schema(description = "처리된 건수", example = "100")
    private Integer processedCount;

    @Schema(description = "성공 건수", example = "95")
    private Integer successCount;

    @Schema(description = "실패 건수", example = "5")
    private Integer failCount;

    @Schema(description = "실행 파라미터")
    private String parameters;

    @Schema(description = "실행 시간 (초)", example = "330")
    public Long getExecutionTimeSeconds() {
        if (startTime != null && endTime != null) {
            return java.time.Duration.between(startTime, endTime).getSeconds();
        }
        return null;
    }

    @Schema(description = "성공률 (%)", example = "95.0")
    public Double getSuccessRate() {
        if (processedCount != null && processedCount > 0) {
            return (double) successCount / processedCount * 100;
        }
        return null;
    }
} 