package com.izza.analysis.vo;

/**
 * 업종 타입 열거형
 * 인구밀도 허용편차 계산을 위한 업종 분류
 */
public enum IndustryType {
    
    /**
     * 제조업 - 기준값: 3,000명/km², 허용편차: ±700
     */
    MANUFACTURING("제조업", 3000, 700),
    
    /**
     * 물류업 - 기준값: 5,000명/km², 허용편차: ±900
     */
    LOGISTICS("물류업", 5000, 900),
    
    /**
     * 정보통신업 - 기준값: 2,000명/km², 허용편차: ±500
     */
    IT("정보통신업", 2000, 500);
    
    private final String displayName;
    private final double standardDensity;
    private final double allowedDeviation;
    
    IndustryType(String displayName, double standardDensity, double allowedDeviation) {
        this.displayName = displayName;
        this.standardDensity = standardDensity;
        this.allowedDeviation = allowedDeviation;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public double getStandardDensity() {
        return standardDensity;
    }
    
    public double getAllowedDeviation() {
        return allowedDeviation;
    }
    
    /**
     * 문자열 코드로부터 IndustryType을 찾는 팩토리 메서드
     */
    public static IndustryType fromCode(String code) {
        if (code == null) {
            return null;
        }
        
        try {
            return IndustryType.valueOf(code.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}