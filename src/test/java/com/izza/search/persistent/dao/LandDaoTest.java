package com.izza.search.persistent.dao;

import com.izza.search.persistent.dto.LandCountQueryResult;
import com.izza.search.persistent.dto.query.CountLandQuery;
import com.izza.search.persistent.dto.query.LandSearchQuery;
import com.izza.search.persistent.model.Land;
import com.izza.search.presentation.dto.LongRangeDto;

import com.izza.search.vo.Point;
import com.izza.support.DatabaseTestSupport;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DisplayName("LandDao 테스트")
class LandDaoTest extends DatabaseTestSupport {

    @Autowired
    private LandDao landDao;



    @Nested
    @DisplayName("findById 메서드 테스트")
    class FindByIdTest {

        @Test
        @DisplayName("존재하는 토지 ID로 조회 시 토지 정보를 반환한다")
        void findById_ExistingId_ReturnsLand() {
            // when
            Optional<Land> result = landDao.findById("5216238");

            // then
            assertThat(result).isPresent();
            Land land = result.get();
            assertThat(land.getUniqueNo()).isEqualTo("5216238");
            assertThat(land.getBeopjungDongCode()).isEqualTo("2714010500");
            assertThat(land.getAddress()).isEqualTo("대구광역시 동구 봉무동 1564");
            assertThat(land.getLandArea()).isEqualByComparingTo(BigDecimal.valueOf(2037.00));
            assertThat(land.getOfficialLandPrice()).isEqualByComparingTo(BigDecimal.valueOf(914900L));
        }

        @Test
        @DisplayName("존재하지 않는 토지 ID로 조회 시 빈 Optional을 반환한다")
        void findById_NonExistingId_ReturnsEmpty() {
            // when
            Optional<Land> result = landDao.findById("NON-EXISTING");

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("조회된 토지는 중심점과 경계 정보를 포함한다")
        void findById_ReturnsLandWithGeometry() {
            // when
            Optional<Land> result = landDao.findById("5216238");

            // then
            assertThat(result).isPresent();
            Land land = result.get();
            assertThat(land.getCenterPoint()).isNotNull();
            assertThat(land.getCenterPoint().lng()).isCloseTo(128.63537596600568, within(0.001));
            assertThat(land.getCenterPoint().lat()).isCloseTo(35.918382496637406, within(0.001));
            assertThat(land.getBoundary()).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("findLands 메서드 테스트")
    class FindLandsTest {

        @Test
        @DisplayName("특정 지역 영역으로 데이터 조회가 잘 되는지 - 서초구 반포동 영역")
        void findLands_BySpecificRegion_ReturnsOnlyTargetRegion() {
            // given - 서초구 반포동 영역
            LandSearchQuery query = new LandSearchQuery(
                    126.99, 37.50,  // southWest (서초구 반포동 포함 영역)
                    127.03, 37.52,  // northEast  
                    1L, 100000L, 1L, 100000000L, List.of()
            );

            // when
            List<Land> results = landDao.findLands(query);

            // then
            assertThat(results).hasSize(2);
            assertThat(results).extracting(Land::getBeopjungDongCode)
                    .containsOnly("1165010700");  // 서초구 반포동만
            assertThat(results).extracting(Land::getUniqueNo)
                    .containsExactlyInAnyOrder("5030679", "5030681");
        }

        @Test
        @DisplayName("통합 쿼리 DTO로 지도 영역 내 토지를 조회한다")
        void findLands_WithMapBounds_ReturnsLands() {
            // given - 대구 동구 봉무동 영역
            LandSearchQuery query = new LandSearchQuery(
                    128.63, 35.91,  // southWest
                    128.64, 35.92,  // northEast
                    1L, 100000L, 1L, 100000000L, List.of()
            );

            // when
            List<Land> results = landDao.findLands(query);

            // then
            assertThat(results).isNotEmpty();
            assertThat(results).hasSize(2);
            assertThat(results).extracting(Land::getUniqueNo)
                    .containsExactlyInAnyOrder("5216238", "5216240");
        }

        @Test
        @DisplayName("용도지역에 맞게 잘 조회 되는지 - 공업지역 필터")
        void findLands_WithUseZoneFilter_IndustrialZone() {
            // given - 용도지역 32번(일반공업지역) 필터
            LandSearchQuery query = new LandSearchQuery(
                    null, null, null, null,  // 지도 영역 없음 (전체)
                    1L, 100000L, 1L, 100000000L,
                    List.of(32)  // 일반공업지역 필터
            );

            // when
            List<Land> results = landDao.findLands(query);

            // then
            assertThat(results).hasSize(2);
            assertThat(results).extracting(Land::getUniqueNo)
                    .containsExactlyInAnyOrder("5216238", "5216240");
            assertThat(results).extracting(Land::getUseDistrictCode1)
                    .containsOnly((short) 32);  // 일반공업지역
        }

        @Test
        @DisplayName("용도지역에 맞게 잘 조회 되는지 - 상업지역 필터")
        void findLands_WithUseZoneFilter_CommercialZone() {
            // given - 용도지역 22번(일반상업지역) 필터
            LandSearchQuery query = new LandSearchQuery(
                    null, null, null, null,  // 지도 영역 없음 (전체)
                    1L, 100000L, 1L, 100000000L,
                    List.of(22)  // 일반상업지역 필터
            );

            // when
            List<Land> results = landDao.findLands(query);

            // then
            assertThat(results).hasSize(2);
            assertThat(results).extracting(Land::getUniqueNo)
                    .containsExactlyInAnyOrder("5030679", "5030681");
            assertThat(results).extracting(Land::getUseDistrictCode1)
                    .containsOnly((short) 22);  // 일반상업지역
        }

        @Test
        @DisplayName("통합 쿼리 DTO로 면적 필터를 적용한다")
        void findLands_WithAreaFilter_ReturnsFilteredLands() {
            // given - 1000㎡ 이상 2000㎡ 이하 면적 필터
            LandSearchQuery query = new LandSearchQuery(
                    null, null, null, null,  // 지도 영역 없음
                    1000L, 2000L,  // 면적 필터 (1000-2000㎡)
                    1L, 100000000L, List.of()
            );

            // when
            List<Land> results = landDao.findLands(query);

            // then
            assertThat(results).hasSize(1);
            assertThat(results.get(0).getUniqueNo()).isEqualTo("5216240");  // 1650㎡
        }

        @Test
        @DisplayName("줌 레벨에따라서 알맞은 단위로 조회 되는지 - 개별 토지 레벨 (높은 줌)")
        void findLands_HighZoomLevel_ReturnsIndividualLands() {
            // given - 높은 줌 레벨 (개별 토지 조회)
            LandSearchQuery query = new LandSearchQuery(
                    128.63, 35.91,  // southWest (대구 동구 봉무동)
                    128.64, 35.92,  // northEast
                    1L, 100000L, 1L, 100000000L, List.of()
            );

            // when
            List<Land> results = landDao.findLands(query);

            // then - 개별 토지들이 조회됨
            assertThat(results).hasSize(2);
            assertThat(results).extracting(Land::getUniqueNo)
                    .containsExactlyInAnyOrder("5216238", "5216240");
            // 개별 토지는 고유번호가 있어야 함
            assertThat(results).allMatch(land -> land.getUniqueNo() != null);
        }

        @Test
        @DisplayName("줌 레벨에따라서 알맞은 단위로 조회 되는지 - 그룹 레벨 (낮은 줌)")
        void findLands_LowZoomLevel_ReturnsGroupedData() {
            // given - 낮은 줌 레벨 (그룹화된 데이터 조회를 위한 넓은 영역)
            LandSearchQuery query = new LandSearchQuery(
                    126.9, 37.4,   // southWest (서울 전체 포함)
                    127.1, 37.6,   // northEast  
                    1L, 100000L, 1L, 100000000L, List.of()
            );

            // when
            List<Land> results = landDao.findLands(query);

            // then - 여러 지역의 토지들이 조회됨
            assertThat(results).hasSize(4);  // 서초구 2개 + 동작구 2개
            
            // 서로 다른 법정동 코드를 가져야 함
            assertThat(results).extracting(Land::getBeopjungDongCode)
                    .contains("1165010700", "1159010500");  // 서초구 반포동, 동작구 흑석동
        }

        @Test
        @DisplayName("통합 쿼리 DTO로 복합 필터를 적용한다")
        void findLands_WithMultipleFilters_ReturnsFilteredLands() {
            // given - 공업지역 + 면적 필터 조합
            LandSearchQuery query = new LandSearchQuery(
                    null, null, null, null,  // 지도 영역 없음
                    1500L, 2500L,  // 면적 필터 (1500-2500㎡)
                    1L, 100000000L,
                    List.of(32)  // 일반공업지역 필터
            );

            // when
            List<Land> results = landDao.findLands(query);

            // then
            assertThat(results).hasSize(2);  // 5216238(2037㎡), 5216240(1650㎡)
            assertThat(results).extracting(Land::getUniqueNo)
                    .containsExactlyInAnyOrder("5216238", "5216240");
        }

        @Test
        @DisplayName("가격 단위로 잘 조회되는지 - 고가 토지 필터링")
        void findLands_WithPriceFilter_HighPriceLands() {
            // given - 고가 토지 필터 (4천만원 이상)
            LandSearchQuery query = new LandSearchQuery(
                    null, null, null, null,  // 지도 영역 없음
                    1L, 100000L,  // 면적 필터 전체 범위
                    40000000L, 50000000L,  // 가격 필터 (4000만원-5000만원)
                    List.of()
            );

            // when
            List<Land> results = landDao.findLands(query);

            // then
            assertThat(results).hasSize(2);  // 서초구 상업지역 토지 2개
            assertThat(results).extracting(Land::getUniqueNo)
                    .containsExactlyInAnyOrder("5030679", "5030681");
            assertThat(results).allMatch(land -> 
                land.getOfficialLandPrice().compareTo(BigDecimal.valueOf(40000000L)) >= 0);
        }

        @Test  
        @DisplayName("가격 단위로 잘 조회되는지 - 저가 토지 필터링")
        void findLands_WithPriceFilter_LowPriceLands() {
            // given - 저가 토지 필터 (100만원 이하)
            LandSearchQuery query = new LandSearchQuery(
                    null, null, null, null,  // 지도 영역 없음
                    1L, 100000L,  // 면적 필터 전체 범위
                    200000L, 1000000L,  // 가격 필터 (20만원-100만원)
                    List.of()
            );

            // when
            List<Land> results = landDao.findLands(query);

            // then
            assertThat(results).hasSize(3);  // 대구 공업지역 2개 + 동작구 1개
            assertThat(results).extracting(Land::getUniqueNo)
                    .containsExactlyInAnyOrder("5216238", "5216240", "5030678");
        }

        @Test
        @DisplayName("크기 단위로 잘 조회되는지 - 대형 토지 필터링")
        void findLands_WithAreaFilter_LargeLands() {
            // given - 대형 토지 필터 (2000㎡ 이상)
            LandSearchQuery query = new LandSearchQuery(
                    null, null, null, null,  // 지도 영역 없음
                    2000L, 3000L,  // 면적 필터 (2000-3000㎡)
                    1L, 100000000L, List.of()
            );

            // when
            List<Land> results = landDao.findLands(query);

            // then
            assertThat(results).hasSize(1);  // 5216238만 해당 (2037㎡)
            assertThat(results.get(0).getUniqueNo()).isEqualTo("5216238");
            assertThat(results.get(0).getLandArea()).isEqualByComparingTo(BigDecimal.valueOf(2037.00));
        }

        @Test
        @DisplayName("크기 단위로 잘 조회되는지 - 소형 토지 필터링")
        void findLands_WithAreaFilter_SmallLands() {
            // given - 소형 토지 필터 (500㎡ 이하)
            LandSearchQuery query = new LandSearchQuery(
                    null, null, null, null,  // 지도 영역 없음
                    100L, 500L,  // 면적 필터 (100-500㎡)
                    1L, 100000000L, List.of()
            );

            // when
            List<Land> results = landDao.findLands(query);

            // then
            assertThat(results).hasSize(1);  // 5030680만 해당 (112㎡)
            assertThat(results.get(0).getUniqueNo()).isEqualTo("5030680");
        }

        @Test
        @DisplayName("지도 영역 없이 전체 토지를 조회한다")
        void findLands_WithoutMapBounds_ReturnsAllLands() {
            // given
            LandSearchQuery query = new LandSearchQuery(
                    null, null, null, null,  // 지도 영역 없음
                    1L, 100000L, 1L, 100000000L, List.of()
            );

            // when
            List<Land> results = landDao.findLands(query);

            // then
            assertThat(results).hasSize(6);  // 전체 6개 토지
        }
    }

        @Test
        @DisplayName("아무 토지도 포함하지 않는 영역은 빈 결과를 반환한다")
        void findLands_EmptyArea_ReturnsEmpty() {
            // given - 아무 토지도 없는 영역 (북한 지역)
            LandSearchQuery query = new LandSearchQuery(
                    128.000, 40.000,  // southWest
                    128.010, 40.010,  // northEast
                    1L, 100000L, 1L, 100000000L, List.of()
            );

            // when
            List<Land> results = landDao.findLands(query);

            // then
            assertThat(results).isEmpty();
        }

    @Nested
    @DisplayName("findPolygonByUniqueNumber 메서드 테스트")
    class FindPolygonByUniqueNumberTest {

        @Test
        @DisplayName("토지의 폴리곤 데이터를 조회한다")
        void findPolygonByUniqueNumber_ExistingLand_ReturnsPolygon() {
            // when
            List<List<Point>> result = landDao.findPolygonByUniqueNumber("5216238");

            // then
            assertThat(result).isNotNull();
            assertThat(result).isNotEmpty();
            // 폴리곤은 최소 3개 이상의 점으로 구성되어야 함
            assertThat(result.get(0)).hasSizeGreaterThanOrEqualTo(3);
        }
    }

    @Nested
    @DisplayName("countLandsByRegions 메서드 테스트")
    class CountLandsByRegionsTest {

        @Test
        @DisplayName("특정 지역코드별 토지 개수를 조회한다")
        void countLandsByRegions_ValidQuery_ReturnsCount() {
            // given
            CountLandQuery query = new CountLandQuery(
                    List.of("11650107", "11590105"),  // 서초구 반포동, 동작구 흑석동
                    1L, 100000L, 1L, 100000000L, List.of(22, 13, 14)  // 일반상업지역, 제1종일반주거지역, 제2종일반주거지역
            );

            // when
            List<LandCountQueryResult> results = landDao.countLandsByRegions(query);

            // then
            assertThat(results).hasSize(2);
            
            // 첫 번째 지역 결과 확인
            LandCountQueryResult first = results.stream()
                    .filter(r -> r.beopjungDongCodePrefix().equals("11650107"))
                    .findFirst()
                    .orElseThrow();
            assertThat(first.count()).isEqualTo(2L); // 상업지역 토지 2개

            // 두 번째 지역 결과 확인
            LandCountQueryResult second = results.stream()
                    .filter(r -> r.beopjungDongCodePrefix().equals("11590105"))
                    .findFirst()
                    .orElseThrow();
            assertThat(second.count()).isEqualTo(2L); // 주거지역 토지 2개
        }

        @Test
        @DisplayName("면적 조건으로 특정 지역 토지 개수를 조회한다")
        void countLandsByRegions_WithAreaFilter_ReturnsFilteredCount() {
            // given
            CountLandQuery query = new CountLandQuery(
                    List.of("27140105"),  // 대구 동구 봉무동
                    1600L, 2100L,  // 1600~2100㎡ 면적 필터
                    1L, 100000000L, List.of(32)  // 일반공업지역
            );

            // when
            List<LandCountQueryResult> results = landDao.countLandsByRegions(query);

            // then
            assertThat(results).hasSize(1);
            assertThat(results.get(0).count()).isEqualTo(2L); // 공업지역 토지 2개 (면적 조건 맞음)
        }
    }
}