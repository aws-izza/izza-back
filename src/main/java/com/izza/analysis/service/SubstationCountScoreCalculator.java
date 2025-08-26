package com.izza.analysis.service;

import com.izza.analysis.vo.AnalysisStatisticsType;
import com.izza.analysis.service.dto.LandAnalysisData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 변전소 개수 점수 계산기
 * 변전소가 많을수록 높은 점수 (전력 공급 안정성 향상)
 */
@Component
@Slf4j
public class SubstationCountScoreCalculator extends AbstractNormalizedScoreCalculator {
    
    @Override
    protected double getActualValue(LandAnalysisData data) {
        return data.getSubstationCount();
    }

    @Override
    protected double getBaseScore() {
        return 0;
    }

    @Override
    public AnalysisStatisticsType getStatisticsType() {
        return AnalysisStatisticsType.SUBSTATION_COUNT;
    }
}