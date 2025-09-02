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
import com.izza.search.persistent.dao.LandDao;
import com.izza.search.presentation.dto.request.LandSearchFilterRequest;
import com.izza.search.presentation.dto.response.AreaDetailResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

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
    private final LandDao landDao;
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
        // 1. 토지 목록 조회 
        List<Land> lands = new ArrayList<>();
        Set<Long> starLandIdSet = new HashSet<>();
        
        // 찜 토지가 있으면 우선 조회
        if (request.getStarLandIds() != null && !request.getStarLandIds().isEmpty()) {
            List<Long> starLandIds = request.getStarLandIds().stream()
                    .map(Long::parseLong)
                    .toList();
            starLandIdSet.addAll(starLandIds);
            List<Land> starLands = landDao.findByIds(starLandIds);
            lands.addAll(starLands);
            log.info("찜 토지 조회 완료: {}", starLands.size());
        }
        
        // fullCode 기반 검색 (찜 토지와 중복 제거)
        if (request.getFullCode() != null && !request.getFullCode().isEmpty()) {
            List<Land> searchedLands = searchLandsByCondition(request);
            List<Land> filteredLands = searchedLands.stream()
                    .filter(land -> !starLandIdSet.contains(land.getId()))
                    .toList();
            lands.addAll(filteredLands);
            log.info("검색된 토지 수: {}, fullCode: {}, 중복 제거 후: {}", 
                    searchedLands.size(), request.getFullCode(), filteredLands.size());
        } else if (starLandIdSet.isEmpty()) {
            throw new IllegalArgumentException("fullCode 또는 starLandIds 중 하나는 필수입니다.");
        }

        // 2. 행정구역 상세 정보 조회 (fullCode 5자리 prefix별로 집계)
        Map<String, AreaDetailResponse> areaDetailsMap = getAreaDetailsByPrefixes(lands);

        // 3. 가중치 맵 미리 계산
        Map<AnalysisStatisticsType, WeightedStatisticsRange> statisticsRanges =
                convertToStatisticsRangeMap(request);
        Map<AnalysisStatisticsType, Double> categoryNormalizedWeights =
                weightCalculator.createCategoryNormalizedWeights(statisticsRanges);
        Map<AnalysisStatisticsType, Double> globalNormalizedWeights =
                weightCalculator.createGlobalNormalizedWeights(statisticsRanges);

        // 4. 배치 단위로 토지별 점수 계산
        List<LandScoreItem> landScoreItems = new ArrayList<>();
        final int BATCH_SIZE = 1000;

        for (int i = 0; i < lands.size(); i += BATCH_SIZE) {
            int endIndex = Math.min(i + BATCH_SIZE, lands.size());
            List<Land> landBatch = lands.subList(i, endIndex);

            log.info("배치 처리 중: {}/{} (배치 크기: {})", i + landBatch.size(), lands.size(), landBatch.size());

            // 배치 단위로 전력 인프라 정보 조회
            List<Long> landIds = landBatch.stream().map(Land::getId).toList();
            List<LandPowerInfrastructureSummary> powerInfraSummaries =
                    powerInfrastructureDao.findByLandIds(landIds);

            // landId를 키로 하는 Map으로 변환 (빠른 조회를 위해)
            Map<Long, LandPowerInfrastructureSummary> powerInfraMap = powerInfraSummaries.stream()
                    .collect(Collectors.toMap(
                            LandPowerInfrastructureSummary::getLandId,
                            summary -> summary));

            // 배치 내 각 토지별 점수 계산
            for (Land land : landBatch) {
                LandPowerInfrastructureSummary powerInfraSummary = powerInfraMap.get(land.getId());

                // 토지에 해당하는 행정구역 정보 조회 (fullCode 5자리 prefix 기준)
                String landFullCode = land.getBeopjungDongCode();
                String prefix5 = landFullCode != null && landFullCode.length() >= 5 
                    ? landFullCode.substring(0, 5) 
                    : landFullCode;
                AreaDetailResponse areaDetails = areaDetailsMap.get(prefix5);

                // LandAnalysisData 구성
                LandAnalysisData analysisData = LandAnalysisData.builder()
                        .land(land)
                        .electricityCostInfo(areaDetails != null ? areaDetails.electricityCostInfo() : null)
                        .emergencyTextInfo(areaDetails != null ? areaDetails.emergencyTextInfo() : null)
                        .populationInfo(areaDetails != null ? areaDetails.populationInfo() : null)
                        .substationCount(powerInfraSummary != null ? powerInfraSummary.getSubstationCount() : 0)
                        .transmissionTowerCount(powerInfraSummary != null ? powerInfraSummary.getTransmissionTowerCount() : 0)
                        .transmissionLineCount(powerInfraSummary != null ? powerInfraSummary.getTransmissionLineCount() : 0)
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
                boolean isStarred = starLandIdSet.contains(land.getId());
                LandScoreItem item = LandScoreItem.builder()
                        .landId(land.getId())
                        .address(land.getAddress())
                        .landArea(land.getLandArea())
                        .officialLandPrice(land.getOfficialLandPrice())
                        .totalScore(totalScore)
                        .categoryScores(convertToCategoryScoreDetails(scoreResults))
                        .globalScores(convertToGlobalScoreDetails(scoreResults))
                        .isStarred(isStarred)
                        .build();

                landScoreItems.add(item);
            }
        }

        // 5. 점수 내림차순 정렬 및 순위 설정
        landScoreItems.sort((a, b) -> Double.compare(b.getTotalScore(), a.getTotalScore()));
        for (int i = 0; i < landScoreItems.size(); i++) {
            landScoreItems.get(i).setRank(i + 1);
        }
        
        // 6. 찜토지 목록과 상위 20위 목록 분리
        List<LandScoreItem> starredLands = landScoreItems.stream()
            .filter(LandScoreItem::isStarred)
            .toList();
            
        List<LandScoreItem> top20Lands = landScoreItems.size() > 20 
            ? landScoreItems.subList(0, 20) 
            : landScoreItems;

        // 7. 응답 객체 구성
        return LandScoreRankingResponse.builder()
                .starredLands(starredLands)
                .topRankedLands(top20Lands)
                .build();
    }


    /**
     * 검색 조건으로 토지 목록 조회
     */
    private List<Land> searchLandsByCondition(LandAnalysisRequest request) {
        // TODO: MSA 구조 변경 시 다른 도메인(search)과의 통신을 위해 인터페이스로 분리 필요
        long landAreaMin;
        long landAreaMax;
        long officialLandPriceMin;
        long officialLandPriceMax;

        if (request.getLandAreaRange() != null) {
            landAreaMin = request.getLandAreaRange().min();
            landAreaMax = request.getLandAreaRange().max();
        } else {
            AnalysisRangeDto landAreaRange = landDataRangeAdapter.getLandAreaRange();
            landAreaMin = landAreaRange.min();
            landAreaMax = landAreaRange.max();
        }

        if (request.getLandPriceRange() != null) {
            officialLandPriceMin = request.getLandPriceRange().min();
            officialLandPriceMax = request.getLandPriceRange().max();
        } else {
            AnalysisRangeDto landPriceRange = landDataRangeAdapter.getLandPriceRange();
            officialLandPriceMin = landPriceRange.min();
            officialLandPriceMax = landPriceRange.max();
        }

        // LandSearchFilterRequest 구성 (null 체크 없이 직접 전달)
        LandSearchFilterRequest filterRequest = new LandSearchFilterRequest(
                landAreaMin,
                landAreaMax,
                officialLandPriceMin,
                officialLandPriceMax,
                request.getTargetUseDistrictCodes());

        // MapSearchService의 findLandsByFullCodeAndFilter 활용
        List<Land> lands = mapSearchService.findLandsByFullCodeAndFilter(request.getFullCode(), filterRequest);

        log.info("토지 검색 완료. fullCode: {}, 조회된 토지 수: {}", request.getFullCode(), lands.size());

        return lands;
    }

    /**
     * 토지 목록에서 fullCode 5자리 prefix를 추출하여 행정구역 상세 정보 조회
     */
    private Map<String, AreaDetailResponse> getAreaDetailsByPrefixes(List<Land> lands) {
        // fullCode 5자리 prefix 추출
        Set<String> prefix5Set = lands.stream()
                .map(Land::getBeopjungDongCode)
                .filter(Objects::nonNull)
                .filter(code -> code.length() >= 5)
                .map(code -> code.substring(0, 5))
                .collect(Collectors.toSet());

        Map<String, AreaDetailResponse> areaDetailsMap = new HashMap<>();
        
        // 각 prefix별로 행정구역 상세 정보 조회
        for (String prefix5 : prefix5Set) {
            try {
                AreaDetailResponse areaDetails = mapSearchService.getAreaDetailsByFullCode(prefix5);
                areaDetailsMap.put(prefix5, areaDetails);
                log.debug("행정구역 정보 조회 완료: prefix5={}", prefix5);
            } catch (Exception e) {
                log.warn("행정구역 정보 조회 실패: prefix5={}, error={}", prefix5, e.getMessage());
            }
        }
        
        log.info("행정구역 정보 조회 완료. 총 {}개 prefix", areaDetailsMap.size());
        return areaDetailsMap;
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
                if(scoreResult == null) {
                    continue;
                }
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

        // 필수 필드들은 null이 아닐 때만 추가
        if (request.getLandAreaRange() != null) {
            map.put(AnalysisStatisticsType.LAND_AREA, request.getLandAreaRange());
        }
        if (request.getLandPriceRange() != null) {
            map.put(AnalysisStatisticsType.OFFICIAL_LAND_PRICE, request.getLandPriceRange());
        }

        if (request.getElectricityCostRange() != null) {
            map.put(AnalysisStatisticsType.ELECTRICITY_COST,
                    convertToWeightedRange(request.getElectricityCostRange(), landDataRangeAdapter.getElectricBillRange()));
        }

        // 선택 지표들은 null이 아닐 때만 추가 (사용자 요청에 없을 경우 어댑터를 통해 기본값 조회)
        if (request.getPopulationDensityRange() != null) {
            map.put(AnalysisStatisticsType.POPULATION_DENSITY, request.getPopulationDensityRange());
        }

        if (request.getSubstationCountRange() != null) {
            map.put(AnalysisStatisticsType.SUBSTATION_COUNT,
                    convertToWeightedRange(request.getSubstationCountRange(), landDataRangeAdapter.getSubstationCountRange()));
        }
        if (request.getTransmissionTowerCountRange() != null) {
            map.put(AnalysisStatisticsType.TRANSMISSION_TOWER_COUNT,
                    convertToWeightedRange(request.getTransmissionTowerCountRange(), landDataRangeAdapter.getTransmissionTowerCountRange()));
        }
        if (request.getTransmissionLineCountRange() != null) {
            map.put(AnalysisStatisticsType.TRANSMISSION_LINE_COUNT,
                    convertToWeightedRange(request.getTransmissionLineCountRange(), landDataRangeAdapter.getTransmissionLineCountRange()));
        }
        if (request.getDisasterCountRange() != null) {
            map.put(AnalysisStatisticsType.DISASTER_COUNT,
                    convertToWeightedRange(request.getDisasterCountRange(), landDataRangeAdapter.getDisasterCountRange()));
        }

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