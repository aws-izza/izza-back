package com.izza.analysis.service;

import com.izza.analysis.presentation.dto.AnalysisRangeDto;
import com.izza.analysis.presentation.dto.request.LandAnalysisRequest;
import com.izza.analysis.presentation.dto.response.LandScoreItem;
import com.izza.analysis.presentation.dto.response.LandScoreRankingResponse;
import com.izza.analysis.persistent.dao.LandPowerInfrastructureSummaryDao;
import com.izza.analysis.persistent.model.LandPowerInfrastructureSummary;
import com.izza.analysis.service.adapter.LandDataRangeAdapter;
import com.izza.analysis.service.dto.LandAnalysisData;
import com.izza.analysis.service.dto.ScoreResult;
import com.izza.analysis.vo.AnalysisStatisticsType;
import com.izza.analysis.vo.IndustryType;
import com.izza.analysis.vo.WeightedStatisticsRange;
import com.izza.search.persistent.model.Land;
import com.izza.search.service.MapSearchService;
import com.izza.search.presentation.dto.request.LandSearchFilterRequest;
import com.izza.search.presentation.dto.response.AreaDetailResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 토지 분석 서비스
 * 토지 분석 요청을 받아서 필요한 데이터를 조회하고 점수를 계산
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LandAnalysisService {

    private final MapSearchService mapSearchService;
    private final LandPowerInfrastructureSummaryDao powerInfrastructureDao;
    private final List<ScoreCalculator> scoreCalculators;
    private final WeightCalculator weightCalculator;
    private final LandDataRangeAdapter landDataRangeAdapter;

    /**
     * 토지 분석을 수행 (fullCode 기반 다중 토지 분석만 지원)
     *
     * @param request 토지 분석 요청
     * @return 토지 점수 순위 응답
     */
    public LandScoreRankingResponse analyzeLand(LandAnalysisRequest request) {
        return analyzeLandRanking(request);
    }


    /**
     * fullCode 기반 토지 점수 순위 분석
     * fullCode, 면적/가격 범위, 용도지역 조건으로 토지 검색 후 각 토지별 점수 계산
     *
     * @param request 토지 분석 요청
     * @return 토지 점수 순위 응답
     */
    public LandScoreRankingResponse analyzeLandRanking(LandAnalysisRequest request) {
        String fullCode = request.getFullCode();
        if (fullCode == null || fullCode.isEmpty()) {
            throw new IllegalArgumentException("fullCode는 필수 파라미터입니다.");
        }

        // 1. 검색 조건으로 토지 목록 조회
        List<Land> lands = searchLandsByCondition(request);
        log.info("검색된 토지 수: {}, fullCode: {}", lands.size(), fullCode);

        // 2. 행정구역 상세 정보 조회 (한 번만)
        AreaDetailResponse areaDetails = mapSearchService.getAreaDetailsByFullCode(fullCode);

        // 3. 가중치 맵 미리 계산
        Map<AnalysisStatisticsType, WeightedStatisticsRange> statisticsRanges =
                convertToStatisticsRangeMap(request);
        Map<AnalysisStatisticsType, Double> categoryNormalizedWeights =
                weightCalculator.createCategoryNormalizedWeights(statisticsRanges);
        Map<AnalysisStatisticsType, Double> globalNormalizedWeights =
                weightCalculator.createGlobalNormalizedWeights(statisticsRanges);

        // 4. 각 토지별 점수 계산
        List<LandScoreItem> landScoreItems = new ArrayList<>();

        for (Land land : lands) {
            // 전력 인프라 정보 조회
            LandPowerInfrastructureSummary powerInfraSummary = getPowerInfrastructureData(land.getId());

            // LandAnalysisData 구성
            LandAnalysisData analysisData = LandAnalysisData.builder()
                    .land(land)
                    .electricityCostInfo(areaDetails.electricityCostInfo())
                    .emergencyTextInfo(areaDetails.emergencyTextInfo())
                    .populationInfo(areaDetails.populationInfo())
                    .substationCount(powerInfraSummary.getSubstationCount())
                    .transmissionTowerCount(powerInfraSummary.getTransmissionTowerCount())
                    .transmissionLineCount(powerInfraSummary.getTransmissionLineCount())
                    .statisticsRanges(statisticsRanges)
                    .categoryNormalizedWeights(categoryNormalizedWeights)
                    .globalNormalizedWeights(globalNormalizedWeights)
                    .targetUseDistrictCodes(request.getTargetUseDistrictCodes())
                    .industryType(IndustryType.fromCode(request.getIndustryType()))
                    .build();

            // 점수 계산
            Map<AnalysisStatisticsType, ScoreResult> scoreResults = calculateScores(analysisData);
            double totalScore = calculateTotalScoreFromResults(scoreResults, analysisData);

            // LandScoreItem 생성
            LandScoreItem item = LandScoreItem.builder()
                    .landId(land.getId())
                    .address(land.getAddress())
                    .landArea(land.getLandArea())
                    .officialLandPrice(land.getOfficialLandPrice())
                    .totalScore(totalScore)
                    .categoryScores(convertToCategoryScoreDetails(scoreResults))
                    .globalScores(convertToGlobalScoreDetails(scoreResults))
                    .build();

            landScoreItems.add(item);
        }

        // 5. 점수 내림차순 정렬
        landScoreItems.sort((a, b) -> Double.compare(b.getTotalScore(), a.getTotalScore()));

        // 6. 응답 객체 구성
        return LandScoreRankingResponse.builder()
                .landScores(landScoreItems)
                .build();
    }


    /**
     * 검색 조건으로 토지 목록 조회
     */
    private List<Land> searchLandsByCondition(LandAnalysisRequest request) {
        // TODO: MSA 구조 변경 시 다른 도메인(search)과의 통신을 위해 인터페이스로 분리 필요

        // LandSearchFilterRequest 구성 (null 체크 없이 직접 전달)
        LandSearchFilterRequest filterRequest = new LandSearchFilterRequest(
                request.getLandAreaRange().min(),
                request.getLandAreaRange().max(),
                request.getLandPriceRange().min(),
                request.getLandPriceRange().max(),
                request.getTargetUseDistrictCodes());

        // MapSearchService의 findLandsByFullCodeAndFilter 활용
        List<Land> lands = mapSearchService.findLandsByFullCodeAndFilter(request.getFullCode(), filterRequest);

        log.info("토지 검색 완료. fullCode: {}, 조회된 토지 수: {}", request.getFullCode(), lands.size());

        return lands;
    }

    /**
     * 점수 계산 (모든 ScoreCalculator 구현체 순회하여 계산)
     */
    private Map<AnalysisStatisticsType, ScoreResult> calculateScores(LandAnalysisData analysisData) {
        Map<AnalysisStatisticsType, ScoreResult> scoreResults = new HashMap<>();

        // 모든 ScoreCalculator 구현체를 순회하면서 점수 계산
        for (ScoreCalculator calculator : scoreCalculators) {
            try {
                ScoreResult scoreResult = calculator.calculateScore(analysisData);
                AnalysisStatisticsType statisticsType = scoreResult.getStatisticsType();
                scoreResults.put(statisticsType, scoreResult);

                log.debug("점수 계산 완료. calculator: {}, type: {}, original: {}, categoryNormalized: {}, globalNormalized: {}",
                        calculator.getCalculatorName(),
                        statisticsType,
                        scoreResult.getOriginalScore(),
                        scoreResult.getCategoryNormalizedScore(),
                        scoreResult.getGlobalNormalizedScore());

            } catch (Exception e) {
                log.error("점수 계산 중 오류 발생. calculator: {}, landId: {}",
                        calculator.getCalculatorName(), analysisData.getLand().getId(), e);
                // 오류 발생 시 해당 계산기 점수는 0으로 설정
                AnalysisStatisticsType statisticsType = calculator.getStatisticsType();
                ScoreResult errorResult = ScoreResult.builder()
                        .statisticsType(statisticsType)
                        .originalScore(0.0)
                        .categoryNormalizedScore(0.0)
                        .globalNormalizedScore(0.0)
                        .build();
                scoreResults.put(statisticsType, errorResult);
            }
        }

        log.debug("전체 점수 계산 완료. landId: {}, scoreResults count: {}",
                analysisData.getLand().getId(), scoreResults.size());

        return scoreResults;
    }

    /**
     * ScoreResult에서 총합 점수 계산 (미리 계산된 가중치 적용)
     */
    private double calculateTotalScoreFromResults(Map<AnalysisStatisticsType, ScoreResult> scoreResults, LandAnalysisData analysisData) {
        if (scoreResults.isEmpty()) {
            return 0.0;
        }

        // 원본 점수 추출하여 최종 점수 계산
        Map<AnalysisStatisticsType, Double> originalScores = new HashMap<>();
        for (Map.Entry<AnalysisStatisticsType, ScoreResult> entry : scoreResults.entrySet()) {
            originalScores.put(entry.getKey(), entry.getValue().getOriginalScore());
        }

        return weightCalculator.calculateFinalWeightedScore(originalScores, analysisData.getGlobalNormalizedWeights());
    }


    /**
     * 전력 인프라 정보 조회
     */
    private LandPowerInfrastructureSummary getPowerInfrastructureData(Long landId) {
        return powerInfrastructureDao.findByLandId(landId).orElse(null);
    }

    /**
     * Presentation DTO의 개별 필드들을 Map으로 변환
     */
    private Map<AnalysisStatisticsType, WeightedStatisticsRange> convertToStatisticsRangeMap(
            LandAnalysisRequest request) {
        Map<AnalysisStatisticsType, WeightedStatisticsRange> map = new HashMap<>();

        map.put(AnalysisStatisticsType.LAND_AREA, request.getLandAreaRange());
        map.put(AnalysisStatisticsType.OFFICIAL_LAND_PRICE, request.getLandPriceRange());
        map.put(AnalysisStatisticsType.ELECTRICITY_COST, request.getElectricityCostRange());


        // 선택 지표들은 사용자 요청에 없을 경우 어댑터를 통해 기본값 조회
        map.put(AnalysisStatisticsType.SUBSTATION_COUNT,
                convertToWeightedRange(request.getSubstationCountRange(), landDataRangeAdapter.getSubstationCountRange()));
        map.put(AnalysisStatisticsType.TRANSMISSION_TOWER_COUNT,
                convertToWeightedRange(request.getTransmissionTowerCountRange(), landDataRangeAdapter.getTransmissionTowerCountRange()));
        map.put(AnalysisStatisticsType.TRANSMISSION_LINE_COUNT,
                convertToWeightedRange(request.getTransmissionLineCountRange(), landDataRangeAdapter.getTransmissionLineCountRange()));
        map.put(AnalysisStatisticsType.DISASTER_COUNT,
                convertToWeightedRange(request.getDisasterCountRange(), landDataRangeAdapter.getDisasterCountRange()));

        return map;
    }


    private WeightedStatisticsRange convertToWeightedRange(WeightedStatisticsRange origin, AnalysisRangeDto rangeDto) {
        return WeightedStatisticsRange.builder()
                .min(rangeDto.min())
                .max(rangeDto.max())
                .weight(origin.weight())
                .build();
    }

    /**
     * ScoreResult를 사용하여 CategoryScoreDetail 목록으로 변환
     * 카테고리 내 가중치가 적용된 점수를 사용 (categoryNormalizedScore)
     */
    private List<LandScoreItem.CategoryScoreDetail> convertToCategoryScoreDetails(
            Map<AnalysisStatisticsType, ScoreResult> scoreResults) {

        Map<AnalysisStatisticsType.AnalysisCategory, List<AnalysisStatisticsType>> categoryGroups = new HashMap<>();
        Map<AnalysisStatisticsType.AnalysisCategory, Double> categoryTotalScores = new HashMap<>();

        // 카테고리별로 그룹화 및 총점 계산 (카테고리 정규화 점수 사용)
        for (Map.Entry<AnalysisStatisticsType, ScoreResult> entry : scoreResults.entrySet()) {
            AnalysisStatisticsType type = entry.getKey();
            ScoreResult scoreResult = entry.getValue();
            Double categoryNormalizedScore = scoreResult.getCategoryNormalizedScore();
            AnalysisStatisticsType.AnalysisCategory category = type.getCategory();

            categoryGroups.computeIfAbsent(category, k -> new ArrayList<>()).add(type);
            categoryTotalScores.merge(category, categoryNormalizedScore, Double::sum);
        }

        // CategoryScoreDetail 목록 생성
        List<LandScoreItem.CategoryScoreDetail> categoryScoreDetails = new ArrayList<>();

        for (AnalysisStatisticsType.AnalysisCategory category : AnalysisStatisticsType.AnalysisCategory.values()) {
            List<AnalysisStatisticsType> typesInCategory = categoryGroups.get(category);
            if (typesInCategory == null || typesInCategory.isEmpty()) {
                continue;
            }

            // 카테고리별 총점 (가중치 적용된 점수 합계)
            double categoryTotalScore = categoryTotalScores.get(category);

            // TypeScoreDetail 목록 생성 (카테고리 정규화 점수 사용)
            List<LandScoreItem.TypeScoreDetail> typeScoreDetails = new ArrayList<>();
            for (AnalysisStatisticsType type : typesInCategory) {
                ScoreResult scoreResult = scoreResults.get(type);
                if (scoreResult != null) {
                    typeScoreDetails.add(LandScoreItem.TypeScoreDetail.builder()
                            .typeName(type.getDisplayName())
                            .score(scoreResult.getCategoryNormalizedScore()) // 카테고리 내 가중치 적용 점수
                            .build());
                }
            }

            // CategoryScoreDetail 생성
            categoryScoreDetails.add(LandScoreItem.CategoryScoreDetail.builder()
                    .categoryName(category.getDisplayName())
                    .totalScore(categoryTotalScore) // 카테고리별 가중치 적용 총점
                    .typeScores(typeScoreDetails)
                    .build());
        }

        return categoryScoreDetails;
    }


    /**
     * ScoreResult를 사용하여 전역 정규화 점수를 CategoryScoreDetail 목록으로 변환
     * 전체 점수 대비 각 카테고리/타입의 기여도를 계산
     */
    private List<LandScoreItem.CategoryScoreDetail> convertToGlobalScoreDetails(
            Map<AnalysisStatisticsType, ScoreResult> scoreResults) {

        Map<AnalysisStatisticsType.AnalysisCategory, List<AnalysisStatisticsType>> categoryGroups = new HashMap<>();
        Map<AnalysisStatisticsType.AnalysisCategory, Double> categoryTotalScores = new HashMap<>();

        // 카테고리별로 그룹화 및 총점 계산 (전역 정규화된 점수 사용)
        for (Map.Entry<AnalysisStatisticsType, ScoreResult> entry : scoreResults.entrySet()) {
            AnalysisStatisticsType type = entry.getKey();
            ScoreResult scoreResult = entry.getValue();
            Double globalScore = scoreResult.getGlobalNormalizedScore();
            AnalysisStatisticsType.AnalysisCategory category = type.getCategory();

            categoryGroups.computeIfAbsent(category, k -> new ArrayList<>()).add(type);
            categoryTotalScores.merge(category, globalScore, Double::sum);
        }

        // CategoryScoreDetail 목록 생성
        List<LandScoreItem.CategoryScoreDetail> globalCategoryScoreDetails = new ArrayList<>();

        for (AnalysisStatisticsType.AnalysisCategory category : AnalysisStatisticsType.AnalysisCategory.values()) {
            List<AnalysisStatisticsType> typesInCategory = categoryGroups.get(category);
            if (typesInCategory == null || typesInCategory.isEmpty()) {
                continue;
            }

            // 카테고리별 전역 점수 합계
            double categoryGlobalScore = categoryTotalScores.get(category);

            // TypeScoreDetail 목록 생성 (전역 정규화 점수 사용)
            List<LandScoreItem.TypeScoreDetail> typeScoreDetails = new ArrayList<>();
            for (AnalysisStatisticsType type : typesInCategory) {
                ScoreResult scoreResult = scoreResults.get(type);
                if (scoreResult != null) {
                    typeScoreDetails.add(LandScoreItem.TypeScoreDetail.builder()
                            .typeName(type.getDisplayName())
                            .score(scoreResult.getGlobalNormalizedScore())  // 전역 정규화된 점수 (가중치 적용된 실제 기여분)
                            .build());
                }
            }

            // CategoryScoreDetail 생성
            globalCategoryScoreDetails.add(LandScoreItem.CategoryScoreDetail.builder()
                    .categoryName(category.getDisplayName())
                    .totalScore(categoryGlobalScore)  // 카테고리별 전역 점수 합계
                    .typeScores(typeScoreDetails)
                    .build());
        }

        return globalCategoryScoreDetails;
    }
}