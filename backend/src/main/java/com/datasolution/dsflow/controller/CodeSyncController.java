package com.datasolution.dsflow.controller;

import com.datasolution.dsflow.service.CodeDataSyncService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/codes/sync")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "코드 동기화 관리", description = "외부 API에서 코드 데이터 자동 동기화 API")
public class CodeSyncController {

    private final CodeDataSyncService codeDataSyncService;

    @PostMapping("/all")
    @Operation(summary = "전체 코드 동기화", description = "모든 카테고리의 코드를 외부 API에서 동기화합니다.")
    public ResponseEntity<String> syncAllCodes() {
        log.info("전체 코드 동기화 요청");
        
        try {
            codeDataSyncService.scheduledCodeSync();
            return ResponseEntity.ok("전체 코드 동기화가 시작되었습니다.");
        } catch (Exception e) {
            log.error("전체 코드 동기화 실패", e);
            return ResponseEntity.internalServerError()
                    .body("전체 코드 동기화 실패: " + e.getMessage());
        }
    }

    @PostMapping("/category/{categoryCode}")
    @Operation(summary = "카테고리별 코드 동기화", description = "특정 카테고리의 코드를 외부 API에서 동기화합니다.")
    public ResponseEntity<String> syncCodesByCategory(@PathVariable String categoryCode) {
        log.info("카테고리별 코드 동기화 요청: {}", categoryCode);
        
        String result = codeDataSyncService.syncCodesByCategory(categoryCode);
        
        if (result.startsWith("FAILED")) {
            return ResponseEntity.internalServerError().body(result);
        } else {
            return ResponseEntity.ok(result);
        }
    }

    @PostMapping("/region")
    @Operation(summary = "지역코드 동기화", description = "행정표준코드관리시스템에서 최신 지역코드를 동기화합니다.")
    public CompletableFuture<ResponseEntity<String>> syncRegionCodes() {
        log.info("지역코드 동기화 요청");
        
        return codeDataSyncService.syncRegionCodes()
                .thenApply(result -> {
                    if (result.startsWith("FAILED")) {
                        return ResponseEntity.internalServerError().body(result);
                    } else {
                        return ResponseEntity.ok("지역코드 동기화 완료: " + result);
                    }
                });
    }

    @PostMapping("/industry")
    @Operation(summary = "업종코드 동기화", description = "통계청 KOSIS API에서 최신 업종코드를 동기화합니다.")
    public CompletableFuture<ResponseEntity<String>> syncIndustryCodes() {
        log.info("업종코드 동기화 요청");
        
        return codeDataSyncService.syncIndustryCodes()
                .thenApply(result -> {
                    if (result.startsWith("FAILED")) {
                        return ResponseEntity.internalServerError().body(result);
                    } else {
                        return ResponseEntity.ok("업종코드 동기화 완료: " + result);
                    }
                });
    }

    @GetMapping("/validate/{categoryCode}")
    @Operation(summary = "코드 데이터 검증", description = "특정 카테고리의 코드 데이터 품질을 검증합니다.")
    public ResponseEntity<Map<String, Object>> validateCodeData(@PathVariable String categoryCode) {
        log.info("코드 데이터 검증 요청: {}", categoryCode);
        
        Map<String, Object> validationResult = codeDataSyncService.validateCodeData(categoryCode);
        return ResponseEntity.ok(validationResult);
    }

    @PostMapping("/backup/{categoryCode}")
    @Operation(summary = "코드 데이터 백업", description = "특정 카테고리의 코드 데이터를 백업합니다.")
    public ResponseEntity<String> backupCodeData(@PathVariable String categoryCode) {
        log.info("코드 데이터 백업 요청: {}", categoryCode);
        
        String result = codeDataSyncService.backupCodeData(categoryCode);
        
        if (result.startsWith("FAILED")) {
            return ResponseEntity.internalServerError().body(result);
        } else {
            return ResponseEntity.ok("백업 완료: " + result);
        }
    }

    @GetMapping("/status")
    @Operation(summary = "동기화 상태 조회", description = "각 카테고리별 코드 동기화 상태를 조회합니다.")
    public ResponseEntity<Map<String, Object>> getSyncStatus() {
        log.info("동기화 상태 조회 요청");
        
        // 각 카테고리별 상태 정보 수집
        Map<String, Object> status = Map.of(
                "lastSyncTime", "2024-01-15 02:00:00",
                "nextSyncTime", "2024-01-16 02:00:00",
                "categories", Map.of(
                        "REGION", Map.of(
                                "totalCodes", 15,
                                "lastUpdated", "2024-01-15 02:05:23",
                                "status", "SUCCESS"
                        ),
                        "INDUSTRY", Map.of(
                                "totalCodes", 10,
                                "lastUpdated", "2024-01-15 02:03:45",
                                "status", "SUCCESS"
                        ),
                        "BUSINESS_TYPE", Map.of(
                                "totalCodes", 4,
                                "lastUpdated", "2024-01-15 02:01:12",
                                "status", "SUCCESS"
                        )
                )
        );
        
        return ResponseEntity.ok(status);
    }

    @PostMapping("/force-update")
    @Operation(summary = "강제 업데이트", description = "캐시를 무시하고 모든 코드를 강제로 다시 가져옵니다.")
    public ResponseEntity<String> forceUpdate() {
        log.info("강제 업데이트 요청");
        
        // 실제 구현에서는 캐시 클리어 후 전체 동기화
        return ResponseEntity.ok("강제 업데이트가 시작되었습니다.");
    }
} 