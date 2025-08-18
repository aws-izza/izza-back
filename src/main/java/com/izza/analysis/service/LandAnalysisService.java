package com.izza.analysis.service;

import com.izza.analysis.vo.LandAnalysisResult;
import com.izza.search.persistent.Land;
import com.izza.search.persistent.LandDao;
import com.izza.search.vo.LandCategoryCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class LandAnalysisService {

    private static final Double BASE_SCORE = 0.5;
    private final LandDao landDao;

    /*
     * 토지 면적 점수 계산
     */
    public BigDecimal calculateLandAreaScore(Long min_land_area, Long max_land_area, Long landId) {

        try {

            Land land = unwrapLandOptional(landDao.findById(landId.toString()), landId);
            BigDecimal land_size = land.getLandArea();

            // 점수 산정
            // base_score + (x - max) / (max - min) * (1 - base_score)
            BigDecimal score = BigDecimal.valueOf(BASE_SCORE)
                    .add(land_size.subtract(BigDecimal.valueOf(max_land_area))
                            .divide(BigDecimal.valueOf(max_land_area - min_land_area))
                            .multiply(BigDecimal.valueOf(1 - BASE_SCORE)));

            // 0~1 범위로 제한
            if (score.compareTo(BigDecimal.ZERO) < 0) {
                score = BigDecimal.ZERO;
            } else if (score.compareTo(BigDecimal.ONE) > 0) {
                score = BigDecimal.ONE;
            }

            log.debug("토지면적 점수 계산 완료. landId: {}, land_size: {}, min_land_size: {}, max_land_size: {}, score: {}",
                    landId, land_size, min_land_area, max_land_area, score);

            return score;

        } catch (Exception e) {
            log.error("변전소 개수 점수 계산 중 오류 발생. landId: {}", landId, e);
            return BigDecimal.ZERO;
        }
    }

    /*
     * 공시지가 점수 계산
     */
    public BigDecimal calculateLandPriceScore(Long min_land_price, Long max_land_price, Long landId) {
        // base_score + ((max_land_price - x) / (max_land_price - min_land_price)) * (1
        // - base_score)

        try {

            Land land = unwrapLandOptional(landDao.findById(landId.toString()), landId);
            BigDecimal land_price = land.getOfficialLandPrice();

            // 점수 산정
            // base_score + (max - x) / (max - min) * (1 - base_score)
            BigDecimal score = BigDecimal.valueOf(BASE_SCORE)
                    .add(BigDecimal.valueOf(max_land_price).subtract(land_price)
                            .divide(BigDecimal.valueOf(max_land_price - min_land_price))
                            .multiply(BigDecimal.valueOf(1 - BASE_SCORE)));

            // 0~1 범위로 제한
            if (score.compareTo(BigDecimal.ZERO) < 0) {
                score = BigDecimal.ZERO;
            } else if (score.compareTo(BigDecimal.ONE) > 0) {
                score = BigDecimal.ONE;
            }

            log.debug("공시지가 점수 계산 완료. landId: {}, land_price: {}, min_land_price: {}, max_land_price: {}, score: {}",
                    landId, land_price, min_land_price, max_land_price, score);

            return score;

        } catch (Exception e) {
            log.error("공시지가 점수 계산 중 오류 발생. landId: {}", landId, e);
            return BigDecimal.ZERO;
        }
    }

    /*
     * 용도지역 점수 계산
     */
    public BigDecimal calculateLandCategoryScore(Long landId, List<Integer> useDistrictCodes) {
        // if land's useDistrictCode1 or useDistrictCode2 matches any of the supplied
        // codes, return 1, else 0.

        try {

            Land land = unwrapLandOptional(landDao.findById(landId.toString()), landId);

            // 용도지구 코드 매칭 확인
            boolean matches = useDistrictCodes.contains(land.getUseDistrict1().getCode()) ||
                    useDistrictCodes.contains(land.getUseDistrict2().getCode());
            BigDecimal score = matches ? BigDecimal.ONE : BigDecimal.ZERO;

            log.debug(
                    "용도지역 점수 계산 완료. landId: {}, useDistrictCode1: {}, useDistrictCode2: {}, targetCodes: {}, matches: {}, score: {}",
                    landId, land.getUseDistrict1().getCode(), land.getUseDistrict2().getCode(),
                    useDistrictCodes, matches, score);

            return score;

        } catch (Exception e) {
            log.error("용도지역 점수 계산 중 오류 발생. landId: {}", landId, e);
            return BigDecimal.ZERO;
        }
    }

    /**
     * 토지의 종합 분석 결과 조회
     */
    public LandAnalysisResult analyzeLand(Long landId, Long minLandArea, Long maxLandArea,
            Long minLandPrice, Long maxLandPrice,
            List<Integer> useDistrictCodes) {
        try {
            // 토지 정보 조회
            Land land = unwrapLandOptional(landDao.findById(landId.toString()), landId);

            // 각 점수 계산
            BigDecimal landAreaScore = calculateLandAreaScore(minLandArea, maxLandArea, landId);
            BigDecimal landPriceScore = calculateLandPriceScore(minLandPrice, maxLandPrice, landId);
            BigDecimal landCategoryScore = calculateLandCategoryScore(landId, useDistrictCodes);

            // 용도지역 코드 결정 (useDistrict1이 우선, 없으면 useDistrict2)
            LandCategoryCode landCategoryCode = land.getUseDistrict1() != null
                    ? LandCategoryCode.fromCode(land.getUseDistrict1().getCode())
                    : LandCategoryCode.fromCode(land.getUseDistrict2().getCode());

            return LandAnalysisResult.builder()
                    .landId(landId)
                    .landArea(land.getLandArea())
                    .landAreaScore(landAreaScore)
                    .landPrice(land.getOfficialLandPrice())
                    .landPriceScore(landPriceScore)
                    .landCategoryCode(landCategoryCode)
                    .landCategoryScore(landCategoryScore)
                    .build();

        } catch (Exception e) {
            log.error("토지 분석 중 오류 발생. landId: {}", landId, e);
            throw new RuntimeException("토지 분석 실패", e);
        }
    }

    /**
     * 토지 통계 정보 조회
     */
    public Map<String, Object> getLandStatistics() {
        Map<String, Object> statistics = new HashMap<>();

        try {
            // 토지 면적 범위 통계
            var landAreaRange = landDao.getLandAreaRange();
            statistics.put("landAreaMin", landAreaRange.min());
            statistics.put("landAreaMax", landAreaRange.max());
            statistics.put("landAreaRange", landAreaRange.max() - landAreaRange.min());

            // 공시지가 범위 통계
            var landPriceRange = landDao.getOfficialLandPriceRange();
            statistics.put("landPriceMin", landPriceRange.min());
            statistics.put("landPriceMax", landPriceRange.max());
            statistics.put("landPriceRange", landPriceRange.max() - landPriceRange.min());

            // 분석 설정 정보
            statistics.put("baseScore", BASE_SCORE);
            statistics.put("scoreCalculationMethod", "base_score + normalized_value * (1 - base_score)");

            // 분석 메타데이터
            statistics.put("analysisVersion", "1.0");
            statistics.put("lastUpdated", java.time.LocalDateTime.now().toString());

        } catch (Exception e) {
            log.error("토지 통계 조회 중 오류 발생", e);
        }

        return statistics;
    }

    public Land unwrapLandOptional(Optional<Land> optionalLand, Long landId) {
        if (optionalLand.isEmpty()) {
            throw new IllegalArgumentException("Land not found with id: " + landId);
        }

        return optionalLand.get();
    }

}
