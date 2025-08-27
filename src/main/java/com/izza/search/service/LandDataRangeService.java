package com.izza.search.service;

import com.izza.exception.BusinessException;
import com.izza.search.persistent.dao.BeopjungDongDao;
import com.izza.search.persistent.dao.ElectricityCostDao;
import com.izza.search.persistent.dao.LandStatisticsDao;
import com.izza.search.persistent.model.BeopjungDong;
import com.izza.search.persistent.model.ElectricityCost;
import com.izza.search.persistent.model.LandStatistics;
import com.izza.search.presentation.dto.LongRangeDto;
import com.izza.search.presentation.dto.response.RegionResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LandDataRangeService {

    private static final String LAND_AREA_RANGE_TYPE = "land_area_range";
    private static final String OFFICIAL_LAND_PRICE_RANGE_TYPE = "official_land_price_range";
    private static final String ELECTRIC_BILL_RANGE_TYPE = "electric_bill_range";
    private static final String TRANSMISSION_TOWER_COUNT_RANGE_TYPE = "transmission_tower_count_range";
    private static final String TRANSMISSION_LINE_COUNT_RANGE_TYPE = "transmission_line_count_range";
    private static final String SUBSTATION_COUNT_RANGE = "substation_count_range";
    private static final String DISASTER_COUNT_RANGE_TYPE = "disaster_count_range";

    private final LandStatisticsDao landStatisticsDao;
    private final ElectricityCostDao electricityCostDao;
    private final BeopjungDongDao beopjungDongDao;


    public LongRangeDto getLandAreaRange() {
        return getRange(LAND_AREA_RANGE_TYPE);
    }

    public LongRangeDto getOfficialLandPriceRange() {
        return getRange(OFFICIAL_LAND_PRICE_RANGE_TYPE);
    }

    public LongRangeDto getElectricBillRange(String fullCode) {
        if (StringUtils.isEmpty(fullCode)) {
            return getRange(ELECTRIC_BILL_RANGE_TYPE);
        }

        Optional<ElectricityCost> electricityCost = electricityCostDao.findByFullCode(fullCode);
        BigDecimal unitCost = electricityCost.get().getUnitCost();

        // 내림(Long)
        long unitCostFloor = unitCost.setScale(0, RoundingMode.FLOOR).longValue();

        // 올림(Long)
        long unitCostCeil = unitCost.setScale(0, RoundingMode.CEILING).longValue();
        return new LongRangeDto(unitCostFloor, unitCostCeil);

    }

    public LongRangeDto getTransmissionTowerCountRange() {
        return getRange(TRANSMISSION_TOWER_COUNT_RANGE_TYPE);
    }

    public LongRangeDto getTransmissionLineCountRange() {
        return getRange(TRANSMISSION_LINE_COUNT_RANGE_TYPE);
    }

    public LongRangeDto getSubstitutionCountRange() {
        return getRange(SUBSTATION_COUNT_RANGE);
    }

    public LongRangeDto getDisasterCountRange() {
        return getRange(DISASTER_COUNT_RANGE_TYPE);
    }

    public List<RegionResponse> getRegionsByFullCode(String fullCode) {
        List<BeopjungDong> regions;
        if (StringUtils.isEmpty(fullCode)) {
            regions = beopjungDongDao.findAllSido();
        } else {
            regions = beopjungDongDao.findByParentCode(fullCode);
        }
        return regions.stream()
                .map(region -> {
                    if (region.getType().equals("SIDO"))
                        return new RegionResponse(region.getFullCode(), region.getSidoName());
                    else {
                        return new RegionResponse(region.getFullCode(), region.getSigName());
                    }
                })
                .toList();
    }

    private LongRangeDto getRange(String statType) {
        Optional<LandStatistics> statistics = landStatisticsDao.findByStatType(statType);

        return statistics.map(stat -> new LongRangeDto(stat.getMinValue(), stat.getMaxValue()))
                .orElseThrow(() -> new BusinessException("통계 데이터를 찾을 수 없습니다: " + statType, HttpStatus.NOT_FOUND));
    }
}