package com.datasolution.dsflow.repository;

import com.datasolution.dsflow.entity.CodeValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CodeValueRepository extends JpaRepository<CodeValue, Long> {
    
    /**
     * 카테고리별 활성화된 코드 값 조회
     */
    @Query("SELECT cv.codeValue FROM CodeValue cv " +
           "JOIN cv.category cc " +
           "WHERE cc.categoryCode = :categoryCode AND cv.isActive = true " +
           "ORDER BY cv.sortOrder")
    List<String> findCodeValuesByCategoryCode(@Param("categoryCode") String categoryCode);
    
    /**
     * 카테고리별 활성화된 코드 값 상세 조회
     */
    @Query("SELECT cv FROM CodeValue cv " +
           "JOIN cv.category cc " +
           "WHERE cc.categoryCode = :categoryCode AND cv.isActive = true " +
           "ORDER BY cv.sortOrder")
    List<CodeValue> findCodeValueDetailsByCategoryCode(@Param("categoryCode") String categoryCode);
    
    /**
     * 부모 코드로 하위 코드 조회 (계층구조)
     */
    @Query("SELECT cv FROM CodeValue cv " +
           "JOIN cv.category cc " +
           "WHERE cc.categoryCode = :categoryCode " +
           "AND cv.parentCodeValue = :parentCodeValue " +
           "AND cv.isActive = true " +
           "ORDER BY cv.sortOrder")
    List<CodeValue> findChildCodesByParent(@Param("categoryCode") String categoryCode, 
                                          @Param("parentCodeValue") String parentCodeValue);
    
    /**
     * 최상위 레벨 코드 조회 (부모가 없는 코드)
     */
    @Query("SELECT cv FROM CodeValue cv " +
           "JOIN cv.category cc " +
           "WHERE cc.categoryCode = :categoryCode " +
           "AND cv.parentCodeValue IS NULL " +
           "AND cv.isActive = true " +
           "ORDER BY cv.sortOrder")
    List<CodeValue> findTopLevelCodes(@Param("categoryCode") String categoryCode);
    
    /**
     * 특정 코드 값 존재 여부 확인
     */
    @Query("SELECT COUNT(cv) > 0 FROM CodeValue cv " +
           "JOIN cv.category cc " +
           "WHERE cc.categoryCode = :categoryCode " +
           "AND cv.codeValue = :codeValue")
    boolean existsByCodeValueAndCategory(@Param("categoryCode") String categoryCode, 
                                        @Param("codeValue") String codeValue);
} 