package com.izza.search.presentation.dto;

import com.izza.search.vo.ElectricityCostInfo;
import com.izza.search.vo.EmergencyTextInfo;
import com.izza.search.vo.PopulationInfo;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "행정구역 상세 정보 응답")
public record AreaDetailResponse(

                @Schema(description = "법정동 코드", example = "1100000000") 
                String fullCode,

                @Schema(description = "법정동 이름", example = "대구광역시 중구") 
                String address,

                @Schema(description = "전기 요금 정보") 
                ElectricityCostInfo electricityCostInfo,

                @Schema(description = "자연재해 정보") 
                EmergencyTextInfo emergencyTextInfo,

                @Schema(description = "인구 정보") 
                PopulationInfo populationInfo) {
}
