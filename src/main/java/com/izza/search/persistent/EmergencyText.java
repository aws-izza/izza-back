package com.izza.search.persistent;

/**
 * 자연재해 정보 - 데이터베이스 매핑용 엔티티
 * 테이블: public.natural_disasters
 */
public class EmergencyText {

    private Integer id; // 자동 생성 ID
    private String receptionRegionName; // RCPTN_RGN_NM (접수 지역명)
    private String disasterTypeName; // DST_SE_NM (재해 구분명)
    private Integer count; // 발생 건수
    private String fullCode; // 법정동 코드

    // 기본 생성자
    public EmergencyText() {
    }

    // 전체 생성자
    public EmergencyText(Integer id, String receptionRegionName, String disasterTypeName,
            Integer count, String fullCode) {
        this.id = id;
        this.receptionRegionName = receptionRegionName;
        this.disasterTypeName = disasterTypeName;
        this.count = count;
        this.fullCode = fullCode;
    }

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getReceptionRegionName() {
        return receptionRegionName;
    }

    public void setReceptionRegionName(String receptionRegionName) {
        this.receptionRegionName = receptionRegionName;
    }

    public String getDisasterTypeName() {
        return disasterTypeName;
    }

    public void setDisasterTypeName(String disasterTypeName) {
        this.disasterTypeName = disasterTypeName;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public String getFullCode() {
        return fullCode;
    }

    public void setFullCode(String fullCode) {
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
