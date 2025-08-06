package com.izza.analysis.service;

import java.math.BigDecimal;
import com.izza.search.vo.LandCategoryCode;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LandAnalysisResult {

    /**
     * 토지 ID
     */
    private Long landId;

    /**
     * 토지면적
     */
    private BigDecimal landArea;

    /**
     * 토지면적 정규화 점수 (0~1)
     */
    private BigDecimal landAreaScore;

    /**
     * 공시지가
     */
    private BigDecimal landPrice;

    /**
     * 공시지가 정규화 점수 (0~1)
     */
    private BigDecimal landPriceScore;

    /**
     * 용도지역
     */
    private LandCategoryCode landCategoryCode;

    /**
     * 용도지역 정규화 점수
     */
    private BigDecimal landCategoryScore;

    /*
     * 토지 종합 점수 계산
     */

    public BigDecimal getOverallLandScore() {
        if (landAreaScore == null || landPriceScore == null || landCategoryScore == null) {
            return BigDecimal.ZERO;
        }

        // get the average of the three scores and return it as BigDecimal
        return landAreaScore.add(landPriceScore).add(landCategoryScore)
                .divide(BigDecimal.valueOf(3), 2, RoundingMode.HALF_UP);
    }

}
