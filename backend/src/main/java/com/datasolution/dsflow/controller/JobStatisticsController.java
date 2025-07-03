package com.datasolution.dsflow.controller;
import com.datasolution.dsflow.dto.JobStatisticsDto;
import com.datasolution.dsflow.service.JobStatisticsService;
import org.apache.coyote.Response;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.time.Instant;
import java.time.ZoneId;


@RestController
@RequestMapping("/statistics")
public class JobStatisticsController {
    private final JobStatisticsService statisticsService;

    public JobStatisticsController(JobStatisticsService statisticsService) {
        this.statisticsService = statisticsService;
    }

    @GetMapping("/jobs")
    public ResponseEntity<List<JobStatisticsDto>> getJobStatistics() {
        List<JobStatisticsDto> result = statisticsService.getAllJobStatistics();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/jobs/period")
    public ResponseEntity<List<JobStatisticsDto>> getJobStatisticsByPeriod(
            @RequestParam("start") Instant start,
            @RequestParam("end") Instant end) {

        LocalDateTime startTime = LocalDateTime.ofInstant(start, ZoneId.of("Asia/Seoul"));
        LocalDateTime endTime = LocalDateTime.ofInstant(end, ZoneId.of("Asia/Seoul"));

        return ResponseEntity.ok(statisticsService.getJobStatisticsByPeriod(startTime, endTime));
    }


}
