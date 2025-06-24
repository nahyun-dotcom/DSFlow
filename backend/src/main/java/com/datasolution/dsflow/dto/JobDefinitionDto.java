package com.datasolution.dsflow.dto;

import com.datasolution.dsflow.entity.enums.JobStatus;
import com.datasolution.dsflow.entity.enums.MethodType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Job 정의 DTO")
public class JobDefinitionDto {

    @Schema(description = "Job ID", example = "1")
    private Long id;

    @NotBlank(message = "Job 코드는 필수입니다")
    @Size(max = 50, message = "Job 코드는 50자 이하여야 합니다")
    @Pattern(regexp = "^[A-Z0-9_]+$", message = "Job 코드는 대문자, 숫자, 언더스코어만 사용 가능합니다")
    @Schema(description = "Job 코드", example = "WEATHER_API_JOB")
    private String jobCode;

    @NotBlank(message = "Job 명은 필수입니다")
    @Size(max = 100, message = "Job 명은 100자 이하여야 합니다")
    @Schema(description = "Job 명", example = "날씨 API 데이터 수집")
    private String jobName;

    @Size(max = 500, message = "설명은 500자 이하여야 합니다")
    @Schema(description = "Job 설명", example = "기상청 공공 API를 통한 날씨 데이터 수집")
    private String description;

    @NotNull(message = "메소드 타입은 필수입니다")
    @Schema(description = "메소드 타입", example = "API_GET")
    private MethodType methodType;

    @NotBlank(message = "리소스 URL은 필수입니다")
    @Size(max = 1000, message = "리소스 URL은 1000자 이하여야 합니다")
    @Schema(description = "리소스 URL", example = "https://apis.data.go.kr/1360000/VilageFcstInfoService_2.0/getVilageFcst")
    private String resourceUrl;

    @Schema(description = "파라미터 (JSON 형식)", example = "{\"serviceKey\":\"YOUR_API_KEY\",\"numOfRows\":\"10\"}")
    private String parameters;

    @NotBlank(message = "Cron 표현식은 필수입니다")
    @Schema(description = "Cron 표현식", example = "0 0 9 * * ?")
    private String cronExpression;

    @Schema(description = "리소스 가중치 (1-10)", example = "1")
    private Integer resourceWeight;

    @Schema(description = "Job 상태", example = "ACTIVE")
    private JobStatus status;

    @Schema(description = "생성일시")
    private LocalDateTime createdAt;

    @Schema(description = "수정일시")
    private LocalDateTime updatedAt;

    @Schema(description = "생성자", example = "admin")
    private String createdBy;

    @Schema(description = "수정자", example = "admin")
    private String updatedBy;
} 