package com.izza.analysis.service;

import com.izza.analysis.vo.AnalysisStatisticsType;
import com.izza.analysis.service.dto.LandAnalysisData;

/**
 * 점수 계산을 위한 공통 인터페이스
 * 모든 점수 계산 로직은 동일한 LandAnalysisRequest를 받아서 double 점수를 반환
 */
public interface ScoreCalculator {
    
    /**
     * 점수를 계산하는 메서드
     * 
     * @param data 토지 분석 데이터 객체 (토지 정보, 지역 정보, 통계 범위 포함)
     * @return 계산된 점수 (0.0 ~ 1.0 범위)
     */
    double calculateScore(LandAnalysisData data);
    
    /**
     * 이 계산기가 처리하는 통계 유형을 반환
     * 
     * @return 통계 유형
     */
    AnalysisStatisticsType getStatisticsType();
    
    /**
     * 계산기의 이름을 반환 (로깅, 디버깅 용도)
     * 
     * @return 계산기 이름
     */
    default String getCalculatorName() {
        return this.getClass().getSimpleName();
    }
}