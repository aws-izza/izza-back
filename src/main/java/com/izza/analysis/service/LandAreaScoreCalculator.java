package com.izza.analysis.service;

import com.izza.analysis.vo.AnalysisStatisticsType;
import com.izza.analysis.service.dto.LandAnalysisData;
import org.springframework.stereotype.Component;

/**
 * 토지면적 점수 계산기
 * 토지면적이 클수록 높은 점수 (기준 점수 0.5)
 */
@Component
public class LandAreaScoreCalculator extends AbstractNormalizedScoreCalculator {

    @Override
    protected double getActualValue(LandAnalysisData data) {
        return data.getLand().getLandArea().doubleValue();
    }

    @Override
    protected double getBaseScore() {
        return 0.5;
    }

    @Override
    public AnalysisStatisticsType getStatisticsType() {
        return AnalysisStatisticsType.LAND_AREA;
    }
}