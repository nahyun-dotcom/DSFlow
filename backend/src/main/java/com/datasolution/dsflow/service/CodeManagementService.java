package com.datasolution.dsflow.service;

import com.datasolution.dsflow.entity.CodeCategory;
import com.datasolution.dsflow.entity.CodeValue;
import com.datasolution.dsflow.repository.CodeCategoryRepository;
import com.datasolution.dsflow.repository.CodeValueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CodeManagementService {

    private final CodeCategoryRepository codeCategoryRepository;
    private final CodeValueRepository codeValueRepository;

    /**
     * 모든 활성화된 코드 카테고리 조회
     */
    public List<CodeCategory> getAllActiveCategories() {
        return codeCategoryRepository.findByIsActiveTrueOrderBySortOrder();
    }

    /**
     * 카테고리별 코드 값 조회 (코드만)
     */
    public List<String> getCodeValuesByCategory(String categoryCode) {
        return codeValueRepository.findCodeValuesByCategoryCode(categoryCode);
    }

    /**
     * 카테고리별 코드 값 상세 조회
     */
    public List<CodeValue> getCodeValueDetailsByCategory(String categoryCode) {
        return codeValueRepository.findCodeValueDetailsByCategoryCode(categoryCode);
    }

    /**
     * 계층구조 코드 조회 - 최상위 레벨
     */
    public List<CodeValue> getTopLevelCodes(String categoryCode) {
        return codeValueRepository.findTopLevelCodes(categoryCode);
    }

    /**
     * 계층구조 코드 조회 - 하위 코드
     */
    public List<CodeValue> getChildCodes(String categoryCode, String parentCodeValue) {
        return codeValueRepository.findChildCodesByParent(categoryCode, parentCodeValue);
    }

    /**
     * 코드 카테고리 등록
     */
    @Transactional
    public CodeCategory createCategory(String categoryCode, String categoryName, String description) {
        if (codeCategoryRepository.existsByCategoryCode(categoryCode)) {
            throw new IllegalArgumentException("이미 존재하는 카테고리 코드입니다: " + categoryCode);
        }

        CodeCategory category = CodeCategory.builder()
                .categoryCode(categoryCode)
                .categoryName(categoryName)
                .description(description)
                .isActive(true)
                .sortOrder(0)
                .build();

        CodeCategory saved = codeCategoryRepository.save(category);
        log.info("새로운 코드 카테고리 생성: {} - {}", categoryCode, categoryName);
        
        return saved;
    }

    /**
     * 코드 값 등록
     */
    @Transactional
    public CodeValue createCodeValue(String categoryCode, String codeValue, String codeName, 
                                   String parentCodeValue, String metadata) {
        CodeCategory category = codeCategoryRepository.findByCategoryCodeAndIsActiveTrue(categoryCode)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리입니다: " + categoryCode));

        if (codeValueRepository.existsByCodeValueAndCategory(categoryCode, codeValue)) {
            throw new IllegalArgumentException("이미 존재하는 코드 값입니다: " + codeValue);
        }

        CodeValue newCodeValue = CodeValue.builder()
                .category(category)
                .codeValue(codeValue)
                .codeName(codeName)
                .parentCodeValue(parentCodeValue)
                .metadata(metadata)
                .isActive(true)
                .sortOrder(0)
                .build();

        CodeValue saved = codeValueRepository.save(newCodeValue);
        log.info("새로운 코드 값 생성: {} - {} (카테고리: {})", codeValue, codeName, categoryCode);
        
        return saved;
    }

    /**
     * 코드 카테고리 활성화/비활성화
     */
    @Transactional
    public void updateCategoryStatus(Long categoryId, Boolean isActive) {
        CodeCategory category = codeCategoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리입니다: " + categoryId));
        
        category.setIsActive(isActive);
        codeCategoryRepository.save(category);
        
        log.info("카테고리 상태 변경: {} -> {}", category.getCategoryCode(), 
                isActive ? "활성화" : "비활성화");
    }

    /**
     * 코드 값 활성화/비활성화
     */
    @Transactional
    public void updateCodeValueStatus(Long codeValueId, Boolean isActive) {
        CodeValue codeValue = codeValueRepository.findById(codeValueId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 코드 값입니다: " + codeValueId));
        
        codeValue.setIsActive(isActive);
        codeValueRepository.save(codeValue);
        
        log.info("코드 값 상태 변경: {} -> {}", codeValue.getCodeValue(), 
                isActive ? "활성화" : "비활성화");
    }

    /**
     * 카테고리별 코드 개수 조회
     */
    public List<Object[]> getCodeCountByCategory() {
        return codeCategoryRepository.getCodeCountByCategory();
    }

    /**
     * 특정 카테고리 존재 여부 확인
     */
    public boolean categoryExists(String categoryCode) {
        return codeCategoryRepository.existsByCategoryCode(categoryCode);
    }

    /**
     * 특정 코드 값 존재 여부 확인
     */
    public boolean codeValueExists(String categoryCode, String codeValue) {
        return codeValueRepository.existsByCodeValueAndCategory(categoryCode, codeValue);
    }
} 