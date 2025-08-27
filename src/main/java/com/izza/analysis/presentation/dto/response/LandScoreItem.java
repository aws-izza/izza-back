package com.izza.analysis.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 개별 토지의 점수 정보
 */
@Data
@Builder
@Schema(description = "토지 점수 정보")
public class LandScoreItem {
    
    @Schema(description = "토지 ID")
    private Long landId;
    
    @Schema(description = "토지 주소")
    private String address;
    
    @Schema(description = "토지 면적 (㎡)")
    private BigDecimal landArea;
    
    @Schema(description = "공시지가 (원/㎡)")
    private BigDecimal officialLandPrice;
    
    @Schema(description = "총합 점수 (0.0 ~ 1.0)")
    private double totalScore;
    
    @Schema(description = "카테고리별 점수")
    private List<CategoryScoreDetail> categoryScores;

    @Schema(description = "총합 점수 기준 카테고리별 점수")
    private List<CategoryScoreDetail> globalScores;
    
//    @Schema(description = "순위 (1부터 시작)")
//    private int rank;
    
    /**
     * 카테고리별 점수 상세 정보
     */
    @Data
    @Builder
    @Schema(description = "카테고리별 점수 상세 정보")
    public static class CategoryScoreDetail {
        
        @Schema(description = "카테고리명", example = "입지조건")
        private String categoryName;
        
        @Schema(description = "카테고리 총점수 (0.0 ~ 1.0)")
        private double totalScore;
        
        @Schema(description = "세부 점수 목록")
        private List<TypeScoreDetail> typeScores;
    }
    
    /**
     * 통계 유형별 점수 상세 정보
     */
    @Data
    @Builder
    @Schema(description = "통계 유형별 점수 상세 정보")
    public static class TypeScoreDetail {
        
        @Schema(description = "통계 유형명", example = "토지면적")
        private String typeName;
        
        @Schema(description = "점수 (0.0 ~ 1.0)")
        private double score;
    }
}