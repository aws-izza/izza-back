package com.izza.search.persistent.model;

import com.izza.search.vo.LandCategoryCode;
import com.izza.search.vo.UseDistrictCode;
import com.izza.search.vo.Point;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 토지대장 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Land {

    private Long id;
    private Long shapeId;
    private String uniqueNo;
    private String beopjungDongCode;
    
    // 주소 정보
    private String address;
    
    // 대장 구분
    private Short ledgerDivisionCode;
    private String ledgerDivisionName;
    
    // 기준 연월
    private Short baseYear;
    private Short baseMonth;
    
    // 지목 정보
    private Short landCategoryCode;
    private String landCategoryName;
    
    // 토지 면적 (㎡)
    private BigDecimal landArea;
    
    // 용도지역 정보
    private Short useDistrictCode1;
    private String useDistrictName1;

    // 토지 이용 상황
    private Short landUseCode;
    private String landUseName;
    
    // 지형 정보
    private Short terrainHeightCode;
    private String terrainHeightName;
    private Short terrainShapeCode;
    private String terrainShapeName;
    
    // 도로 측면 정보
    private Short roadSideCode;
    private String roadSideName;
    
    // 공시지가 (원/㎡)
    private BigDecimal officialLandPrice;
    
    // 데이터 기준일자
    private LocalDateTime dataStandardDate;
    
    // 경계 정보 (PostGIS Geometry를 PointDto 리스트로 처리)
    private List<Point> boundary;
    
    // 토지 중심점
    private Point centerPoint;

    private String useZoneCategory;
    
    /**
     * 지목코드를 enum으로 반환
     */
    public LandCategoryCode getLandCategory() {
        if (landCategoryCode == null) {
            return LandCategoryCode.UNSPECIFIED;
        }
        return LandCategoryCode.fromCode(landCategoryCode.intValue());
    }

    /**
     * 첫 번째 용도지구코드를 enum으로 반환
     */
    public UseDistrictCode getUseDistrict1() {
        if (useDistrictCode1 == null) {
            return UseDistrictCode.UNSPECIFIED;
        }
        return UseDistrictCode.fromCode(useDistrictCode1.intValue());
    }
}