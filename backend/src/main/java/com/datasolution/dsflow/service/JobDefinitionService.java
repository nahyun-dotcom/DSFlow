package com.datasolution.dsflow.service;

import com.datasolution.dsflow.dto.JobDefinitionDto;
import com.datasolution.dsflow.entity.JobDefinition;
import com.datasolution.dsflow.entity.enums.JobStatus;
import com.datasolution.dsflow.exception.BusinessException;
import com.datasolution.dsflow.repository.JobDefinitionRepository;
import com.datasolution.dsflow.util.CronExpressionValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class JobDefinitionService {

    private final JobDefinitionRepository jobDefinitionRepository;
    private final CronExpressionValidator cronValidator;

    public Page<JobDefinitionDto> getAllJobs(Pageable pageable) {
        return jobDefinitionRepository.findAll(pageable)
                .map(this::convertToDto);
    }

    public List<JobDefinitionDto> getActiveJobs() {
        return jobDefinitionRepository.findByStatus(JobStatus.ACTIVE)
                .stream()
                .map(this::convertToDto)
                .toList();
    }

    public JobDefinitionDto getJobByCode(String jobCode) {
        JobDefinition job = jobDefinitionRepository.findByJobCode(jobCode)
                .orElseThrow(() -> new BusinessException("Job을 찾을 수 없습니다: " + jobCode));
        return convertToDto(job);
    }

    @Transactional
    public JobDefinitionDto createJob(JobDefinitionDto dto) {
        validateJobDto(dto);
        
        if (jobDefinitionRepository.existsByJobCode(dto.getJobCode())) {
            throw new BusinessException("이미 존재하는 Job 코드입니다: " + dto.getJobCode());
        }

        JobDefinition job = convertToEntity(dto);
        job.setCreatedAt(LocalDateTime.now());
        job.setUpdatedAt(LocalDateTime.now());
        
        JobDefinition savedJob = jobDefinitionRepository.save(job);
        log.info("Job 생성 완료: {}", savedJob.getJobCode());
        
        return convertToDto(savedJob);
    }

    @Transactional
    public JobDefinitionDto updateJob(String jobCode, JobDefinitionDto dto) {
        JobDefinition existingJob = jobDefinitionRepository.findByJobCode(jobCode)
                .orElseThrow(() -> new BusinessException("Job을 찾을 수 없습니다: " + jobCode));

        validateJobDto(dto);

        // Job 코드 변경 시 중복 체크
        if (!existingJob.getJobCode().equals(dto.getJobCode()) && 
            jobDefinitionRepository.existsByJobCode(dto.getJobCode())) {
            throw new BusinessException("이미 존재하는 Job 코드입니다: " + dto.getJobCode());
        }

        updateJobFields(existingJob, dto);
        existingJob.setUpdatedAt(LocalDateTime.now());
        
        JobDefinition updatedJob = jobDefinitionRepository.save(existingJob);
        log.info("Job 수정 완료: {}", updatedJob.getJobCode());
        
        return convertToDto(updatedJob);
    }

    @Transactional
    public void deleteJob(String jobCode) {
        JobDefinition job = jobDefinitionRepository.findByJobCode(jobCode)
                .orElseThrow(() -> new BusinessException("Job을 찾을 수 없습니다: " + jobCode));

        job.setStatus(JobStatus.DELETED);
        job.setUpdatedAt(LocalDateTime.now());
        
        jobDefinitionRepository.save(job);
        log.info("Job 삭제 완료: {}", jobCode);
    }

    @Transactional
    public void toggleJobStatus(String jobCode) {
        JobDefinition job = jobDefinitionRepository.findByJobCode(jobCode)
                .orElseThrow(() -> new BusinessException("Job을 찾을 수 없습니다: " + jobCode));

        JobStatus newStatus = job.getStatus() == JobStatus.ACTIVE ? JobStatus.INACTIVE : JobStatus.ACTIVE;
        job.setStatus(newStatus);
        job.setUpdatedAt(LocalDateTime.now());
        
        jobDefinitionRepository.save(job);
        log.info("Job 상태 변경 완료: {} -> {}", jobCode, newStatus);
    }

    private void validateJobDto(JobDefinitionDto dto) {
        if (!cronValidator.isValidExpression(dto.getCronExpression())) {
            throw new BusinessException("잘못된 Cron 표현식입니다: " + dto.getCronExpression());
        }

        if (dto.getResourceWeight() != null && (dto.getResourceWeight() < 1 || dto.getResourceWeight() > 10)) {
            throw new BusinessException("리소스 가중치는 1-10 사이여야 합니다: " + dto.getResourceWeight());
        }
    }

    private void updateJobFields(JobDefinition job, JobDefinitionDto dto) {
        job.setJobCode(dto.getJobCode());
        job.setJobName(dto.getJobName());
        job.setDescription(dto.getDescription());
        job.setMethodType(dto.getMethodType());
        job.setResourceUrl(dto.getResourceUrl());
        job.setParameters(dto.getParameters());
        job.setCronExpression(dto.getCronExpression());
        job.setResourceWeight(dto.getResourceWeight() != null ? dto.getResourceWeight() : 1);
        
        if (dto.getStatus() != null) {
            job.setStatus(dto.getStatus());
        }
        
        if (dto.getUpdatedBy() != null) {
            job.setUpdatedBy(dto.getUpdatedBy());
        }
    }

    private JobDefinition convertToEntity(JobDefinitionDto dto) {
        return JobDefinition.builder()
                .jobCode(dto.getJobCode())
                .jobName(dto.getJobName())
                .description(dto.getDescription())
                .methodType(dto.getMethodType())
                .resourceUrl(dto.getResourceUrl())
                .parameters(dto.getParameters())
                .cronExpression(dto.getCronExpression())
                .resourceWeight(dto.getResourceWeight() != null ? dto.getResourceWeight() : 1)
                .status(dto.getStatus() != null ? dto.getStatus() : JobStatus.ACTIVE)
                .createdBy(dto.getCreatedBy())
                .updatedBy(dto.getUpdatedBy())
                .build();
    }

    private JobDefinitionDto convertToDto(JobDefinition job) {
        return JobDefinitionDto.builder()
                .id(job.getId())
                .jobCode(job.getJobCode())
                .jobName(job.getJobName())
                .description(job.getDescription())
                .methodType(job.getMethodType())
                .resourceUrl(job.getResourceUrl())
                .parameters(job.getParameters())
                .cronExpression(job.getCronExpression())
                .resourceWeight(job.getResourceWeight())
                .status(job.getStatus())
                .createdAt(job.getCreatedAt())
                .updatedAt(job.getUpdatedAt())
                .createdBy(job.getCreatedBy())
                .updatedBy(job.getUpdatedBy())
                .build();
    }
} 