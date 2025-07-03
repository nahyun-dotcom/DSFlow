package com.datasolution.dsflow.controller;

import com.datasolution.dsflow.dto.JobDefinitionDto;
import com.datasolution.dsflow.service.JobDefinitionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/jobs")
@RequiredArgsConstructor
@Tag(name = "Job Definition", description = "Job 정의 관리 API")
public class JobDefinitionController {

    private final JobDefinitionService jobDefinitionService;

    /***
     *  Job 목록 조회
     * @param pageable
     * @return JobDefinitionDto
     */
    @GetMapping
    @Operation(summary = "Job 목록 조회", description = "등록된 모든 Job 목록을 페이징하여 조회합니다.")
    public ResponseEntity<Page<JobDefinitionDto>> getAllJobs(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(jobDefinitionService.getAllJobs(pageable));
    }

    @GetMapping("/active")
    @Operation(summary = "활성 Job 목록 조회", description = "활성 상태인 Job 목록을 조회합니다.")
    public ResponseEntity<List<JobDefinitionDto>> getActiveJobs() {
        return ResponseEntity.ok(jobDefinitionService.getActiveJobs());
    }

    @GetMapping("/{jobCode}")
    @Operation(summary = "Job 상세 조회", description = "Job 코드로 특정 Job의 상세 정보를 조회합니다.")
    public ResponseEntity<JobDefinitionDto> getJobByCode(
            @Parameter(description = "Job 코드", example = "WEATHER_API_JOB")
            @PathVariable String jobCode) {
        return ResponseEntity.ok(jobDefinitionService.getJobByCode(jobCode));
    }

    @PostMapping
    @Operation(summary = "Job 생성", description = "새로운 Job을 생성합니다.")
    public ResponseEntity<JobDefinitionDto> createJob(
            @Valid @RequestBody JobDefinitionDto jobDto) {
        return ResponseEntity.ok(jobDefinitionService.createJob(jobDto));
    }

    @PutMapping("/{jobCode}")
    @Operation(summary = "Job 수정", description = "기존 Job 정보를 수정합니다.")
    public ResponseEntity<JobDefinitionDto> updateJob(
            @Parameter(description = "Job 코드", example = "WEATHER_API_JOB")
            @PathVariable String jobCode,
            @Valid @RequestBody JobDefinitionDto jobDto) {
        return ResponseEntity.ok(jobDefinitionService.updateJob(jobCode, jobDto));
    }

    @DeleteMapping("/{jobCode}")
    @Operation(summary = "Job 삭제", description = "Job을 삭제합니다. (논리 삭제)")
    public ResponseEntity<Void> deleteJob(
            @Parameter(description = "Job 코드", example = "WEATHER_API_JOB")
            @PathVariable String jobCode) {
        jobDefinitionService.deleteJob(jobCode);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{jobCode}/toggle-status")
    @Operation(summary = "Job 상태 토글", description = "Job의 활성/비활성 상태를 변경합니다.")
    public ResponseEntity<Void> toggleJobStatus(
            @Parameter(description = "Job 코드", example = "WEATHER_API_JOB")
            @PathVariable String jobCode) {
        jobDefinitionService.toggleJobStatus(jobCode);
        return ResponseEntity.ok().build();
    }
} 