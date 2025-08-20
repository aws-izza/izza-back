package com.izza.search.service;

import com.izza.search.persistent.dao.LandStatisticsDao;
import com.izza.search.persistent.model.LandStatistics;
import com.izza.search.presentation.dto.LongRangeDto;
import org.springframework.stereotype.Service;

import java.util.Optional;
import com.izza.exception.BusinessException;
import org.springframework.http.HttpStatus;

@Service
public class LandDataRangeService {

    private static final String LAND_AREA_RANGE_TYPE = "land_area_range";
    private static final String OFFICIAL_LAND_PRICE_RANGE_TYPE = "official_land_price_range";

    private final LandStatisticsDao landStatisticsDao;

    public LandDataRangeService(LandStatisticsDao landStatisticsDao) {
        this.landStatisticsDao = landStatisticsDao;
    }

    public LongRangeDto getLandAreaRange() {
        return getRange(LAND_AREA_RANGE_TYPE);
    }

    public LongRangeDto getOfficialLandPriceRange() {
        return getRange(OFFICIAL_LAND_PRICE_RANGE_TYPE);
    }

    private LongRangeDto getRange(String statType) {
        Optional<LandStatistics> statistics = landStatisticsDao.findByStatType(statType);
        
        return statistics.map(stat -> new LongRangeDto(stat.getMinValue(), stat.getMaxValue()))
                        .orElseThrow(() -> new BusinessException("통계 데이터를 찾을 수 없습니다: " + statType, HttpStatus.NOT_FOUND));
    }
}