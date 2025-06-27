package com.datasolution.dsflow.service;


import com.datasolution.dsflow.dto.JobStatisticsDto;
import com.datasolution.dsflow.repository.JobExecutionLogRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional(readOnly = true)
public class JobStatisticsService {
    private final JobExecutionLogRepository logRepository;

    public JobStatisticsService(JobExecutionLogRepository logRepository) {
        this.logRepository = logRepository;
    }

    // TODO: 전체 Job 통계 조회
    public List<JobStatisticsDto> getAllJobStatistics() {
        return logRepository.findJobStatistics();
    }

    // TODO: 특정 기간의 통계 조회
    public List<JobStatisticsDto> getJobStatisticsByPeriod(
            LocalDateTime start, LocalDateTime end) {
        return logRepository.findByJobCodeAndStartTimeBetween(start, end);

    }

//    // TODO: 성공률 계산 로직
//    private double calculateSuccessRate(long total, long success) {
//        return logRepository.findJobStatistics()
//    }

}
