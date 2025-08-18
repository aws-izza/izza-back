package com.izza.search.service;

import com.izza.search.domain.BeopjungDongType;
import com.izza.search.domain.ZoomLevel;
import com.izza.search.persistent.model.BeopjungDong;
import com.izza.search.persistent.dao.BeopjungDongDao;
import com.izza.search.persistent.model.Land;
import com.izza.search.persistent.dao.LandDao;
import com.izza.search.persistent.dto.LandCountQueryResult;
import com.izza.search.persistent.model.ElectricityCost;
import com.izza.search.persistent.dao.ElectricityCostDao;
import com.izza.search.persistent.model.EmergencyText;
import com.izza.search.persistent.dao.EmergencyTextDao;
import com.izza.search.persistent.dto.query.CountLandQuery;
import com.izza.search.persistent.dto.query.MapSearchQuery;
import com.izza.search.persistent.model.Population;
import com.izza.search.persistent.dao.PopulationDao;
import com.izza.search.presentation.dto.response.AreaDetailResponse;
import com.izza.search.presentation.dto.response.LandDetailResponse;
import com.izza.search.presentation.dto.response.LandGroupSearchResponse;
import com.izza.search.presentation.dto.request.LandSearchFilterRequest;
import com.izza.search.presentation.dto.request.MapSearchRequest;
import com.izza.search.presentation.dto.response.PolygonDataResponse;
import com.izza.search.vo.ElectricityCostInfo;
import com.izza.search.vo.EmergencyTextInfo;
import com.izza.search.vo.Point;
import com.izza.search.vo.PopulationInfo;
import com.izza.search.vo.UseZoneCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
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
        ZoomLevel zoomLevel = ZoomLevel.from(mapSearchRequest.zoomLevel());
        MapSearchQuery mapSearchQuery = new MapSearchQuery(
                zoomLevel,
                new Point(mapSearchRequest.southWestLng(), mapSearchRequest.southWestLat()),
                new Point(mapSearchRequest.northEastLng(), mapSearchRequest.northEastLat()));

        List<BeopjungDong> beopjeongDongs = beopjungDongDao.findAreasByZoomLevel(mapSearchQuery);

        List<Integer> useZoneIds = UseZoneCode
                .convertCategoryNamesToZoneCodes(landSearchFilterRequest.useZoneCategories());

        BeopjungDongType beopjungDongType = BeopjungDongType.valueOf(zoomLevel.getType());
        List<String> fullCodePrefixes = beopjeongDongs.stream()
                .map(dong -> dong.getFullCode().substring(0, beopjungDongType.getCodeLength()))
                .distinct()
                .toList();

        List<LandCountQueryResult> landCountQueryResults = new ArrayList<>();

        if (!beopjeongDongs.isEmpty()) {
            CountLandQuery query = new CountLandQuery(
                    fullCodePrefixes,
                    landSearchFilterRequest.landAreaMin(),
                    landSearchFilterRequest.landAreaMax(),
                    landSearchFilterRequest.officialLandPriceMin(),
                    landSearchFilterRequest.officialLandPriceMax(),
                    useZoneIds);

            landCountQueryResults = landDao.countLandsByRegions(query);
        }

        return zipFrom(beopjeongDongs, landCountQueryResults);
    }

    private List<LandGroupSearchResponse> zipFrom(
            List<BeopjungDong> beopjeongDongs, List<LandCountQueryResult> landCountQueryResults) {
        List<LandGroupSearchResponse> response = new ArrayList<>();
        for (BeopjungDong beopjungDong : beopjeongDongs) {
            Long count = landCountQueryResults.stream()
                    .filter(result -> beopjungDong.getFullCode().startsWith(result.beopjungDongCodePrefix()))
                    .findAny()
                    .map(LandCountQueryResult::count)
                    .orElse(0L);

            response.add(new LandGroupSearchResponse(
                    beopjungDong.getFullCode(),
                    beopjungDong.getSimpleName(),
                    count,
                    beopjungDong.getCenterPoint(),
                    "GROUP"));
        }
        return response;
    }

    public PolygonDataResponse getPolygonDataById(
            String polygonType,
            String id) {

        if (polygonType.equals("GROUP")) {
            List<List<Point>> areaPolygon = beopjungDongDao.findPolygonByFullCode(id);
            return new PolygonDataResponse(areaPolygon);
        } else if (polygonType.equals("LAND")) {
            List<List<Point>> landPolygon = landDao.findPolygonByUniqueNumber(id);
            return new PolygonDataResponse(landPolygon);
        } else {
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
        Optional<Land> landOptional = landDao.findById(landId);
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