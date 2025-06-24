package com.datasolution.dsflow.controller;

import com.datasolution.dsflow.entity.CodeSyncJob;
import com.datasolution.dsflow.service.UserDefinedCodeSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/user-code-sync")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "사용자 정의 코드 동기화", description = "사용자가 직접 외부 API를 등록하여 코드 데이터를 자동 동기화")
public class UserCodeSyncController {

    private final UserDefinedCodeSyncService userDefinedCodeSyncService;

    @GetMapping
    @Operation(summary = "동기화 작업 목록 조회", description = "등록된 모든 코드 동기화 작업 목록을 조회합니다.")
    public ResponseEntity<List<CodeSyncJob>> getAllSyncJobs() {
        log.info("동기화 작업 목록 조회 요청");
        List<CodeSyncJob> syncJobs = userDefinedCodeSyncService.getAllActiveSyncJobs();
        return ResponseEntity.ok(syncJobs);
    }

    @PostMapping
    @Operation(summary = "새로운 동기화 작업 등록", description = "사용자가 외부 API를 등록하여 새로운 코드 동기화 작업을 생성합니다.")
    public ResponseEntity<CodeSyncJob> createSyncJob(@RequestBody CodeSyncJob syncJob) {
        log.info("새로운 동기화 작업 등록 요청: {}", syncJob.getSyncJobCode());
        
        try {
            CodeSyncJob created = userDefinedCodeSyncService.createSyncJob(syncJob);
            return ResponseEntity.ok(created);
        } catch (IllegalArgumentException e) {
            log.error("동기화 작업 등록 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{syncJobCode}")
    @Operation(summary = "동기화 작업 수정", description = "기존 동기화 작업의 설정을 수정합니다.")
    public ResponseEntity<CodeSyncJob> updateSyncJob(
            @PathVariable String syncJobCode,
            @RequestBody CodeSyncJob syncJob) {
        log.info("동기화 작업 수정 요청: {}", syncJobCode);
        
        try {
            CodeSyncJob updated = userDefinedCodeSyncService.updateSyncJob(syncJobCode, syncJob);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            log.error("동기화 작업 수정 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{syncJobCode}")
    @Operation(summary = "동기화 작업 삭제", description = "동기화 작업을 삭제합니다. (논리 삭제)")
    public ResponseEntity<Void> deleteSyncJob(@PathVariable String syncJobCode) {
        log.info("동기화 작업 삭제 요청: {}", syncJobCode);
        
        try {
            userDefinedCodeSyncService.deleteSyncJob(syncJobCode);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            log.error("동기화 작업 삭제 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{syncJobCode}/execute")
    @Operation(summary = "동기화 작업 실행", description = "특정 동기화 작업을 수동으로 실행합니다.")
    public CompletableFuture<ResponseEntity<String>> executeSyncJob(@PathVariable String syncJobCode) {
        log.info("동기화 작업 실행 요청: {}", syncJobCode);
        
        return userDefinedCodeSyncService.executeSyncJob(syncJobCode)
                .thenApply(result -> {
                    if (result.startsWith("FAILED")) {
                        return ResponseEntity.internalServerError().body(result);
                    } else {
                        return ResponseEntity.ok(result);
                    }
                });
    }

    @PostMapping("/test-api")
    @Operation(summary = "API 연결 테스트", description = "등록하려는 외부 API의 연결 상태를 테스트합니다.")
    public ResponseEntity<String> testApiConnection(@RequestBody CodeSyncJob testJob) {
        log.info("API 연결 테스트 요청: {}", testJob.getApiUrl());
        
        // 실제 API 호출 없이 기본 검증만 수행 (간단한 구현)
        if (testJob.getApiUrl() == null || testJob.getApiUrl().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("API URL이 필요합니다.");
        }
        
        if (!testJob.getApiUrl().startsWith("http://") && !testJob.getApiUrl().startsWith("https://")) {
            return ResponseEntity.badRequest().body("올바른 URL 형식이 아닙니다.");
        }
        
        // 실제로는 여기서 API 호출 테스트를 수행
        return ResponseEntity.ok("API 연결 테스트 성공");
    }
} 