package com.izza.search.persistent.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LandStatistics {
    
    private String statType;
    private Long minValue;
    private Long maxValue;
    private LocalDateTime updatedAt;
    
    public LandStatistics() {}
    
    @Builder
    public LandStatistics(String statType, Long minValue, Long maxValue, LocalDateTime updatedAt) {
        this.statType = statType;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.updatedAt = updatedAt;
    }
}