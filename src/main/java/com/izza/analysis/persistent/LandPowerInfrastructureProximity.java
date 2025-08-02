package com.izza.analysis.persistent;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * 토지 전력 인프라 근접성 DTO
 */
@Data
public class LandPowerInfrastructureProximity {
    
    private Long id;
    private Long landId;
    private String infrastructureType;
    private String infrastructureOsmId;
    private BigDecimal distanceMeters;
    private Integer voltage;
    private Map<String, Object> additionalInfo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // 기본 생성자
    public LandPowerInfrastructureProximity() {}
    
    /**
     * 인프라 타입이 변전소인지 확인
     */
    public boolean isSubstation() {
        return "substation".equals(infrastructureType);
    }
    
    /**
     * 인프라 타입이 송전선인지 확인
     */
    public boolean isTransmissionLine() {
        return "transmission_line".equals(infrastructureType);
    }
    
    /**
     * 인프라 타입이 송전탑인지 확인
     */
    public boolean isTransmissionTower() {
        return "transmission_tower".equals(infrastructureType);
    }
    
    /**
     * 거리를 킬로미터 단위로 반환
     */
    public BigDecimal getDistanceKilometers() {
        if (distanceMeters == null) {
            return null;
        }
        return distanceMeters.divide(BigDecimal.valueOf(1000), 3, BigDecimal.ROUND_HALF_UP);
    }
    
    /**
     * 고압 인프라인지 확인 (22.9kV 이상)
     */
    public boolean isHighVoltage() {
        return voltage != null && voltage >= 22900;
    }
    
    /**
     * 초고압 인프라인지 확인 (154kV 이상)
     */
    public boolean isExtraHighVoltage() {
        return voltage != null && voltage >= 154000;
    }
    
    /**
     * 근접 거리인지 확인 (1km 이내)
     */
    public boolean isNearby() {
        return distanceMeters != null && distanceMeters.compareTo(BigDecimal.valueOf(1000)) <= 0;
    }
    
    /**
     * 매우 근접한 거리인지 확인 (500m 이내)
     */
    public boolean isVeryNearby() {
        return distanceMeters != null && distanceMeters.compareTo(BigDecimal.valueOf(500)) <= 0;
    }
    
    /**
     * 추가 정보에서 특정 키의 값을 반환
     */
    public Object getAdditionalInfoValue(String key) {
        if (additionalInfo == null) {
            return null;
        }
        return additionalInfo.get(key);
    }
    
    /**
     * 추가 정보에서 케이블 수 반환 (송전선의 경우)
     */
    public Integer getCables() {
        Object cables = getAdditionalInfoValue("cables");
        if (cables instanceof Number) {
            return ((Number) cables).intValue();
        }
        return null;
    }
    
    /**
     * 추가 정보에서 회로 수 반환 (송전선의 경우)
     */
    public Integer getCircuits() {
        Object circuits = getAdditionalInfoValue("circuits");
        if (circuits instanceof Number) {
            return ((Number) circuits).intValue();
        }
        return null;
    }

    @Override
    public String toString() {
        return "LandPowerInfrastructureProximity{" +
                "id=" + id +
                ", landId=" + landId +
                ", infrastructureType='" + infrastructureType + '\'' +
                ", infrastructureOsmId='" + infrastructureOsmId + '\'' +
                ", distanceMeters=" + distanceMeters +
                ", voltage=" + voltage +
                ", additionalInfo=" + additionalInfo +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}