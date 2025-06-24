package com.datasolution.dsflow.repository;

import com.datasolution.dsflow.entity.JobDefinition;
import com.datasolution.dsflow.entity.enums.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobDefinitionRepository extends JpaRepository<JobDefinition, Long> {

    Optional<JobDefinition> findByJobCode(String jobCode);

    List<JobDefinition> findByStatus(JobStatus status);

    List<JobDefinition> findByStatusOrderByResourceWeightAsc(JobStatus status);

    boolean existsByJobCode(String jobCode);

    @Query("SELECT jd FROM JobDefinition jd WHERE jd.status = :status AND jd.jobCode != :excludeJobCode ORDER BY jd.resourceWeight ASC")
    List<JobDefinition> findActiveJobsExcluding(JobStatus status, String excludeJobCode);

    @Query("SELECT SUM(jd.resourceWeight) FROM JobDefinition jd WHERE jd.status = 'ACTIVE'")
    Integer getTotalResourceWeight();
} 