package com.izza.analysis.persistent.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 토지별 전력 인프라 요약 정보 모델
 * land_power_infrastructure_summary 테이블과 매핑
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LandPowerInfrastructureSummary {
    
    private Long landId;
    private Integer substationCount;
    private BigDecimal substationClosestDistanceMeters;
    private Integer transmissionLineCount;
    private BigDecimal transmissionLineClosestDistanceMeters;
    private Integer transmissionTowerCount;
    private BigDecimal transmissionTowerClosestDistanceMeters;
    private Integer totalInfrastructureCount;
    private Boolean hasHighVoltage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}