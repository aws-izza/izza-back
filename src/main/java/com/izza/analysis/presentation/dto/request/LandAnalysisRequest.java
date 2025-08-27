package com.izza.analysis.presentation.dto.request;

import com.izza.analysis.vo.WeightedStatisticsRange;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 토지 분석 요청 DTO (Presentation Layer)
 * 클라이언트로부터 받는 분석 요청 파라미터만 포함
 */
@Data
@Builder
@Schema(description = "토지 분석 요청")
public class LandAnalysisRequest {
    
    @Schema(description = "법정동 코드", example = "11676000")
    private String fullCode;
    
    @Schema(description = "토지 면적 통계 범위")
    private WeightedStatisticsRange landAreaRange;
    
    @Schema(description = "공시지가 통계 범위")
    private WeightedStatisticsRange landPriceRange;
    
    @Schema(description = "전기 요금 통계 범위")
    private WeightedStatisticsRange electricityCostRange;
    
    @Schema(description = "변전소 개수 통계 범위")
    private WeightedStatisticsRange substationCountRange;
    
    @Schema(description = "송전탑 개수 통계 범위")
    private WeightedStatisticsRange transmissionTowerCountRange;
    
    @Schema(description = "전기선 개수 통계 범위")
    private WeightedStatisticsRange transmissionLineCountRange;
    
    @Schema(description = "인구 밀도 통계 범위")
    private WeightedStatisticsRange populationDensityRange;

    @Schema(description = "재해 발생 통계 범위")
    private WeightedStatisticsRange disasterCountRange;
    
    @Schema(description = "산업 업종 타입 (인구밀도 계산용)", example = "MANUFACTURING", allowableValues = {"MANUFACTURING", "LOGISTICS", "IT"})
    private String industryType;
    
    @Schema(description = "용도지역 필터 조건 (매칭되는 카테고리 목록)", example = "[\"COMMERCIAL\", \"INDUSTRIAL\"]")
    private List<String> targetUseDistrictCodes;
}