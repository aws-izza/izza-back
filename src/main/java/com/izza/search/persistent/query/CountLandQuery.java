package com.izza.search.persistent.query;

import com.izza.search.domain.BeopjungDongType;

public record CountLandQuery(
        String beopjungDongCode,
        BeopjungDongType type,
        Double landAreaMin,
        Double landAreaMax,
        Double officialLandPriceMin,
        Double officialLandPriceMax
) {
}
