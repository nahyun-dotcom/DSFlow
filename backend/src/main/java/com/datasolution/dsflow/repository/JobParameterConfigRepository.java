package com.datasolution.dsflow.repository;

import com.datasolution.dsflow.entity.JobParameterConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobParameterConfigRepository extends JpaRepository<JobParameterConfig, Long> {
    
    List<JobParameterConfig> findByJobDefinitionIdAndIsActiveTrueOrderBySortOrder(Long jobDefinitionId);
    
    List<JobParameterConfig> findByJobDefinitionId(Long jobDefinitionId);
    
    void deleteByJobDefinitionId(Long jobDefinitionId);
} 