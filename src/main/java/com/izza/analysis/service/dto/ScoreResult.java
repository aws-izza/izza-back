package com.izza.analysis.service.dto;

import com.izza.analysis.vo.AnalysisStatisticsType;
import lombok.Builder;
import lombok.Data;

/**
 * 점수 계산 결과를 담는 DTO
 * 원본 점수와 정규화된 점수들을 포함
 */
@Data
@Builder
public class ScoreResult {
    
    /**
     * 통계 유형
     */
    private AnalysisStatisticsType statisticsType;
    
    /**
     * 원본 점수 (0.0 ~ 1.0)
     */
    private double originalScore;
    
    /**
     * 카테고리 내 정규화된 점수 (가중치 적용)
     */
    private double categoryNormalizedScore;
    
    /**
     * 전체 정규화된 점수 (가중치 적용)
     */
    private double globalNormalizedScore;
}