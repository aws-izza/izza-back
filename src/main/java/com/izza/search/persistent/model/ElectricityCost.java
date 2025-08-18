package com.izza.search.persistent.model;

import java.math.BigDecimal;

/**
 * 전기 요금 정보 - 데이터베이스 매핑용 엔티티
 * 테이블: public.electricity
 */
public class ElectricityCost {
    
    private String fullCode;    // 법정동 코드
    private Integer year;       // 연도
    private Integer month;      // 월
    private String metro;       // 광역시/도
    private String city;        // 시/군/구
    private BigDecimal unitCost; // 단위 요금 (원/kWh)
    
    // 기본 생성자
    public ElectricityCost() {}
    
    // 전체 생성자
    public ElectricityCost(String fullCode, Integer year, Integer month, 
                          String metro, String city, BigDecimal unitCost) {
        this.fullCode = fullCode;
        this.year = year;
        this.month = month;
        this.metro = metro;
        this.city = city;
        this.unitCost = unitCost;
    }
    
    // Getters and Setters
    public String getFullCode() {
        return fullCode;
    }
    
    public void setFullCode(String fullCode) {
        this.fullCode = fullCode;
    }
    
    public Integer getYear() {
        return year;
    }
    
    public void setYear(Integer year) {
        this.year = year;
    }
    
    public Integer getMonth() {
        return month;
    }
    
    public void setMonth(Integer month) {
        this.month = month;
    }
    
    public String getMetro() {
        return metro;
    }
    
    public void setMetro(String metro) {
        this.metro = metro;
    }
    
    public String getCity() {
        return city;
    }
    
    public void setCity(String city) {
        this.city = city;
    }
    
    public BigDecimal getUnitCost() {
        return unitCost;
    }
    
    public void setUnitCost(BigDecimal unitCost) {
        this.unitCost = unitCost;
    }
    
    @Override
    public String toString() {
        return "ElectricityCost{" +
                "fullCode='" + fullCode + '\'' +
                ", year=" + year +
                ", month=" + month +
                ", metro='" + metro + '\'' +
                ", city='" + city + '\'' +
                ", unitCost=" + unitCost +
                '}';

    }
}