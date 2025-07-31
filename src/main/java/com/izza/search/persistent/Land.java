package com.izza.search.persistent;

import com.izza.search.vo.LandCategoryCode;
import com.izza.search.vo.UseDistrictCode;
import com.izza.search.vo.Point;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 토지대장 DTO
 */
@Data
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
    private Short useDistrictCode2;
    private String useDistrictName2;
    
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
    
    // 시스템 필드
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 기본 생성자
    public Land() {}
    
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
     * 지목 설명 반환
     */
    public String getLandCategoryDescription() {
        return getLandCategory().getDescription();
    }
    
    /**
     * 농업용 토지인지 확인
     */
    public boolean isAgriculturalLand() {
        return getLandCategory().isAgricultural();
    }
    
    /**
     * 건축 가능한 토지인지 확인
     */
    public boolean isBuildableLand() {
        return getLandCategory().isBuildable();
    }
    
    /**
     * 공공시설 토지인지 확인
     */
    public boolean isPublicFacilityLand() {
        return getLandCategory().isPublicFacility();
    }
    
    /**
     * 수자원 관련 토지인지 확인
     */
    public boolean isWaterRelatedLand() {
        return getLandCategory().isWaterRelated();
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
    
    /**
     * 두 번째 용도지구코드를 enum으로 반환
     */
    public UseDistrictCode getUseDistrict2() {
        if (useDistrictCode2 == null) {
            return UseDistrictCode.UNSPECIFIED;
        }
        return UseDistrictCode.fromCode(useDistrictCode2.intValue());
    }
    
    /**
     * 첫 번째 용도지구 설명 반환
     */
    public String getUseDistrict1Description() {
        return getUseDistrict1().getDescription();
    }
    
    /**
     * 두 번째 용도지구 설명 반환
     */
    public String getUseDistrict2Description() {
        return getUseDistrict2().getDescription();
    }
    
    /**
     * 개발진흥지구에 속하는지 확인
     */
    public boolean isDevelopmentDistrict() {
        return getUseDistrict1().isDevelopmentDistrict() || getUseDistrict2().isDevelopmentDistrict();
    }
    
    /**
     * 보호지구에 속하는지 확인
     */
    public boolean isProtectionDistrict() {
        return getUseDistrict1().isProtectionDistrict() || getUseDistrict2().isProtectionDistrict();
    }
    
    /**
     * 경관지구에 속하는지 확인
     */
    public boolean isLandscapeDistrict() {
        return getUseDistrict1().isLandscapeDistrict() || getUseDistrict2().isLandscapeDistrict();
    }
    
    /**
     * 건축 제한이 있는 용도지구인지 확인
     */
    public boolean hasConstructionRestrictionByDistrict() {
        return getUseDistrict1().hasConstructionRestriction() || getUseDistrict2().hasConstructionRestriction();
    }

    @Override
    public String toString() {
        return "LandDto{" +
                "id=" + id +
                ", shapeId=" + shapeId +
                ", uniqueNo='" + uniqueNo + '\'' +
                ", beopjungDongCode='" + beopjungDongCode + '\'' +
                ", address='" + address + '\'' +
                ", ledgerDivisionCode=" + ledgerDivisionCode +
                ", ledgerDivisionName='" + ledgerDivisionName + '\'' +
                ", baseYear=" + baseYear +
                ", baseMonth=" + baseMonth +
                ", landCategoryCode=" + landCategoryCode +
                ", landCategoryName='" + landCategoryName + '\'' +
                ", landArea=" + landArea +
                ", useDistrictCode1=" + useDistrictCode1 +
                ", useDistrictName1='" + useDistrictName1 + '\'' +
                ", useDistrictCode2=" + useDistrictCode2 +
                ", useDistrictName2='" + useDistrictName2 + '\'' +
                ", landUseCode=" + landUseCode +
                ", landUseName='" + landUseName + '\'' +
                ", terrainHeightCode=" + terrainHeightCode +
                ", terrainHeightName='" + terrainHeightName + '\'' +
                ", terrainShapeCode=" + terrainShapeCode +
                ", terrainShapeName='" + terrainShapeName + '\'' +
                ", roadSideCode=" + roadSideCode +
                ", roadSideName='" + roadSideName + '\'' +
                ", officialLandPrice=" + officialLandPrice +
                ", dataStandardDate=" + dataStandardDate +
                ", boundary=" + boundary +
                ", centerPoint=" + centerPoint +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}