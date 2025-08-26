package com.izza.analysis.service;

import com.izza.analysis.presentation.dto.request.LandAnalysisRequest;
import com.izza.analysis.presentation.dto.response.LandScoreItem;
import com.izza.analysis.presentation.dto.response.LandScoreRankingResponse;
import com.izza.analysis.persistent.dao.LandPowerInfrastructureSummaryDao;
import com.izza.analysis.persistent.model.LandPowerInfrastructureSummary;
import com.izza.analysis.service.dto.LandAnalysisData;
import com.izza.analysis.vo.AnalysisStatisticsType;
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

        // 3. 각 토지별 점수 계산
        List<LandScoreItem> landScoreItems = new ArrayList<>();
        Map<AnalysisStatisticsType, WeightedStatisticsRange> statisticsRanges =
                convertToStatisticsRangeMap(request);

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
                    .targetUseDistrictCodes(request.getTargetUseDistrictCodes())
                    .build();

            // 점수 계산
            Map<AnalysisStatisticsType, Double> scores = calculateScores(analysisData);
            double totalScore = calculateTotalScore(scores);

            // LandScoreItem 생성
            LandScoreItem item = LandScoreItem.builder()
                    .landId(land.getId())
                    .address(land.getAddress())
                    .landArea(land.getLandArea())
                    .officialLandPrice(land.getOfficialLandPrice())
                    .totalScore(totalScore)
                    .categoryScores(convertToCategoryScoreDetails(scores))
                    .build();

            landScoreItems.add(item);
        }

        // 4. 점수 내림차순 정렬
        landScoreItems.sort((a, b) -> Double.compare(b.getTotalScore(), a.getTotalScore()));

        // 5. 응답 객체 구성
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
    private Map<AnalysisStatisticsType, Double> calculateScores(LandAnalysisData analysisData) {
        Map<AnalysisStatisticsType, Double> scores = new HashMap<>();

        // 모든 ScoreCalculator 구현체를 순회하면서 점수 계산
        for (ScoreCalculator calculator : scoreCalculators) {
            try {
                double score = calculator.calculateScore(analysisData);
                AnalysisStatisticsType statisticsType = calculator.getStatisticsType();
                scores.put(statisticsType, score);

                log.debug("점수 계산 완료. calculator: {}, type: {}, score: {}",
                        calculator.getCalculatorName(), statisticsType, score);

            } catch (Exception e) {
                log.error("점수 계산 중 오류 발생. calculator: {}, landId: {}",
                        calculator.getCalculatorName(), analysisData.getLand().getId(), e);
                // 오류 발생 시 해당 계산기 점수는 0으로 설정
                scores.put(calculator.getStatisticsType(), 0.0);
            }
        }

        log.debug("전체 점수 계산 완료. landId: {}, scores: {}",
                analysisData.getLand().getId(), scores);

        return scores;
    }

    /**
     * 총합 점수 계산 (가중치 적용)
     */
    private double calculateTotalScore(Map<AnalysisStatisticsType, Double> scores) {
        if (scores.isEmpty()) {
            return 0.0;
        }

        // 단순 평균 계산 (향후 가중치 적용 가능)
        double totalScore = scores.values().stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);

        // 0~1 범위로 제한
        return Math.max(0.0, Math.min(1.0, totalScore));
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
        map.put(AnalysisStatisticsType.LAND_PRICE, request.getLandPriceRange());
        map.put(AnalysisStatisticsType.ELECTRICITY_COST, request.getElectricityCostRange());
        map.put(AnalysisStatisticsType.SUBSTATION_COUNT, request.getSubstationCountRange());
        map.put(AnalysisStatisticsType.TRANSMISSION_TOWER_COUNT, request.getTransmissionTowerCountRange());
        map.put(AnalysisStatisticsType.TRANSMISSION_LINE_COUNT, request.getTransmissionLineCountRange());
        map.put(AnalysisStatisticsType.POPULATION_DENSITY, request.getPopulationDensityRange());
        map.put(AnalysisStatisticsType.DISASTER_COUNT, request.getDisasterCountRange());

        return map;
    }

    /**
     * Map<AnalysisStatisticsType, Double> 점수를 CategoryScoreDetail 목록으로 변환
     */
    private List<LandScoreItem.CategoryScoreDetail> convertToCategoryScoreDetails(
            Map<AnalysisStatisticsType, Double> scores) {

        Map<AnalysisStatisticsType.AnalysisCategory, List<AnalysisStatisticsType>> categoryGroups = new HashMap<>();
        Map<AnalysisStatisticsType.AnalysisCategory, Double> categoryTotalScores = new HashMap<>();

        // 카테고리별로 그룹화 및 총점 계산
        for (Map.Entry<AnalysisStatisticsType, Double> entry : scores.entrySet()) {
            AnalysisStatisticsType type = entry.getKey();
            Double score = entry.getValue();
            AnalysisStatisticsType.AnalysisCategory category = type.getCategory();

            categoryGroups.computeIfAbsent(category, k -> new ArrayList<>()).add(type);
            categoryTotalScores.merge(category, score, Double::sum);
        }

        // CategoryScoreDetail 목록 생성
        List<LandScoreItem.CategoryScoreDetail> categoryScoreDetails = new ArrayList<>();

        for (AnalysisStatisticsType.AnalysisCategory category : AnalysisStatisticsType.AnalysisCategory.values()) {
            List<AnalysisStatisticsType> typesInCategory = categoryGroups.get(category);
            if (typesInCategory == null || typesInCategory.isEmpty()) {
                continue;
            }

            // 카테고리 평균 점수 계산
            double categoryAvgScore = categoryTotalScores.get(category) / typesInCategory.size();

            // TypeScoreDetail 목록 생성
            List<LandScoreItem.TypeScoreDetail> typeScoreDetails = new ArrayList<>();
            for (AnalysisStatisticsType type : typesInCategory) {
                Double score = scores.get(type);
                if (score != null) {
                    typeScoreDetails.add(LandScoreItem.TypeScoreDetail.builder()
                            .typeName(type.getDisplayName())
                            .score(score)
                            .build());
                }
            }

            // CategoryScoreDetail 생성
            categoryScoreDetails.add(LandScoreItem.CategoryScoreDetail.builder()
                    .categoryName(category.getDisplayName())
                    .totalScore(categoryAvgScore)
                    .typeScores(typeScoreDetails)
                    .build());
        }

        return categoryScoreDetails;
    }


}