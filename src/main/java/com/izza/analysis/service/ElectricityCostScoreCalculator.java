package com.izza.analysis.service;

import com.izza.analysis.vo.AnalysisStatisticsType;
import com.izza.analysis.service.dto.LandAnalysisData;
import org.springframework.stereotype.Component;

/**
 * 전기요금 점수 계산기
 * 전기요금이 클수록 높은 점수 (기준 점수 0.5)
 */
@Component
public class ElectricityCostScoreCalculator extends AbstractNormalizedScoreCalculator {
    
    @Override
    protected double getActualValue(LandAnalysisData data) {
        if(data.getElectricityCostInfo() != null) {
            return data.getElectricityCostInfo().unitCost().doubleValue();
        }
        return 170.0;
    }

    @Override
    protected double getBaseScore() {
        return 0.5;
    }

    @Override
    public AnalysisStatisticsType getStatisticsType() {
        return AnalysisStatisticsType.ELECTRICITY_COST;
    }
}