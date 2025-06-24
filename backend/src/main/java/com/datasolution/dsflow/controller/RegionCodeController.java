package com.datasolution.dsflow.controller;

import com.datasolution.dsflow.entity.RegionCode;
import com.datasolution.dsflow.service.RegionCodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/region-codes")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "지역코드 관리", description = "지역코드 관리 API")
public class RegionCodeController {

    private final RegionCodeService regionCodeService;

    @GetMapping
    @Operation(summary = "활성화된 지역코드 목록 조회", description = "활성화된 모든 지역코드 목록을 조회합니다.")
    public ResponseEntity<List<RegionCode>> getAllActiveRegionCodes() {
        log.info("활성화된 지역코드 목록 조회 요청");
        List<RegionCode> regionCodes = regionCodeService.getAllActiveRegionCodes();
        return ResponseEntity.ok(regionCodes);
    }

    @GetMapping("/codes")
    @Operation(summary = "활성화된 지역코드 목록 조회 (코드만)", description = "활성화된 지역코드 목록을 코드만 조회합니다.")
    public ResponseEntity<List<String>> getActiveLawdCdList() {
        log.info("활성화된 지역코드 목록 조회 요청 (코드만)");
        List<String> lawdCdList = regionCodeService.getActiveLawdCdList();
        return ResponseEntity.ok(lawdCdList);
    }

    @GetMapping("/count")
    @Operation(summary = "활성화된 지역코드 개수 조회", description = "활성화된 지역코드의 총 개수를 조회합니다.")
    public ResponseEntity<Long> getTotalActiveRegionCount() {
        log.info("활성화된 지역코드 개수 조회 요청");
        long count = regionCodeService.getTotalActiveRegionCount();
        return ResponseEntity.ok(count);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "지역코드 상태 변경", description = "지역코드의 활성화/비활성화 상태를 변경합니다.")
    public ResponseEntity<Void> updateRegionCodeStatus(
            @PathVariable Long id,
            @RequestParam Boolean isActive) {
        log.info("지역코드 상태 변경 요청: ID={}, isActive={}", id, isActive);
        regionCodeService.updateRegionCodeStatus(id, isActive);
        return ResponseEntity.ok().build();
    }

    @PostMapping
    @Operation(summary = "새로운 지역코드 추가", description = "새로운 지역코드를 추가합니다.")
    public ResponseEntity<RegionCode> addRegionCode(
            @RequestParam String lawdCd,
            @RequestParam String regionName,
            @RequestParam(required = false) String sidoName,
            @RequestParam(required = false) String gugunName) {
        log.info("새로운 지역코드 추가 요청: lawdCd={}, regionName={}", lawdCd, regionName);
        RegionCode regionCode = regionCodeService.addRegionCode(lawdCd, regionName, sidoName, gugunName);
        return ResponseEntity.ok(regionCode);
    }
} 