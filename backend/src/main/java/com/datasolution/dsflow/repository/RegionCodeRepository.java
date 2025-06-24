package com.datasolution.dsflow.repository;

import com.datasolution.dsflow.entity.RegionCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RegionCodeRepository extends JpaRepository<RegionCode, Long> {
    
    List<RegionCode> findByIsActiveTrue();
    
    @Query("SELECT r.lawdCd FROM RegionCode r WHERE r.isActive = true ORDER BY r.lawdCd")
    List<String> findActiveLawdCdList();
} 