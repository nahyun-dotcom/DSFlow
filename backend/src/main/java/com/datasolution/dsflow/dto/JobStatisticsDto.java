package com.datasolution.dsflow.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Job 통계 DTO")
public class JobStatisticsDto {

    @Schema(description = "Job 코드", example = "WEATHER_API_JOB")
    private String jobCode;

    @Schema(description = "Job 명", example = "날씨 API 데이터 수집")
    private String jobName;

    @Schema(description = "총 실행 횟수", example = "1")
    private Long totalExecutions;

    @Schema(description = "성공 실행 횟수", example = "1")
    private Long successfulExecutions;

    @Schema(description = "실패 실행 횟수", example = "1")
    private Long failedExecutions;

    @Schema(description = "마지막 실행 시간", example = "")
    private LocalDateTime lastExecutionTime;

    @Schema(description = "성공률 (%)", example = "95.0")
    private Double successRate;

    // JPQL 쿼리에서 사용하는 생성자
    public JobStatisticsDto(String jobCode, String jobName, Long totalExecutions,
                            Long successfulExecutions, Long failedExecutions, LocalDateTime lastExecutionTime) {
        this.jobCode = jobCode;
        this.jobName = jobName;
        this.totalExecutions = totalExecutions;
        this.successfulExecutions = successfulExecutions;
        this.failedExecutions = failedExecutions;
        this.lastExecutionTime = lastExecutionTime;

        if (totalExecutions != null && totalExecutions > 0 && successfulExecutions != null) {
            this.successRate = (double) successfulExecutions / totalExecutions * 100;
        } else {
            this.successRate = null;
        }
    }

}
