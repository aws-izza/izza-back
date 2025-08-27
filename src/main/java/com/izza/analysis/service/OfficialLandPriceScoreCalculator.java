package com.izza.analysis.service;

import com.izza.analysis.vo.AnalysisStatisticsType;
import com.izza.analysis.service.dto.LandAnalysisData;
import org.springframework.stereotype.Component;

/**
 * 공시지가 점수 계산기
 * 공시지가가 클수록 높은 점수 (기준 점수 0.5)
 */
@Component
public class OfficialLandPriceScoreCalculator extends AbstractNormalizedScoreCalculator {
    
    @Override
    protected double getActualValue(LandAnalysisData data) {
        return data.getLand().getOfficialLandPrice().doubleValue();
    }

    @Override
    protected double getBaseScore() {
        return 0.5;
    }

    @Override
    public AnalysisStatisticsType getStatisticsType() {
        return AnalysisStatisticsType.OFFICIAL_LAND_PRICE;
    }
}