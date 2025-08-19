package com.izza.search.service;

import com.izza.search.persistent.dao.LandDataRangeDynamoDao;
import com.izza.search.persistent.model.LandDataRange;
import com.izza.search.presentation.dto.LongRangeDto;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LandDataRangeService {

    private static final String LAND_AREA_RANGE_TYPE = "land_area";
    private static final String OFFICIAL_LAND_PRICE_RANGE_TYPE = "official_land_price";

    private final LandDataRangeDynamoDao landDataRangeDynamoDao;

    public LandDataRangeService(LandDataRangeDynamoDao landDataRangeDynamoDao) {
        this.landDataRangeDynamoDao = landDataRangeDynamoDao;
    }

    public LongRangeDto getLandAreaRange() {
        return getRange(LAND_AREA_RANGE_TYPE);
    }

    public LongRangeDto getOfficialLandPriceRange() {
        return getRange(OFFICIAL_LAND_PRICE_RANGE_TYPE);
    }

    private LongRangeDto getRange(String rangeType) {
        Optional<LandDataRange> range = landDataRangeDynamoDao.findByRangeType(rangeType);
        
        return range.map(dataRange -> new LongRangeDto(dataRange.getMinValue(), dataRange.getMaxValue()))
                   .orElseThrow(() -> new IllegalArgumentException("Range data not found for type: " + rangeType));
    }
}