package com.izza.analysis.service.dto;

import com.izza.analysis.vo.AnalysisStatisticsType;
import com.izza.analysis.vo.WeightedStatisticsRange;
import com.izza.search.persistent.model.Land;
import com.izza.search.vo.ElectricityCostInfo;
import com.izza.search.vo.EmergencyTextInfo;
import com.izza.search.vo.PopulationInfo;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * 토지 분석을 위한 종합적인 데이터 객체 (Service Layer)
 * 검색을 통해 조회된 토지 정보와 지역 상세 정보, 그리고 분석 파라미터를 포함
 */
@Data
@Builder
public class LandAnalysisData {
    
    // === 검색을 통해 조회된 데이터 ===
    
    // 토지 기본 정보
    private Land land;
    
    // 지역 상세 정보 (AreaDetailResponse의 구성 요소들)
    private ElectricityCostInfo electricityCostInfo;
    private EmergencyTextInfo emergencyTextInfo;
    private PopulationInfo populationInfo;
    
    // 전력 인프라 정보
    private Integer substationCount;
    private Integer transmissionTowerCount;
    private Integer transmissionLineCount;
    
    // === 분석 요청 파라미터 ===
    
    // 통계 범위 정보 (통계 유형별 min/max 값과 가중치)
    private Map<AnalysisStatisticsType, WeightedStatisticsRange> statisticsRanges;
    
    // 카테고리별 정규화된 가중치 (카테고리 내에서 100% 기준)
    private Map<AnalysisStatisticsType, Double> categoryNormalizedWeights;
    
    // 전체 정규화된 가중치 (전체에서 100% 기준)
    private Map<AnalysisStatisticsType, Double> globalNormalizedWeights;
    
    // 용도지역 필터 조건
    private List<String> targetUseDistrictCodes;
    
}