package com.izza.analysis.service.adapter;

import com.izza.analysis.presentation.dto.AnalysisRangeDto;
import com.izza.search.presentation.dto.LongRangeDto;
import com.izza.search.service.LandDataRangeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * LandDataRangeService와의 통신을 위한 어댑터 구현체
 * MSA 확장 시 이 부분을 HTTP 클라이언트나 메시지큐 기반으로 교체 가능
 */
@Component
@RequiredArgsConstructor
public class LandDataRangeAdapterImpl implements LandDataRangeAdapter {

    private final LandDataRangeService landDataRangeService;

    @Override
    public AnalysisRangeDto getSubstationCountRange() {
        LongRangeDto searchDto = landDataRangeService.getSubstitutionCountRange();
        return convertToAnalysisRangeDto(searchDto);
    }

    @Override
    public AnalysisRangeDto getTransmissionTowerCountRange() {
        LongRangeDto searchDto = landDataRangeService.getTransmissionTowerCountRange();
        return convertToAnalysisRangeDto(searchDto);
    }

    @Override
    public AnalysisRangeDto getTransmissionLineCountRange() {
        LongRangeDto searchDto = landDataRangeService.getTransmissionLineCountRange();
        return convertToAnalysisRangeDto(searchDto);
    }


    @Override
    public AnalysisRangeDto getDisasterCountRange() {
        // 현재는 임시로 고정값 반환
        LongRangeDto searchDto = landDataRangeService.getDisasterCountRange();
        return convertToAnalysisRangeDto(searchDto);
    }

    /**
     * search 도메인의 LongRangeDto를 analysis 도메인의 AnalysisRangeDto로 변환
     */
    private AnalysisRangeDto convertToAnalysisRangeDto(LongRangeDto longRangeDto) {
        return AnalysisRangeDto.of(
                longRangeDto.min(),
                longRangeDto.max()
        );
    }
}