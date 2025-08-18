package com.izza.search.persistent.model;

import lombok.Getter;
import lombok.Setter;

/**
 * 자연재해 정보 - 데이터베이스 매핑용 엔티티
 * 테이블: public.natural_disasters
 */
@Setter
@Getter
public class EmergencyText {

    // Getters and Setters
    // 자동 생성 ID
    private Integer id;

    // RCPTN_RGN_NM (접수 지역명)
    private String receptionRegionName;

    // DST_SE_NM (재해 구분명)
    private String disasterTypeName;

    // 발생 건수
    private Integer count;

    // 법정동 코드
    private String fullCode;

    // 기본 생성자
    public EmergencyText() {
    }

    public EmergencyText(Integer id, String receptionRegionName, String disasterTypeName,
            Integer count, String fullCode) {
        this.id = id;
        this.receptionRegionName = receptionRegionName;
        this.disasterTypeName = disasterTypeName;
        this.count = count;
        this.fullCode = fullCode;
    }

    @Override
    public String toString() {
        return "EmergencyText{" +
                "id=" + id +
                ", receptionRegionName='" + receptionRegionName + '\'' +
                ", disasterTypeName='" + disasterTypeName + '\'' +
                ", count=" + count +
                ", fullCode='" + fullCode + '\'' +
                '}';
    }
}
