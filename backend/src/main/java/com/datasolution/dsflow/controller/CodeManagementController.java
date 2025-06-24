package com.datasolution.dsflow.controller;

import com.datasolution.dsflow.entity.CodeCategory;
import com.datasolution.dsflow.entity.CodeValue;
import com.datasolution.dsflow.service.CodeManagementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/codes")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "코드 관리", description = "유연한 코드 관리 시스템 API")
public class CodeManagementController {

    private final CodeManagementService codeManagementService;

    @GetMapping("/categories")
    @Operation(summary = "활성화된 코드 카테고리 목록 조회", description = "모든 활성화된 코드 카테고리 목록을 조회합니다.")
    public ResponseEntity<List<CodeCategory>> getAllActiveCategories() {
        log.info("활성화된 코드 카테고리 목록 조회 요청");
        List<CodeCategory> categories = codeManagementService.getAllActiveCategories();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{categoryCode}/values")
    @Operation(summary = "카테고리별 코드 값 조회", description = "특정 카테고리의 활성화된 코드 값 목록을 조회합니다.")
    public ResponseEntity<List<String>> getCodeValuesByCategory(@PathVariable String categoryCode) {
        log.info("카테고리별 코드 값 조회 요청: {}", categoryCode);
        List<String> codeValues = codeManagementService.getCodeValuesByCategory(categoryCode);
        return ResponseEntity.ok(codeValues);
    }

    @GetMapping("/{categoryCode}/details")
    @Operation(summary = "카테고리별 코드 값 상세 조회", description = "특정 카테고리의 활성화된 코드 값 상세 정보를 조회합니다.")
    public ResponseEntity<List<CodeValue>> getCodeValueDetailsByCategory(@PathVariable String categoryCode) {
        log.info("카테고리별 코드 값 상세 조회 요청: {}", categoryCode);
        List<CodeValue> codeValues = codeManagementService.getCodeValueDetailsByCategory(categoryCode);
        return ResponseEntity.ok(codeValues);
    }

    @GetMapping("/{categoryCode}/top-level")
    @Operation(summary = "최상위 레벨 코드 조회", description = "계층구조에서 최상위 레벨 코드들을 조회합니다.")
    public ResponseEntity<List<CodeValue>> getTopLevelCodes(@PathVariable String categoryCode) {
        log.info("최상위 레벨 코드 조회 요청: {}", categoryCode);
        List<CodeValue> topLevelCodes = codeManagementService.getTopLevelCodes(categoryCode);
        return ResponseEntity.ok(topLevelCodes);
    }

    @GetMapping("/{categoryCode}/children/{parentCodeValue}")
    @Operation(summary = "하위 코드 조회", description = "계층구조에서 특정 부모 코드의 하위 코드들을 조회합니다.")
    public ResponseEntity<List<CodeValue>> getChildCodes(
            @PathVariable String categoryCode,
            @PathVariable String parentCodeValue) {
        log.info("하위 코드 조회 요청: {} - {}", categoryCode, parentCodeValue);
        List<CodeValue> childCodes = codeManagementService.getChildCodes(categoryCode, parentCodeValue);
        return ResponseEntity.ok(childCodes);
    }

    @PostMapping("/categories")
    @Operation(summary = "새로운 코드 카테고리 생성", description = "새로운 코드 카테고리를 생성합니다.")
    public ResponseEntity<CodeCategory> createCategory(@RequestBody Map<String, String> request) {
        log.info("새로운 코드 카테고리 생성 요청: {}", request.get("categoryCode"));
        
        CodeCategory category = codeManagementService.createCategory(
                request.get("categoryCode"),
                request.get("categoryName"),
                request.get("description")
        );
        
        return ResponseEntity.ok(category);
    }

    @PostMapping("/{categoryCode}/values")
    @Operation(summary = "새로운 코드 값 생성", description = "특정 카테고리에 새로운 코드 값을 생성합니다.")
    public ResponseEntity<CodeValue> createCodeValue(
            @PathVariable String categoryCode,
            @RequestBody Map<String, String> request) {
        log.info("새로운 코드 값 생성 요청: {} - {}", categoryCode, request.get("codeValue"));
        
        CodeValue codeValue = codeManagementService.createCodeValue(
                categoryCode,
                request.get("codeValue"),
                request.get("codeName"),
                request.get("parentCodeValue"),
                request.get("metadata")
        );
        
        return ResponseEntity.ok(codeValue);
    }

    @PatchMapping("/categories/{categoryId}/status")
    @Operation(summary = "코드 카테고리 상태 변경", description = "코드 카테고리의 활성화/비활성화 상태를 변경합니다.")
    public ResponseEntity<Void> updateCategoryStatus(
            @PathVariable Long categoryId,
            @RequestParam Boolean isActive) {
        log.info("코드 카테고리 상태 변경 요청: ID={}, isActive={}", categoryId, isActive);
        codeManagementService.updateCategoryStatus(categoryId, isActive);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/values/{codeValueId}/status")
    @Operation(summary = "코드 값 상태 변경", description = "코드 값의 활성화/비활성화 상태를 변경합니다.")
    public ResponseEntity<Void> updateCodeValueStatus(
            @PathVariable Long codeValueId,
            @RequestParam Boolean isActive) {
        log.info("코드 값 상태 변경 요청: ID={}, isActive={}", codeValueId, isActive);
        codeManagementService.updateCodeValueStatus(codeValueId, isActive);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/statistics")
    @Operation(summary = "카테고리별 코드 개수 통계", description = "각 카테고리별 코드 개수 통계를 조회합니다.")
    public ResponseEntity<List<Object[]>> getCodeCountByCategory() {
        log.info("카테고리별 코드 개수 통계 조회 요청");
        List<Object[]> statistics = codeManagementService.getCodeCountByCategory();
        return ResponseEntity.ok(statistics);
    }

    @GetMapping("/{categoryCode}/exists")
    @Operation(summary = "카테고리 존재 여부 확인", description = "특정 카테고리 코드의 존재 여부를 확인합니다.")
    public ResponseEntity<Boolean> categoryExists(@PathVariable String categoryCode) {
        log.info("카테고리 존재 여부 확인 요청: {}", categoryCode);
        boolean exists = codeManagementService.categoryExists(categoryCode);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/{categoryCode}/values/{codeValue}/exists")
    @Operation(summary = "코드 값 존재 여부 확인", description = "특정 카테고리 내 코드 값의 존재 여부를 확인합니다.")
    public ResponseEntity<Boolean> codeValueExists(
            @PathVariable String categoryCode,
            @PathVariable String codeValue) {
        log.info("코드 값 존재 여부 확인 요청: {} - {}", categoryCode, codeValue);
        boolean exists = codeManagementService.codeValueExists(categoryCode, codeValue);
        return ResponseEntity.ok(exists);
    }
} 