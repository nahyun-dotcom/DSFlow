package com.datasolution.dsflow.service;

import com.datasolution.dsflow.entity.RegionCode;
import com.datasolution.dsflow.repository.RegionCodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class RegionCodeService {

    private final RegionCodeRepository regionCodeRepository;

    /**
     * 모든 활성화된 지역코드 조회
     */
    public List<RegionCode> getAllActiveRegionCodes() {
        return regionCodeRepository.findByIsActiveTrue();
    }

    /**
     * 활성화된 지역코드 목록 조회 (코드만)
     */
    public List<String> getActiveLawdCdList() {
        return regionCodeRepository.findActiveLawdCdList();
    }

    /**
     * 지역코드 개수 조회
     */
    public long getTotalActiveRegionCount() {
        return regionCodeRepository.findByIsActiveTrue().size();
    }

    /**
     * 지역코드 활성화/비활성화
     */
    @Transactional
    public void updateRegionCodeStatus(Long id, Boolean isActive) {
        RegionCode regionCode = regionCodeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("지역코드를 찾을 수 없습니다: " + id));
        
        regionCode.setIsActive(isActive);
        regionCodeRepository.save(regionCode);
        
        log.info("지역코드 상태 변경: {} -> {}", regionCode.getLawdCd(), isActive ? "활성화" : "비활성화");
    }

    /**
     * 새로운 지역코드 추가
     */
    @Transactional
    public RegionCode addRegionCode(String lawdCd, String regionName, String sidoName, String gugunName) {
        RegionCode regionCode = RegionCode.builder()
                .lawdCd(lawdCd)
                .regionName(regionName)
                .sidoName(sidoName)
                .gugunName(gugunName)
                .isActive(true)
                .build();
        
        RegionCode saved = regionCodeRepository.save(regionCode);
        log.info("새로운 지역코드 추가: {} - {}", lawdCd, regionName);
        
        return saved;
    }
} 