package com.izza.search.presentation;

import com.izza.search.service.MapSearchService;
import com.izza.search.presentation.dto.AreaDetailResponse;
import com.izza.search.presentation.dto.BaseApiResponse;
import com.izza.search.presentation.dto.LandDetailResponse;
import com.izza.search.presentation.dto.LandGroupSearchResponse;
import com.izza.search.presentation.dto.LandSearchFilterRequest;
import com.izza.search.presentation.dto.MapSearchRequest;
import com.izza.search.presentation.dto.PolygonDataResponse;
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
@RequestMapping("/api/v1/land-search")
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
            @PathVariable("landId") String landId
    ) {
        return BaseApiResponse.ok(mapSearchService.getLandDataById(landId));
    }

    @GetMapping("/area/{landId}")
    @Operation(summary = "특정 토지가 소속된 행정구역의 상세 정보 조회")
    public BaseApiResponse<AreaDetailResponse> getAreaDetails(
            @PathVariable("landId") String landId
    ) {
        
        /*
         * user selects a land?
         * -> fetch the full_code of the area the includes the land
         * -> get electricity cost, population, and emergency texts within that area
         * -> -> each data tables show data on diff. district levels, need to be accumulated to SIG level?
         * return the area's details
         */

         // ANCHOR:: ELECTRICITY
         // land table lists full_code until EMD level but electricity only lists it until SIG
         // actually this works out nicely because we need SIG anyways
         // NOTE :: so match land and electricity on full_code on SIG level


         // ANCHOR:: NATURAL_DISASTERS
         // natural_disasters table mostly lists full_code on SIG level, but sometimes it contains ones that go to EMD level
         // these are NOT included on the SIG count, so should be added up together
         // SQL query: select * from natural_disasters where full_code NOT LIKE '%00000';
         // SQL query: select * from natural_disasters where full_code like '51730%';

         /*
          *  id |         RCPTN_RGN_NM         | DST_SE_NM | count | full_code  
            ----+------------------------------+-----------+-------+------------
             36 | 강원특별자치도 횡성군              | 호우       |     1 | 5173000000
             37 | 강원특별자치도 횡성군 공근면         | 산사태     |     1 | 5173036000
             38 | 강원특별자치도 횡성군 서원면         | 산사태     |     1 | 5173037000
          */


          // ANCHOR:: POPULATION
          // population table has way too many columns: male/female age 0, age 1, ..age 80
          // should zipped into increments of 10
          // currently doesn't need to distinguish male/female, just the total population within each age group
          // age gruop: young adults (20-39), mid-age (40-59), seniors (60+)
          // TODO :: create a table -> population_simiple
          // only include age ranges, full_code, etc.

          // TODO :: create DTOs for persistent and presentation and DAOs

        return BaseApiResponse.ok(mapSearchService.getAreaDetails(landId));
    }
}
