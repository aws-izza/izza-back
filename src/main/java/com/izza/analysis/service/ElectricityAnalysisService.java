package com.izza.analysis.service;

import com.izza.search.persistent.dao.ElectricityCostDao;
import com.izza.search.persistent.dao.LandDao;
import com.izza.search.persistent.model.ElectricityCost;
import com.izza.search.persistent.model.Land;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ElectricityAnalysisService {

    private static final Double BASE_SCORE = 0.5;
    private final ElectricityCostDao electricityCostDao;
    private final LandDao landDao;

    /**
     * 전기 요금 점수 계산
     * 공식: base_score + ((max_price - x) / (max_price - min_price)) × (1 -
     * base_score)
     * 전기 요금이 낮을수록 높은 점수를 받음
     */
    public BigDecimal calculateElectricityCostScore(BigDecimal minUnitCost, BigDecimal maxUnitCost, String landId) {
        try {

            // 파라미터로 전달받은 최소/최대 전기 요금 사용
            if (minUnitCost == null || maxUnitCost == null) {
                log.warn("전기 요금 최소/최대값이 null입니다. landId: {}", landId);
                return BigDecimal.valueOf(BASE_SCORE);
            }

            // 최소값과 최대값이 같은 경우 (데이터가 하나뿐인 경우)
            if (maxUnitCost.compareTo(minUnitCost) == 0) {
                log.debug("전기 요금 최소값과 최대값이 동일합니다. 기본 점수 반환. landId: {}", landId);
                return BigDecimal.valueOf(BASE_SCORE);
            }

            // 토지 정보 조회
            Land land = unwrapLandOptional(landDao.findById(landId), landId);
            String fullCode = land.getBeopjungDongCode();

            // 해당 지역의 전기 요금 정보 조회
            String electricityFullCode = convertToElectricityFullCode(fullCode);
            ElectricityCost electricityCost = unwrapElectricityCostOptional(electricityCostDao.findByFullCode(electricityFullCode), landId, fullCode);
            BigDecimal unitCost = electricityCost.getUnitCost();

            if (unitCost == null) {
                log.warn("전기 요금 단위 비용이 null입니다. landId: {}, fullCode: {}", landId, fullCode);
                return BigDecimal.valueOf(BASE_SCORE);
            }

            // 점수 산정: base_score + ((max_price - x) / (max_price - min_price)) × (1 -
            // base_score)
            BigDecimal score = BigDecimal.valueOf(BASE_SCORE)
                    .add(maxUnitCost.subtract(unitCost)
                            .divide(maxUnitCost.subtract(minUnitCost), 10, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(1 - BASE_SCORE)));

            // 0~1 범위로 제한
            if (score.compareTo(BigDecimal.ZERO) < 0) {
                score = BigDecimal.ZERO;
            } else if (score.compareTo(BigDecimal.ONE) > 0) {
                score = BigDecimal.ONE;
            }

            log.debug(
                    "전기 요금 점수 계산 완료. landId: {}, fullCode: {}, electricityFullCode: {}, unitCost: {}, minUnitCost: {}, maxUnitCost: {}, score: {}",
                    landId, fullCode, electricityFullCode, unitCost, minUnitCost, maxUnitCost, score);

            return score;

        } catch (Exception e) {
            log.error("전기 요금 점수 계산 중 오류 발생. landId: {}", landId, e);
            return BigDecimal.ZERO;
        }
    }

    /**
     * 토지의 법정동 코드를 전기 요금 테이블 형식으로 변환
     * 전기 요금 테이블은 첫 5자리 + "00000" 형태로 저장됨
     */
    private String convertToElectricityFullCode(String fullCode) {
        if (fullCode == null || fullCode.length() < 5) {
            return fullCode;
        }
        return fullCode.substring(0, 5) + "00000";
    }

    /**
     * Optional<Land>를 Land로 변환하는 유틸리티 메서드
     */
    private Land unwrapLandOptional(Optional<Land> optionalLand, String landId) {
        if (optionalLand.isEmpty()) {
            throw new IllegalArgumentException("Land not found with id: " + landId);
        }
        return optionalLand.get();
    }

    /**
     * Optional<ElectricityCost>를 ElectricityCost로 변환하는 유틸리티 메서드
     */
    private ElectricityCost unwrapElectricityCostOptional(Optional<ElectricityCost> optionalElectricityCost,
                                                          String landId, String fullCode) {
        if (optionalElectricityCost.isEmpty()) {
            log.warn("전기 요금 정보를 찾을 수 없습니다. landId: {}, fullCode: {}", landId, fullCode);
            throw new IllegalArgumentException(
                    "ElectricityCost not found for landId: " + landId + ", fullCode: " + fullCode);
        }
        return optionalElectricityCost.get();
    }
}