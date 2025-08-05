package com.izza.search.service;

import com.izza.search.domain.BeopjungDongType;
import com.izza.search.domain.ZoomLevel;
import com.izza.search.persistent.BeopjungDong;
import com.izza.search.persistent.BeopjungDongDao;
import com.izza.search.persistent.ElectricityCost;
import com.izza.search.persistent.ElectricityCostDao;
import com.izza.search.persistent.EmergencyText;
import com.izza.search.persistent.EmergencyTextDao;
import com.izza.search.persistent.query.CountLandQuery;
import com.izza.search.persistent.query.MapSearchQuery;
import com.izza.search.persistent.Land;
import com.izza.search.persistent.LandDao;
import com.izza.search.persistent.Population;
import com.izza.search.persistent.PopulationDao;
import com.izza.search.presentation.dto.AreaDetailResponse;
import com.izza.search.presentation.dto.LandDetailResponse;
import com.izza.search.presentation.dto.LandGroupSearchResponse;
import com.izza.search.presentation.dto.LandSearchFilterRequest;
import com.izza.search.presentation.dto.MapSearchRequest;
import com.izza.search.presentation.dto.PolygonDataResponse;
import com.izza.search.vo.ElectricityCostInfo;
import com.izza.search.vo.EmergencyTextInfo;
import com.izza.search.vo.Point;
import com.izza.search.vo.PopulationInfo;
import com.izza.search.vo.UseZoneCode;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MapSearchService {
    private final BeopjungDongDao beopjungDongDao;
    private final LandDao landDao;
    private final ElectricityCostDao electricityCostDao;
    private final EmergencyTextDao emergencyTextDao;
    private final PopulationDao populationDao;

    public List<LandGroupSearchResponse> getAllLandGroupMarkers(
            MapSearchRequest mapSearchRequest, LandSearchFilterRequest landSearchFilterRequest) {
        ZoomLevel zoomLevel = ZoomLevel.from(mapSearchRequest.zoomLevel());
        if (zoomLevel.equals(ZoomLevel.LAND)) {
            return getLandSearchResponses(mapSearchRequest, landSearchFilterRequest);
        } else {
            return getGroupSearchResponses(mapSearchRequest, landSearchFilterRequest);
        }
    }

    private List<LandGroupSearchResponse> getLandSearchResponses(MapSearchRequest mapSearchRequest,
            LandSearchFilterRequest landSearchFilterRequest) {
        List<Land> lands = landDao.findLandsInMapBounds(mapSearchRequest, landSearchFilterRequest);

        return lands.stream().map(land -> new LandGroupSearchResponse(
                land.getUniqueNo(),
                land.getAddress(),
                null,
                land.getCenterPoint(),
                "LAND")).toList();
    }

    private List<LandGroupSearchResponse> getGroupSearchResponses(
            MapSearchRequest mapSearchRequest, LandSearchFilterRequest landSearchFilterRequest) {
        MapSearchQuery mapSearchQuery = new MapSearchQuery(
                ZoomLevel.from(mapSearchRequest.zoomLevel()),
                new Point(mapSearchRequest.southWestLng(), mapSearchRequest.southWestLat()),
                new Point(mapSearchRequest.northEastLng(), mapSearchRequest.northEastLat()));

        List<BeopjungDong> beopjeongDongs = beopjungDongDao.findAreasByZoomLevel(mapSearchQuery);

        return beopjeongDongs.stream().map(beopjungDong -> {
            // String -> UseZoneCategory -> UseZoneCode.code 로 변환
            List<Integer> useZoneIds = UseZoneCode
                    .convertCategoryNamesToZoneCodes(landSearchFilterRequest.useZoneCategories());

            CountLandQuery query = new CountLandQuery(
                    beopjungDong.getFullCode(),
                    BeopjungDongType.valueOf(beopjungDong.getType().trim()),
                    landSearchFilterRequest.landAreaMin(),
                    landSearchFilterRequest.landAreaMax(),
                    landSearchFilterRequest.officialLandPriceMin(),
                    landSearchFilterRequest.officialLandPriceMax(),
                    useZoneIds);

            long count = landDao.countLandsByRegion(query);
            return new LandGroupSearchResponse(
                    beopjungDong.getFullCode(),
                    beopjungDong.getSimpleName(),
                    count,
                    beopjungDong.getCenterPoint(), "GROUP");
        })
                .toList();
    }

    public PolygonDataResponse getPolygonDataById(
            String polygonType,
            String id) {

        if (polygonType.equals("GROUP")) { // 토지 폴리곤
            List<Point> areaPolygon = beopjungDongDao.findPolygonByFullCode(id).orElse(List.of());
            return new PolygonDataResponse(areaPolygon);
        } else if (polygonType.equals("LAND")) { // 행정구역 폴리곤
            List<Point> landPolygon = landDao.findPolygonByUniqueNumber(id).orElse(List.of());
            return new PolygonDataResponse(landPolygon);
        } else { // 에러
            throw new IllegalArgumentException("유효하지 않은 폴리곤 타입 입니다: " + polygonType);
        }

    }

    public LandDetailResponse getLandDataById(String landId) {
        Optional<Land> landOptional = landDao.findById(landId);
        if (landOptional.isEmpty()) {
            throw new IllegalArgumentException("Land not found with id: " + landId);
        }

        Land land = landOptional.get();

        return new LandDetailResponse(
                land.getUniqueNo(),
                land.getBeopjungDongCode(),
                land.getAddress(),
                land.getLedgerDivisionCode(),
                land.getLedgerDivisionName(),
                land.getBaseYear(),
                land.getBaseMonth(),
                land.getLandCategoryCode(),
                land.getLandCategoryName(),
                land.getLandArea(),
                land.getUseDistrictCode1(),
                land.getUseDistrictName1(),
                land.getUseDistrictCode2(),
                land.getUseDistrictName2(),
                land.getLandUseCode(),
                land.getLandUseName(),
                land.getTerrainHeightCode(),
                land.getTerrainHeightName(),
                land.getTerrainShapeCode(),
                land.getTerrainShapeName(),
                land.getRoadSideCode(),
                land.getRoadSideName(),
                land.getOfficialLandPrice(),
                land.getDataStandardDate(),
                land.getBoundary(),
                land.getCenterPoint());
    }

    public AreaDetailResponse getAreaDetails(
            String landId) {
        // first fetch the land of the land from landId
        Optional<Land> landOptional = landDao.findByFullCode(landId);
        if (landOptional.isEmpty()) {
            throw new IllegalArgumentException("Land not found with id: " + landId);
        }

        // then extract its full_code, converting it to sig_code
        Land land = landOptional.get();
        String full_code = land.getBeopjungDongCode();
        String sig_code = full_code.substring(0, 5) + "00000";

        // then fetch the area's information using sig_code
        Optional<BeopjungDong> areaOptional = beopjungDongDao.findByFullCode(sig_code);
        if (areaOptional.isEmpty()) {
            throw new IllegalArgumentException("Area not found with full_code: " + sig_code);
        }
        
        BeopjungDong area = areaOptional.get();
        String address = area.getKoreanName();

        // then fetch electricity cost
        ElectricityCost electricityCost = electricityCostDao.findAllByFullCode(sig_code).get(0);
        ElectricityCostInfo costInfo = ElectricityCostInfo.of(electricityCost);

        // then fetch emergency texts
        List<EmergencyText> emergencyText = emergencyTextDao.findByFullCode(sig_code);
        EmergencyTextInfo textInfo = EmergencyTextInfo.fromDisasterList(emergencyText);

        // lastly populations
        // population data need to be accumulated - they're on EMD level
        Population populationSum = populationDao.findAggregatedByFullCode(full_code);
        PopulationInfo populationInfo = PopulationInfo.of(populationSum);

        return new AreaDetailResponse(full_code, address ,costInfo, textInfo, populationInfo);
    }
}