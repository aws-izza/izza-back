package com.izza.analysis.service;

import com.izza.search.persistent.Land;
import com.izza.search.persistent.LandDao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.math.BigDecimal;
import java.util.List;
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

            Land land = unwrapLandOptional(landDao.findById(landId), landId);
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

            Land land = unwrapLandOptional(landDao.findById(landId), landId);
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

            Land land = unwrapLandOptional(landDao.findById(landId), landId);

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

    public Land unwrapLandOptional(Optional<Land> optionalLand, Long landId) {
        if (optionalLand.isEmpty()) {
            throw new IllegalArgumentException("Land not found with id: " + landId);
        }

        return optionalLand.get();
    }

}
