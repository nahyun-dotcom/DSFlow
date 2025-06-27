package com.datasolution.dsflow.repository;

import com.datasolution.dsflow.dto.JobStatisticsDto;
import com.datasolution.dsflow.entity.JobExecutionLog;
import com.datasolution.dsflow.entity.enums.ExecutionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface JobExecutionLogRepository extends JpaRepository<JobExecutionLog, Long> {

    Optional<JobExecutionLog> findByBatchJobExecutionId(Long batchJobExecutionId);

    List<JobExecutionLog> findByJobDefinitionJobCodeOrderByStartTimeDesc(String jobCode);

    Page<JobExecutionLog> findByOrderByStartTimeDesc(Pageable pageable);

    Page<JobExecutionLog> findByStatusOrderByStartTimeDesc(ExecutionStatus status, Pageable pageable);

    Page<JobExecutionLog> findByJobDefinitionJobCodeOrderByStartTimeDesc(String jobCode, Pageable pageable);

    @Query("SELECT jel FROM JobExecutionLog jel WHERE jel.startTime BETWEEN :startDate AND :endDate ORDER BY jel.startTime DESC")
    Page<JobExecutionLog> findByStartTimeBetween(@Param("startDate") LocalDateTime startDate, 
                                                @Param("endDate") LocalDateTime endDate, 
                                                Pageable pageable);

    @Query("SELECT jel FROM JobExecutionLog jel WHERE jel.baseDate = :baseDate AND jel.jobDefinition.jobCode = :jobCode")
    Optional<JobExecutionLog> findByBaseDateAndJobCode(@Param("baseDate") LocalDate baseDate, 
                                                       @Param("jobCode") String jobCode);

    @Query("SELECT COUNT(jel) FROM JobExecutionLog jel WHERE jel.status = :status AND jel.startTime >= :fromDate")
    Long countByStatusAndStartTimeAfter(@Param("status") ExecutionStatus status, 
                                       @Param("fromDate") LocalDateTime fromDate);

    @Query("SELECT jel.status, COUNT(jel) FROM JobExecutionLog jel WHERE jel.startTime >= :fromDate GROUP BY jel.status")
    List<Object[]> getStatusStatistics(@Param("fromDate") LocalDateTime fromDate);

    @Query("SELECT jel FROM JobExecutionLog jel WHERE jel.jobDefinition.jobCode = :jobCode AND jel.baseDate = :baseDate AND jel.status IN ('STARTED', 'RUNNING')")
    List<JobExecutionLog> findRunningJobsByJobCodeAndBaseDate(@Param("jobCode") String jobCode, 
                                                             @Param("baseDate") LocalDate baseDate);

    @Query("SELECT new com.datasolution.dsflow.dto.JobStatisticsDto(" +
            "log.jobDefinition.jobCode, log.jobDefinition.jobName, " +
            "COUNT(log), " +
            "SUM(CASE WHEN log.status = 'SUCCESS' THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN log.status = 'FAILED' THEN 1 ELSE 0 END), " +
            "MAX(log.endTime)) " +
            "FROM JobExecutionLog log " +
            "GROUP BY log.jobDefinition.jobCode, log.jobDefinition.jobName")
    List<JobStatisticsDto> findJobStatistics();

    @Query("SELECT new com.datasolution.dsflow.dto.JobStatisticsDto(" +
            "log.jobDefinition.jobCode, log.jobDefinition.jobName, " +
            "COUNT(log), " +
            "SUM(CASE WHEN log.status = 'SUCCESS' THEN 1 ELSE 0 END), " +
            "SUM(CASE WHEN log.status = 'FAILED' THEN 1 ELSE 0 END), " +
            "MAX(log.endTime)) " +
            "FROM JobExecutionLog log " +
            "WHERE log.startTime BETWEEN :start AND :end " +
            "GROUP BY log.jobDefinition.jobCode, log.jobDefinition.jobName")
    List<JobStatisticsDto> findByJobCodeAndStartTimeBetween(@Param("start") LocalDateTime start,
                                                     @Param("end") LocalDateTime end);

} 