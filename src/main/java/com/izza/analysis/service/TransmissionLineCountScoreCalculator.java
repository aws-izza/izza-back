package com.izza.analysis.service;

import com.izza.analysis.vo.AnalysisStatisticsType;
import com.izza.analysis.service.dto.LandAnalysisData;
import org.springframework.stereotype.Component;

/**
 * 전기선 개수 점수 계산기
 * 전기선이 많을수록 높은 점수 (전력 인프라 충실성 향상)
 */
@Component
public class TransmissionLineCountScoreCalculator extends AbstractNormalizedScoreCalculator {
    
    @Override
    protected double getActualValue(LandAnalysisData data) {
        return data.getTransmissionLineCount();
    }

    @Override
    protected double getBaseScore() {
        return 0;
    }

    @Override
    public AnalysisStatisticsType getStatisticsType() {
        return AnalysisStatisticsType.TRANSMISSION_LINE_COUNT;
    }
}