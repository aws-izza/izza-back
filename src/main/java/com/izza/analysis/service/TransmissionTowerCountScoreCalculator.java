package com.izza.analysis.service;

import com.izza.analysis.vo.AnalysisStatisticsType;
import com.izza.analysis.service.dto.LandAnalysisData;
import org.springframework.stereotype.Component;

/**
 * 송전탑 개수 점수 계산기
 * 송전탑이 많을수록 높은 점수 (전력 전송 인프라 충실성 향상)
 */
@Component
public class TransmissionTowerCountScoreCalculator extends AbstractNormalizedScoreCalculator {
    
    @Override
    protected double getActualValue(LandAnalysisData data) {
        return data.getTransmissionTowerCount();
    }

    @Override
    protected double getBaseScore() {
        return 0;
    }

    @Override
    public AnalysisStatisticsType getStatisticsType() {
        return AnalysisStatisticsType.TRANSMISSION_TOWER_COUNT;
    }
}