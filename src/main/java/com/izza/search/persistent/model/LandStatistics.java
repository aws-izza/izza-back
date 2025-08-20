package com.izza.search.persistent.model;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class LandStatistics {
    
    private String statType;
    private Long minValue;
    private Long maxValue;
    private LocalDateTime updatedAt;

    @Builder
    public LandStatistics(String statType, Long minValue, Long maxValue, LocalDateTime updatedAt) {
        this.statType = statType;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.updatedAt = updatedAt;
    }
}