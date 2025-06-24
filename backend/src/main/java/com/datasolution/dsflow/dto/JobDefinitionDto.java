package com.datasolution.dsflow.dto;

import com.datasolution.dsflow.entity.enums.JobParameterType;
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
    @Schema(description = "Job 코드", example = "REAL_ESTATE_API_JOB")
    private String jobCode;

    @NotBlank(message = "Job 명은 필수입니다")
    @Size(max = 100, message = "Job 명은 100자 이하여야 합니다")
    @Schema(description = "Job 명", example = "부동산 실거래가 데이터 수집")
    private String jobName;

    @Size(max = 500, message = "설명은 500자 이하여야 합니다")
    @Schema(description = "Job 설명", example = "국토교통부 부동산 실거래가 공공 API를 통한 데이터 수집")
    private String description;

    @NotNull(message = "메소드 타입은 필수입니다")
    @Schema(description = "메소드 타입", example = "API_GET")
    private MethodType methodType;

    @NotBlank(message = "리소스 URL은 필수입니다")
    @Size(max = 1000, message = "리소스 URL은 1000자 이하여야 합니다")
    @Schema(description = "리소스 URL", example = "http://openapi.molit.go.kr/OpenAPI_ToolInstallPackage/service/rest/RTMSOBJSvc/getRTMSDataSvcAptTradeDev")
    private String resourceUrl;

    @Schema(description = "파라미터 (JSON 형식)", example = "{\"serviceKey\":\"YOUR_API_KEY\",\"numOfRows\":\"1000\"}")
    private String parameters;

    @NotBlank(message = "Cron 표현식은 필수입니다")
    @Schema(description = "Cron 표현식", example = "0 0 2 * * ?")
    private String cronExpression;

    @Schema(description = "리소스 가중치 (1-10)", example = "1")
    private Integer resourceWeight;

    @Schema(description = "Job 상태", example = "ACTIVE")
    private JobStatus status;

    @Schema(description = "파라미터 타입", example = "MATRIX")
    private JobParameterType parameterType;

    @Schema(description = "배치 크기 (한 번에 처리할 파라미터 조합 수)", example = "10")
    private Integer batchSize;

    @Schema(description = "API 호출 간 지연 시간 (초)", example = "1")
    private Integer delaySeconds;

    @Schema(description = "지역코드 사용 여부", example = "true")
    private Boolean useRegionCodes;

    @Schema(description = "처리할 월 범위", example = "3")
    private Integer dateRangeMonths;

    @Schema(description = "생성일시")
    private LocalDateTime createdAt;

    @Schema(description = "수정일시")
    private LocalDateTime updatedAt;

    @Schema(description = "생성자", example = "admin")
    private String createdBy;

    @Schema(description = "수정자", example = "admin")
    private String updatedBy;
} 