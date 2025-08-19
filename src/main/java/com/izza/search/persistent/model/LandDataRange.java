package com.izza.search.persistent.model;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

import java.time.Instant;

@DynamoDbBean
public class LandDataRange {
    
    private String rangeType;
    private Long minValue;
    private Long maxValue;
    private String lastUpdated;
    private Long ttl;

    public LandDataRange() {}

    public LandDataRange(String rangeType, Long minValue, Long maxValue) {
        this.rangeType = rangeType;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.lastUpdated = Instant.now().toString();
        this.ttl = Instant.now().plusSeconds(3600).getEpochSecond(); // 1시간 TTL
    }

    @DynamoDbPartitionKey
    public String getRangeType() {
        return rangeType;
    }

    public void setRangeType(String rangeType) {
        this.rangeType = rangeType;
    }

    public Long getMinValue() {
        return minValue;
    }

    public void setMinValue(Long minValue) {
        this.minValue = minValue;
    }

    public Long getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(Long maxValue) {
        this.maxValue = maxValue;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public Long getTtl() {
        return ttl;
    }

    public void setTtl(Long ttl) {
        this.ttl = ttl;
    }
}