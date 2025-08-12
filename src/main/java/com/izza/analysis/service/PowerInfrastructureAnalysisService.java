package com.izza.analysis.service;

import com.izza.analysis.persistent.LandPowerInfrastructureProximity;
import com.izza.analysis.persistent.LandPowerInfrastructureProximityDao;
import com.izza.analysis.vo.PowerInfrastructureAnalysisResult;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 전력 인프라 분석 서비스
 * README.md의 정규화 공식을 기반으로 변전소, 송전탑 밀도 점수를 계산
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PowerInfrastructureAnalysisService {

    private final LandPowerInfrastructureProximityDao proximityDao;

    // 분석 면적 (15km 반경 = 706.86 km²)
    private static final double ANALYSIS_AREA_KM2 = Math.PI * 15 * 15;

    /**
     * 토지의 변전소 개수 점수 계산
     * 정규화 공식: (x - 최소값) / (최대값 - 최소값)
     */
    public BigDecimal calculateSubstationCountScore(Long landId) {
        try {
            // 해당 토지의 변전소 개수 조회
            int substationCount = proximityDao.getSubstationCountByLandId(landId);

            // 전체 데이터의 최솟값, 최댓값 조회
            Integer minCount = proximityDao.findMinSubstationCount().orElse(0);
            Integer maxCount = proximityDao.findMaxSubstationCount().orElse(0);

            if (maxCount.equals(minCount)) {
                log.warn("변전소 개수의 최솟값과 최댓값이 동일합니다. landId: {}, count: {}", landId, substationCount);
                return BigDecimal.ONE; // 모든 값이 동일한 경우 1점 반환
            }

            // 정규화 점수 계산: (x - 최소값) / (최대값 - 최소값)
            BigDecimal score = BigDecimal.valueOf(substationCount - minCount)
                    .divide(BigDecimal.valueOf(maxCount - minCount), 4, RoundingMode.HALF_UP);

            // 0~1 범위로 제한
            if (score.compareTo(BigDecimal.ZERO) < 0) {
                score = BigDecimal.ZERO;
            } else if (score.compareTo(BigDecimal.ONE) > 0) {
                score = BigDecimal.ONE;
            }

            log.debug("변전소 개수 점수 계산 완료. landId: {}, count: {}, score: {}",
                    landId, substationCount, score);

            return score;

        } catch (Exception e) {
            log.error("변전소 개수 점수 계산 중 오류 발생. landId: {}", landId, e);
            return BigDecimal.ZERO;
        }
    }

    /**
     * 토지의 송전탑 개수 점수 계산
     * 정규화 공식: (x - 최소값) / (최대값 - 최소값)
     */
    public BigDecimal calculateTransmissionTowerCountScore(Long landId) {
        try {
            // 해당 토지의 송전탑 개수 조회
            int towerCount = proximityDao.getTransmissionTowerCountByLandId(landId);

            // 전체 데이터의 최솟값, 최댓값 조회
            Integer minCount = proximityDao.findMinTransmissionTowerCount().orElse(0);
            Integer maxCount = proximityDao.findMaxTransmissionTowerCount().orElse(0);

            if (maxCount.equals(minCount)) {
                log.warn("송전탑 개수의 최솟값과 최댓값이 동일합니다. landId: {}, count: {}", landId, towerCount);
                return BigDecimal.ONE; // 모든 값이 동일한 경우 1점 반환
            }

            // 정규화 점수 계산: (x - 최소값) / (최대값 - 최소값)
            BigDecimal score = BigDecimal.valueOf(towerCount - minCount)
                    .divide(BigDecimal.valueOf(maxCount - minCount), 4, RoundingMode.HALF_UP);

            // 0~1 범위로 제한
            if (score.compareTo(BigDecimal.ZERO) < 0) {
                score = BigDecimal.ZERO;
            } else if (score.compareTo(BigDecimal.ONE) > 0) {
                score = BigDecimal.ONE;
            }

            log.debug("송전탑 개수 점수 계산 완료. landId: {}, count: {}, score: {}",
                    landId, towerCount, score);

            return score;

        } catch (Exception e) {
            log.error("송전탑 개수 점수 계산 중 오류 발생. landId: {}", landId, e);
            return BigDecimal.ZERO;
        }
    }

    /**
     * 토지의 전력 인프라 종합 분석 결과 조회
     */
    public PowerInfrastructureAnalysisResult analyzePowerInfrastructure(Long landId) {
        try {
            // 변전소 관련 정보
            int substationCount = proximityDao.getSubstationCountByLandId(landId);
            BigDecimal substationScore = calculateSubstationCountScore(landId);

            // 송전탑 관련 정보
            int towerCount = proximityDao.getTransmissionTowerCountByLandId(landId);
            BigDecimal towerScore = calculateTransmissionTowerCountScore(landId);

            // 전력 인프라 근접성 상세 정보
            List<LandPowerInfrastructureProximity> proximityList = proximityDao.findByLandId(landId);

            return PowerInfrastructureAnalysisResult.builder()
                    .landId(landId)
                    .substationCount(substationCount)
                    .substationScore(substationScore)
                    .transmissionTowerCount(towerCount)
                    .transmissionTowerScore(towerScore)
                    .proximityDetails(proximityList)
                    .analysisAreaKm2(BigDecimal.valueOf(ANALYSIS_AREA_KM2))
                    .build();

        } catch (Exception e) {
            log.error("전력 인프라 분석 중 오류 발생. landId: {}", landId, e);
            throw new RuntimeException("전력 인프라 분석 실패", e);
        }
    }

    /**
     * 전력 인프라 통계 정보 조회
     */
    public Map<String, Object> getPowerInfrastructureStatistics() {
        Map<String, Object> statistics = new HashMap<>();

        try {
            // 변전소 통계
            Integer minSubstationCount = proximityDao.findMinSubstationCount().orElse(0);
            Integer maxSubstationCount = proximityDao.findMaxSubstationCount().orElse(0);

            statistics.put("substationMinCount", minSubstationCount);
            statistics.put("substationMaxCount", maxSubstationCount);

            // 송전탑 통계
            Integer minTowerCount = proximityDao.findMinTransmissionTowerCount().orElse(0);
            Integer maxTowerCount = proximityDao.findMaxTransmissionTowerCount().orElse(0);

            statistics.put("transmissionTowerMinCount", minTowerCount);
            statistics.put("transmissionTowerMaxCount", maxTowerCount);

            // 분석 면적 정보
            statistics.put("analysisAreaKm2", ANALYSIS_AREA_KM2);
            statistics.put("analysisRadiusKm", 15.0);

        } catch (Exception e) {
            log.error("전력 인프라 통계 조회 중 오류 발생", e);
        }

        return statistics;
    }
}