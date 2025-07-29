package com.izza.search.presentation;

import com.izza.search.vo.Point;
import com.izza.search.presentation.dto.BaseApiResponse;
import com.izza.search.presentation.dto.LandDetailResponse;
import com.izza.search.presentation.dto.LandGroupSearchResponse;
import com.izza.search.presentation.dto.LandSearchFilterRequest;
import com.izza.search.presentation.dto.MapSearchRequest;
import com.izza.search.presentation.dto.PolygonDataResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.websocket.server.PathParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/land-search")
@Tag(name = "토지 검색")
public class LandSearchController {

    @GetMapping("/points")
    @Operation(summary = "지도상 행정구역 정보 검색 (마커)",
            description = """
                    지도 화면 범위 내 행정 구역에 검색된 토지 갯수 혹은 토지를 조회합니다. \n
                    테스트 API에서는 Zoom Level이 10 이상이면 토지가 조회됩니다.
                    """)
    public BaseApiResponse<List<LandGroupSearchResponse>> getAllLandGroupMarkers(
            @ModelAttribute MapSearchRequest mapSearchRequest,
            @ModelAttribute LandSearchFilterRequest landSearchFilterRequest
    ) {
        List<LandGroupSearchResponse> response;
        if (mapSearchRequest.zoomLevel() < 10) {
            response = List.of(
                    new LandGroupSearchResponse(1L, "서울특별시", 12345L, new Point(37.551902938787826, 126.9918052066339), "GROUP"),
                    new LandGroupSearchResponse(2L, "경기도", 1110223L, new Point(37.551902938787826, 126.9918052066339), "GROUP")
            );
        } else {
            response = List.of(
                    new LandGroupSearchResponse(1L, "서울특별시 금천구 벚꽃로 105", null, new Point(37.551902938787826, 126.9918052066339), "LAND"),
                    new LandGroupSearchResponse(1L, "서울특별시 금천구 벚꽃로 104", null, new Point(37.551902938787826, 126.9918052066339), "LAND")
            );
        }

        return BaseApiResponse.ok(response);
    }

    @GetMapping("/polygon")
    @Operation(summary = "특정 행정구역, 토지 폴리곤 데이터 조회")
    public BaseApiResponse<PolygonDataResponse> getLandPolygon(
            //type은 group, land
            @PathParam("polygonType") String polygonType
    ) {
        List<java.awt.Point> points = List.of();
        return BaseApiResponse.ok(new PolygonDataResponse(points));
    }

    @GetMapping("/land/{landId}")
    @Operation(summary = "토지 상세 정보 조회")
    public BaseApiResponse<LandDetailResponse> getLandDetails(
            @PathVariable("landId") Long landId
    ) {
        var response = new LandDetailResponse(1L, "서울특별시 금천구 벚꽃로",
                120.1, 130_000_000L, "일반공업지역");
        return BaseApiResponse.ok(response);
    }
}
