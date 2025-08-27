//package com.izza.search.integration;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.izza.support.DatabaseTestSupport;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Nested;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.setup.MockMvcBuilders;
//import org.springframework.web.context.WebApplicationContext;
//
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//import static org.hamcrest.Matchers.*;
//
//@SpringBootTest
//@AutoConfigureWebMvc
//@DisplayName("토지 검색 통합 테스트")
//class SearchIntegrationTest extends DatabaseTestSupport {
//
//    @Autowired
//    private WebApplicationContext webApplicationContext;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    private MockMvc mockMvc;
//
//    @Override
//    protected void setupTestData() {
//        // 기본 테스트 데이터 설정
//        insertTestLand("TEST-001", "1111010100", "서울특별시 종로구 청운동 1번지",
//                      100.0, 1000000L, 126.9780, 37.5665);
//        insertTestLand("TEST-002", "1168010100", "서울특별시 강남구 역삼동 1번지",
//                      200.0, 2000000L, 127.0276, 37.4979);
//        insertTestLand("TEST-003", "1111010100", "서울특별시 종로구 청운동 2번지",
//                      50.0, 500000L, 126.9785, 37.5670);
//
//        // 통계 데이터 삽입
//        insertTestStatistics("land_area_range", 50L, 200L);
//        insertTestStatistics("official_land_price_range", 500000L, 2000000L);
//
//        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
//    }
//
//    @Nested
//    @DisplayName("기본 정보 API")
//    class BaseInfoApiTest {
//
//        @Test
//        @DisplayName("토지 면적 범위 조회")
//        void getLandAreaRange() throws Exception {
//            mockMvc.perform(get("/api/v1/base-info/land-area-range"))
//                    .andExpect(status().isOk())
//                    .andExpect(jsonPath("$.success").value(true))
//                    .andExpect(jsonPath("$.data.min").value(50))
//                    .andExpect(jsonPath("$.data.max").value(200));
//        }
//
//        @Test
//        @DisplayName("공시지가 범위 조회")
//        void getOfficialLandPriceRange() throws Exception {
//            mockMvc.perform(get("/api/v1/base-info/official-land-price-range"))
//                    .andExpect(status().isOk())
//                    .andExpect(jsonPath("$.success").value(true))
//                    .andExpect(jsonPath("$.data.min").value(500000))
//                    .andExpect(jsonPath("$.data.max").value(2000000));
//        }
//
//        @Test
//        @DisplayName("기업 적합 용도지역 카테고리 조회")
//        void getEnterpriseUseZoneCategories() throws Exception {
//            mockMvc.perform(get("/api/v1/base-info/use-zone-categories"))
//                    .andExpect(status().isOk())
//                    .andExpect(jsonPath("$.success").value(true))
//                    .andExpect(jsonPath("$.data").isArray())
//                    .andExpect(jsonPath("$.data", hasSize(greaterThan(0))));
//        }
//    }
//
//    @Nested
//    @DisplayName("지도 검색 API")
//    class MapSearchApiTest {
//
//        @Test
//        @DisplayName("지도 영역으로 토지 검색 - 기본 조회")
//        void searchLandsInMapBounds() throws Exception {
//            mockMvc.perform(get("/api/v1/land-search/points")
//                    .param("southWestLng", "126.970")
//                    .param("southWestLat", "37.560")
//                    .param("northEastLng", "126.985")
//                    .param("northEastLat", "37.575")
//                    .param("zoomLevel", "14")
//                    .param("landAreaMin", "1")
//                    .param("landAreaMax", "1000000")
//                    .param("officialLandPriceMin", "1")
//                    .param("officialLandPriceMax", "10000000"))
//                    .andExpect(status().isOk())
//                    .andExpect(jsonPath("$.success").value(true))
//                    .andExpect(jsonPath("$.data").isArray());
//        }
//
//        @Test
//        @DisplayName("지도 영역으로 토지 검색 - 면적 필터")
//        void searchLandsWithAreaFilter() throws Exception {
//            mockMvc.perform(get("/api/v1/land-search/points")
//                    .param("southWestLng", "126.970")
//                    .param("southWestLat", "37.560")
//                    .param("northEastLng", "126.985")
//                    .param("northEastLat", "37.575")
//                    .param("zoomLevel", "14")
//                    .param("landAreaMin", "80")
//                    .param("landAreaMax", "150")
//                    .param("officialLandPriceMin", "1")
//                    .param("officialLandPriceMax", "10000000"))
//                    .andExpect(status().isOk())
//                    .andExpect(jsonPath("$.success").value(true))
//                    .andExpect(jsonPath("$.data").isArray());
//        }
//
//        @Test
//        @DisplayName("지도 영역으로 토지 검색 - 가격 필터")
//        void searchLandsWithPriceFilter() throws Exception {
//            mockMvc.perform(get("/api/v1/land-search/points")
//                    .param("southWestLng", "126.970")
//                    .param("southWestLat", "37.560")
//                    .param("northEastLng", "126.985")
//                    .param("northEastLat", "37.575")
//                    .param("zoomLevel", "14")
//                    .param("landAreaMin", "1")
//                    .param("landAreaMax", "1000000")
//                    .param("officialLandPriceMin", "800000")
//                    .param("officialLandPriceMax", "1200000"))
//                    .andExpect(status().isOk())
//                    .andExpect(jsonPath("$.success").value(true))
//                    .andExpect(jsonPath("$.data").isArray());
//        }
//
//        @Test
//        @DisplayName("전체 영역 토지 검색")
//        void searchAllLands() throws Exception {
//            mockMvc.perform(get("/api/v1/land-search/points")
//                    .param("zoomLevel", "14")
//                    .param("landAreaMin", "1")
//                    .param("landAreaMax", "1000000")
//                    .param("officialLandPriceMin", "1")
//                    .param("officialLandPriceMax", "10000000"))
//                    .andExpect(status().isOk())
//                    .andExpect(jsonPath("$.success").value(true))
//                    .andExpect(jsonPath("$.data").isArray());
//        }
//    }
//
//    @Nested
//    @DisplayName("토지 상세 정보 API")
//    class LandDetailApiTest {
//
//        @Test
//        @DisplayName("토지 ID로 상세 정보 조회")
//        void getLandById() throws Exception {
//            mockMvc.perform(get("/api/v1/land-search/land/{landId}", "TEST-001"))
//                    .andExpect(status().isOk())
//                    .andExpect(jsonPath("$.success").value(true))
//                    .andExpect(jsonPath("$.data.uniqueNo").value("TEST-001"))
//                    .andExpect(jsonPath("$.data.beopjungDongCode").value("1111010100"))
//                    .andExpect(jsonPath("$.data.address").value("서울특별시 종로구 청운동 1번지"))
//                    .andExpect(jsonPath("$.data.landArea").value(100.0))
//                    .andExpect(jsonPath("$.data.officialLandPrice").value(1000000));
//        }
//
//        @Test
//        @DisplayName("존재하지 않는 토지 ID 조회")
//        void getLandById_NotFound() throws Exception {
//            mockMvc.perform(get("/api/v1/land-search/land/{landId}", "NON-EXISTING"))
//                    .andExpect(status().isNotFound())
//                    .andExpect(jsonPath("$.success").value(false));
//        }
//
//        @Test
//        @DisplayName("토지 폴리곤 데이터 조회")
//        void getLandPolygon() throws Exception {
//            mockMvc.perform(get("/api/v1/land-search/polygon/{id}", "TEST-001")
//                    .param("polygonType", "land"))
//                    .andExpect(status().isOk())
//                    .andExpect(jsonPath("$.success").value(true))
//                    .andExpect(jsonPath("$.data").exists());
//        }
//    }
//
////    @Nested
////    @DisplayName("토지 행정구역 API")
////    class LandAreaApiTest {
////
////        @Test
////        @DisplayName("토지의 행정구역 상세 정보 조회")
////        void getAreaDetails() throws Exception {
////            mockMvc.perform(get("/api/v1/land-search/area/{landId}", "TEST-001"))
////                    .andExpect(status().isOk())
////                    .andExpect(jsonPath("$.success").value(true))
////                    .andExpect(jsonPath("$.data").exists());
////        }
////    }
//}