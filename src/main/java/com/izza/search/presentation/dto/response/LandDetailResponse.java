package com.izza.search.presentation.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.izza.search.vo.Point;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "토지 상세 정보 응답")
public record LandDetailResponse(

        // 기본 구분자
        @Schema(description = "토지 ID", example = "1")
        String uniqueNo,

        @Schema(description = "법정동 코드", example = "1100000000")
        String full_code,

        // 주소 정보
        @Schema(description = "토지 주소", example = "대구광역시 중구 동인동1가 7-1")
        String address,

        // 대장 구분
        @Schema(description = "대장 구분 코드", example = "1")
        Short ledgerDivisionCode,

        @Schema(description = "대장 구분 이름", example = "일반")
        String ledgerDivisionName,

        // 기준 연월
        @Schema(description = "기준 년", example = "2024")
        Short baseYear,

        @Schema(description = "기준 월", example = "1")
        Short baseMonth,

        // 지목 정보
        @Schema(description = "지목 코드", example = "1")
        Short landCategoryCode,

        @Schema(description = "지목 이름", example = "종교용지")
        String landCategoryName,

        // 토지 면적 (㎡)
        @Schema(description = "토지 면적", example = "880")
        BigDecimal landArea,

        // 용도지역 정보
        @Schema(description = "용도지역 코드 1", example = "21")
        Short useDistrictCode1,

        @Schema(description = "용도지역 이름 1", example = "중심상업지역")
        String useDistrictName1,

        // 토지 이용 상황
        @Schema(description = "토지 이용 코드", example = "220")
        Short landUseCode,
        
        @Schema(description = "토지 이용 이름", example = "업무용")
        String landUseName,

        // 지형 정보
        @Schema(description = "지형 높이 코드", example = "2")
        Short terrainHeightCode,

        @Schema(description = "지형 높이 이름", example = "평지")
        String terrainHeightName,

        @Schema(description = "지형 모양 코드", example = "4")
        Short terrainShapeCode,

        @Schema(description = "지형 모양 이름", example = "사다리형")
        String terrainShapeName,

        // 도로 측면 정보
        @Schema(description = "도로 측면 코드", example = "2")
        Short roadSideCode,

        @Schema(description = "도로 측면 이름", example = "광대소각")
        String roadSideName,
    
        // 공시지가 (원/㎡)
        @Schema(description = "공시지가 (원/㎡)", example = "1764000")
        BigDecimal officialLandPrice,
    
        // 데이터 기준일자
        @Schema(description = "데이터 기준일자", example = "2025-01-20 20:48:59.938")
        LocalDateTime dataStandardDate,
    
        // 경계 정보 (PostGIS Geometry를 PointDto 리스트로 처리)
        @Schema(description = "경계 정보")
        List<Point> boundary,
    
        // 토지 중심점
        @Schema(description = "토지 중심점")
        Point centerPoint
) {
}
