package com.datasolution.dsflow.repository;

import com.datasolution.dsflow.entity.CodeCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CodeCategoryRepository extends JpaRepository<CodeCategory, Long> {
    
    /**
     * 활성화된 코드 카테고리 조회
     */
    List<CodeCategory> findByIsActiveTrueOrderBySortOrder();
    
    /**
     * 카테고리 코드로 조회
     */
    Optional<CodeCategory> findByCategoryCodeAndIsActiveTrue(String categoryCode);
    
    /**
     * 카테고리 코드 존재 여부 확인
     */
    boolean existsByCategoryCode(String categoryCode);
    
    /**
     * 카테고리별 코드 개수 조회
     */
    @Query("SELECT cc.categoryCode, COUNT(cv) FROM CodeCategory cc " +
           "LEFT JOIN cc.codeValues cv ON cv.isActive = true " +
           "WHERE cc.isActive = true " +
           "GROUP BY cc.categoryCode")
    List<Object[]> getCodeCountByCategory();
} 