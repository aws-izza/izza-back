package com.izza.analysis.service;

import com.izza.analysis.service.dto.LandAnalysisData;
import com.izza.analysis.service.dto.ScoreResult;
import com.izza.analysis.vo.AnalysisStatisticsType;
import com.izza.analysis.vo.IndustryType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 인구밀도 점수 계산기
 * README.md 기준: max(0, 1 - abs(x - 기준값) / 허용편차)
 * 업종별 기준값에 가까울수록 높은 점수를 받는 방식
 */
@Service
@Slf4j
public class PopulationDensityScoreCalculator implements ScoreCalculator {

    @Override
    public String getCalculatorName() {
        return "PopulationDensityScoreCalculator";
    }

    @Override
    public AnalysisStatisticsType getStatisticsType() {
        return AnalysisStatisticsType.POPULATION_DENSITY;
    }

    @Override
    public ScoreResult calculateScore(LandAnalysisData data) {
        // 1. 실제 값 가져오기 (인구밀도)
        double actualValue = data.getPopulationInfo().getMiddleAgedPopulation();

        // 2. 업종 타입에 따른 기준값과 허용편차 결정
        IndustryType industryType = determineIndustryType(data);
        if (industryType == null) {
            log.warn("업종 타입이 지정되지 않아 제조업 기준으로 계산합니다.");
            industryType = IndustryType.MANUFACTURING; // 기본값
        }

        double standardDensity = industryType.getStandardDensity();
        double allowedDeviation = industryType.getAllowedDeviation();

        // 3. 원본 점수 계산
        double originalScore = calculateDeviationBasedScore(actualValue, standardDensity, allowedDeviation);

        // 4. 가중치 적용
        AnalysisStatisticsType statisticsType = getStatisticsType();
        Double categoryWeight = data.getCategoryNormalizedWeights().get(statisticsType);
        Double globalWeight = data.getGlobalNormalizedWeights().get(statisticsType);

        double categoryNormalizedScore = categoryWeight != null ?
                originalScore * (categoryWeight / 100) : originalScore;
        double globalNormalizedScore = globalWeight != null ?
                originalScore * (globalWeight / 100) : originalScore;

        log.debug("인구밀도 점수 계산 완료 - 실제값: {}, 업종: {}, 기준값: {}, 허용편차: {}, 원본점수: {}",
                actualValue, industryType.getDisplayName(), standardDensity, allowedDeviation, originalScore);

        return ScoreResult.builder()
                .statisticsType(statisticsType)
                .originalScore(originalScore)
                .categoryNormalizedScore(categoryNormalizedScore)
                .globalNormalizedScore(globalNormalizedScore)
                .build();
    }

    /**
     * 인구밀도 기반 점수 계산
     * 허용범위(기준값±허용편차)일 때 0.5점, 인구가 많을수록 +점수, 적을수록 -점수
     */
    private double calculateDeviationBasedScore(double actualValue, double standardDensity, double allowedDeviation) {
        if (allowedDeviation == 0) {
            return actualValue == standardDensity ? 0.5 : 0.0;
        }

        double lowerBound = standardDensity - allowedDeviation;
        double upperBound = standardDensity + allowedDeviation;

        // 허용 범위 내에 있으면 0.5점
        if (actualValue >= lowerBound && actualValue <= upperBound) {
            return 0.5;
        }

        // 허용 범위보다 인구가 많은 경우 (상한선 초과)
        if (actualValue > upperBound) {
            double excessRatio = (actualValue - upperBound) / allowedDeviation;
            return Math.min(1.0, 0.5 + (excessRatio * 0.5));
        }

        // 허용 범위보다 인구가 적은 경우 (하한선 미만)
        double deficitRatio = (lowerBound - actualValue) / allowedDeviation;
        return Math.max(0.0, 0.5 - (deficitRatio * 0.5));
    }

    /**
     * LandAnalysisData에서 업종 타입을 추출
     */
    private IndustryType determineIndustryType(LandAnalysisData data) {
        return data.getIndustryType();
    }
}