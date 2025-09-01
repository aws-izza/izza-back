package com.izza.utils;

import com.izza.search.presentation.dto.LongRangeDto;

public class LongRangeUtils {

    public static LongRangeDto normalize(long minValue, long maxValue, int bucketSize) {
        long normalizedMin = (minValue / bucketSize) * bucketSize;
        long normalizedMax = ((maxValue + bucketSize - 1) / bucketSize) * bucketSize;
        return new LongRangeDto(normalizedMin, normalizedMax);
    }

    public static LongRangeDto normalizeAreaRange(long minArea, long maxArea) {
        return normalize(minArea, maxArea, 500);
    }

    public static LongRangeDto normalizePriceRange(long minPrice, long maxPrice) {
        return normalize(minPrice, maxPrice, 500000);
    }
}