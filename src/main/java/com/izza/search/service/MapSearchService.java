package com.izza.search.service;

import com.izza.search.domain.BeopjungDongType;
import com.izza.search.domain.ZoomLevel;
import com.izza.search.persistent.BeopjungDong;
import com.izza.search.persistent.BeopjungDongDao;
import com.izza.search.persistent.query.CountLandQuery;
import com.izza.search.persistent.query.MapSearchQuery;
import com.izza.search.persistent.Land;
import com.izza.search.persistent.LandDao;
import com.izza.search.presentation.dto.LandGroupSearchResponse;
import com.izza.search.presentation.dto.LandSearchFilterRequest;
import com.izza.search.presentation.dto.MapSearchRequest;
import com.izza.search.vo.Point;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MapSearchService {
    private final BeopjungDongDao beopjungDongDao;
    private final LandDao landDao;

    public List<LandGroupSearchResponse> getAllLandGroupMarkers(
            MapSearchRequest mapSearchRequest, LandSearchFilterRequest landSearchFilterRequest
    ) {
        if (mapSearchRequest.zoomLevel() < 10) {
            return getLandGroupSearchResponses(mapSearchRequest, landSearchFilterRequest);
        } else {
            List<Land> lands = landDao.findLandsInMapBounds(mapSearchRequest, landSearchFilterRequest);

            return lands.stream().map(land ->
                    new LandGroupSearchResponse(
                            land.getId().toString(),
                            land.getAddress(),
                            null,
                            land.getCenterPoint(),
                            "LAND"
                    )).toList();
        }
    }

    private List<LandGroupSearchResponse> getLandGroupSearchResponses(
            MapSearchRequest mapSearchRequest, LandSearchFilterRequest landSearchFilterRequest
    ) {
        MapSearchQuery mapSearchQuery = new MapSearchQuery(
                ZoomLevel.from(mapSearchRequest.zoomLevel()),
                new Point(mapSearchRequest.southWestLng(), mapSearchRequest.southWestLat()),
                new Point(mapSearchRequest.northEastLng(), mapSearchRequest.northEastLat())
        );

        List<BeopjungDong> beopjeongDongs = beopjungDongDao.findAreasByZoomLevel(mapSearchQuery);

        return beopjeongDongs.stream().map(beopjungDong -> {
                    CountLandQuery query = new CountLandQuery(
                            beopjungDong.getFullCode(),
                            BeopjungDongType.valueOf(beopjungDong.getType().trim()),
                            landSearchFilterRequest.landAreaMin(),
                            landSearchFilterRequest.landAreaMax(),
                            landSearchFilterRequest.officialLandPriceMin(),
                            landSearchFilterRequest.officialLandPriceMax()
                    );

                    long count = landDao.countLandsByRegion(query);
                    return new LandGroupSearchResponse(
                            beopjungDong.getFullCode(),
                            beopjungDong.getSimpleName(),
                            count,
                            beopjungDong.getCenterPoint(), "GROUP");
                })
                .toList();
    }
}