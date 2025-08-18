package com.izza.search.persistent.dto.query;

import java.util.List;

public record CountLandQuery(
        List<String> fullCodePrefixes,
        Long landAreaMin,
        Long landAreaMax,
        Long officialLandPriceMin,
        Long officialLandPriceMax,
        List<Integer> useZoneIds
) {
}
