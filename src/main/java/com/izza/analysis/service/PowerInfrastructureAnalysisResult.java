package com.izza.analysis.service;

import com.izza.analysis.persistent.LandPowerInfrastructureProximity;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 전력 인프라 분석 결과 DTO
 */
@Data
@Builder
public class PowerInfrastructureAnalysisResult {

    /**
     * 토지 ID
     */
    private Long landId;

    /**
     * 변전소 개수 (15km 내)
     */
    private Integer substationCount;

    /**
     * 변전소 정규화 점수 (0~1)
     */
    private BigDecimal substationScore;

    /**
     * 송전탑 개수 (15km 내)
     */
    private Integer transmissionTowerCount;

    /**
     * 송전탑 정규화 점수 (0~1)
     */
    private BigDecimal transmissionTowerScore;

    /**
     * 전력 인프라 근접성 상세 정보
     */
    private List<LandPowerInfrastructureProximity> proximityDetails;

    /**
     * 분석 면적 (km²)
     */
    private BigDecimal analysisAreaKm2;

    /**
     * 전력 인프라 종합 점수 계산 (변전소 + 송전탑 평균)
     */
    public BigDecimal getOverallInfrastructureScore() {
        if (substationScore == null || transmissionTowerScore == null) {
            return BigDecimal.ZERO;
        }

        return substationScore.add(transmissionTowerScore)
                .divide(BigDecimal.valueOf(2), 4, BigDecimal.ROUND_HALF_UP);
    }

    /**
     * 전력 인프라 총 개수
     */
    public Integer getTotalInfrastructureCount() {
        int substation = substationCount != null ? substationCount : 0;
        int tower = transmissionTowerCount != null ? transmissionTowerCount : 0;
        return substation + tower;
    }

    /**
     * 변전소 우세 여부 (변전소가 송전탑보다 많은지)
     */
    public boolean isSubstationDominant() {
        int substation = substationCount != null ? substationCount : 0;
        int tower = transmissionTowerCount != null ? transmissionTowerCount : 0;
        return substation > tower;
    }

    /**
     * 고개수 전력 인프라 지역 여부 (총 개수가 평균 이상인지 판단용)
     */
    public boolean isHighCountArea(Integer averageCount) {
        if (averageCount == null) {
            return false;
        }
        return getTotalInfrastructureCount().compareTo(averageCount) > 0;
    }
}