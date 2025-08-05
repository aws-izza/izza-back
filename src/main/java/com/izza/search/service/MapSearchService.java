package com.izza.search.service;

import com.izza.search.domain.BeopjungDongType;
import com.izza.search.domain.ZoomLevel;
import com.izza.search.persistent.BeopjungDong;
import com.izza.search.persistent.BeopjungDongDao;
import com.izza.search.persistent.dto.LandCountQueryResult;
import com.izza.search.persistent.query.CountLandQuery;
import com.izza.search.persistent.query.MapSearchQuery;
import com.izza.search.persistent.Land;
import com.izza.search.persistent.LandDao;
import com.izza.search.presentation.dto.LandDetailResponse;
import com.izza.search.presentation.dto.LandGroupSearchResponse;
import com.izza.search.presentation.dto.LandSearchFilterRequest;
import com.izza.search.presentation.dto.MapSearchRequest;
import com.izza.search.presentation.dto.PolygonDataResponse;
import com.izza.search.vo.Point;
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

    public List<LandGroupSearchResponse> getAllLandGroupMarkers(
            MapSearchRequest mapSearchRequest, LandSearchFilterRequest landSearchFilterRequest) {
        ZoomLevel zoomLevel = ZoomLevel.from(mapSearchRequest.zoomLevel());
        if (zoomLevel.equals(ZoomLevel.LAND)) {
            return getLandSearchResponses(mapSearchRequest, landSearchFilterRequest);
        } else {
            return getGroupSearchResponses(mapSearchRequest, landSearchFilterRequest);
        }
    }

    private List<LandGroupSearchResponse> getLandSearchResponses(MapSearchRequest mapSearchRequest, LandSearchFilterRequest landSearchFilterRequest) {
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

        CountLandQuery query = new CountLandQuery(
                beopjeongDongs.stream().map(BeopjungDong::getFullCode).toList(),
                BeopjungDongType.valueOf(zoomLevel.getType()),
                landSearchFilterRequest.landAreaMin(),
                landSearchFilterRequest.landAreaMax(),
                landSearchFilterRequest.officialLandPriceMin(),
                landSearchFilterRequest.officialLandPriceMax(),
                useZoneIds);

        List<LandCountQueryResult> landCountQueryResults = landDao.countLandsByRegion(query);

        return zipFrom(beopjeongDongs, landCountQueryResults);
    }

    private List<LandGroupSearchResponse> zipFrom(
            List<BeopjungDong> beopjeongDongs, List<LandCountQueryResult> landCountQueryResults
    ) {
        List<LandGroupSearchResponse> response = new ArrayList<>();
        for (int i = 0; i < beopjeongDongs.size(); i++) {
            BeopjungDong beopjungDong = beopjeongDongs.get(i);
            LandCountQueryResult landCountQueryResult = landCountQueryResults.get(i);
            response.add(new LandGroupSearchResponse(
                    beopjungDong.getFullCode(),
                    beopjungDong.getSimpleName(),
                    landCountQueryResult.count(),
                    beopjungDong.getCenterPoint(),
                    "GROUP"
            ));
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
}