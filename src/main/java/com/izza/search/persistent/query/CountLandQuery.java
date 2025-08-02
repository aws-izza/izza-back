package com.izza.search.persistent.query;

import com.izza.search.domain.BeopjungDongType;
import java.util.List;

public record CountLandQuery(
        String beopjungDongCode,
        BeopjungDongType type,
        Long landAreaMin,
        Long landAreaMax,
        Long officialLandPriceMin,
        Long officialLandPriceMax,
        List<Integer> useZoneIds
) {
}
