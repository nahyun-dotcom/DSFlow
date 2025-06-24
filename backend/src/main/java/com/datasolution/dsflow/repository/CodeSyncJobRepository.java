package com.datasolution.dsflow.repository;

import com.datasolution.dsflow.entity.CodeSyncJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CodeSyncJobRepository extends JpaRepository<CodeSyncJob, Long> {
    
    /**
     * 활성화된 동기화 작업 조회
     */
    List<CodeSyncJob> findByIsActiveTrueOrderByCreatedAtDesc();
    
    /**
     * 자동 동기화 활성화된 작업 조회
     */
    List<CodeSyncJob> findByIsActiveTrueAndAutoSyncTrueOrderByCreatedAtDesc();
    
    /**
     * 동기화 작업 코드로 조회
     */
    Optional<CodeSyncJob> findBySyncJobCodeAndIsActiveTrue(String syncJobCode);
    
    /**
     * 카테고리별 동기화 작업 조회
     */
    List<CodeSyncJob> findByTargetCategoryCodeAndIsActiveTrueOrderByCreatedAtDesc(String categoryCode);
    
    /**
     * 동기화 작업 코드 존재 여부 확인
     */
    boolean existsBySyncJobCode(String syncJobCode);
    
    /**
     * 특정 시간 이후 동기화가 필요한 작업 조회
     */
    @Query("SELECT csj FROM CodeSyncJob csj WHERE csj.isActive = true AND csj.autoSync = true " +
           "AND (csj.lastSyncTime IS NULL OR csj.lastSyncTime < :threshold)")
    List<CodeSyncJob> findJobsNeedingSync(@Param("threshold") java.time.LocalDateTime threshold);
} 