package com.izza.search.service;

import com.izza.search.domain.ZoomLevel;
import com.izza.search.persistent.AreaPolygon;
import com.izza.search.persistent.BeopjungDongDao;
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
    private final BeopjungDongDao polygonDao;
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
        MapSearchQuery areaQuery = new MapSearchQuery(
                ZoomLevel.from(mapSearchRequest.zoomLevel()),
                new Point(mapSearchRequest.southWestLng(), mapSearchRequest.southWestLat()),
                new Point(mapSearchRequest.northEastLng(), mapSearchRequest.northEastLat())
        );
        System.out.println(areaQuery);

        List<AreaPolygon> beopjeongDongs = polygonDao.findAreasByZoomLevel(areaQuery);
        System.out.println(beopjeongDongs);

        return beopjeongDongs.stream().map(b -> {
                    long count = landDao.countLandsByRegion(b.getFullCode(), mapSearchRequest.zoomLevel(), landSearchFilterRequest);
                    return new LandGroupSearchResponse(b.getFullCode(), b.getKoreanName(), count, b.getCenterPoint(), "GROUP");
                })
                .toList();
    }
}