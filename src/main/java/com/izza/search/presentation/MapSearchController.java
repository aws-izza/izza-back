package com.izza.search.presentation;

import com.izza.search.service.MapSearchService;
import com.izza.search.presentation.dto.response.AreaDetailResponse;
import com.izza.search.presentation.dto.response.BaseApiResponse;
import com.izza.search.presentation.dto.response.LandDetailResponse;
import com.izza.search.presentation.dto.response.LandGroupSearchResponse;
import com.izza.search.presentation.dto.request.LandSearchFilterRequest;
import com.izza.search.presentation.dto.request.MapSearchRequest;
import com.izza.search.presentation.dto.response.PolygonDataResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("${app.base-path}/api/v1/land-search")
@RequiredArgsConstructor
@Tag(name = "토지 검색")
public class MapSearchController {
    private final MapSearchService mapSearchService;

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
        return BaseApiResponse.ok(mapSearchService.getAllLandGroupMarkers(mapSearchRequest, landSearchFilterRequest));
    }

    @GetMapping("/polygon/{id}")
    @Operation(summary = "특정 행정구역, 토지 폴리곤 데이터 조회",
        description = """
                특정 행정구역 또는 토지의 폴리곤 데이터를 조회합니다. \n
                행정구역은 법정동 코드로, 토지는 토지번호로 조회합니다.
                """)
    public BaseApiResponse<PolygonDataResponse> getLandPolygon(
            // polygonType: group (행정구역) || land (토지)
            @RequestParam("polygonType") String polygonType,
            @PathVariable("id") String id
    ) {
        return BaseApiResponse.ok(mapSearchService.getPolygonDataById(polygonType, id));
    }

    @GetMapping("/land/{landId}")
    @Operation(summary = "토지 상세 정보 조회")
    public BaseApiResponse<LandDetailResponse> getLandDetails(
            @PathVariable("landId") Long landId
    ) {
        return BaseApiResponse.ok(mapSearchService.getLandDataById(landId));
    }

    @GetMapping("/area/{landId}")
    @Operation(summary = "특정 토지가 소속된 행정구역의 상세 정보 조회")
    public BaseApiResponse<AreaDetailResponse> getAreaDetails(
            @PathVariable("landId") Long landId
    ) {
        return BaseApiResponse.ok(mapSearchService.getAreaDetailsByLandId(landId));
    }
}
