package com.izza.analysis.service.adapter;

import com.izza.analysis.presentation.dto.AnalysisRangeDto;

/**
 * 토지 데이터 범위 조회를 위한 어댑터 인터페이스
 */
public interface LandDataRangeAdapter {

    /**
     * 변전소 개수 범위 조회
     */
    AnalysisRangeDto getSubstationCountRange();

    /**
     * 송전탑 개수 범위 조회
     */
    AnalysisRangeDto getTransmissionTowerCountRange();

    /**
     * 송전선 개수 범위 조회
     */
    AnalysisRangeDto getTransmissionLineCountRange();

    /**
     * 재난문자 발송 건수 범위 조회
     */
    AnalysisRangeDto getDisasterCountRange();

    /**
     * 전기요금 범위 조회
     */
    AnalysisRangeDto getElectricBillRange();
}