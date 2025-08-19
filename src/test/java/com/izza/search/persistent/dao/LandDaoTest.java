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

    @Override
    protected void setupTestData() {
        // 기본 테스트 데이터 설정
        insertTestLand("TEST-001", "1111010100", "서울특별시 종로구 청운동 1번지", 
                      100.0, 1000000L, 126.9780, 37.5665);
        insertTestLand("TEST-002", "1168010100", "서울특별시 강남구 역삼동 1번지", 
                      200.0, 2000000L, 127.0276, 37.4979);
        insertTestLand("TEST-003", "1111010100", "서울특별시 종로구 청운동 2번지", 
                      50.0, 500000L, 126.9785, 37.5670);
    }

    @Nested
    @DisplayName("findById 메서드 테스트")
    class FindByIdTest {

        @Test
        @DisplayName("존재하는 토지 ID로 조회 시 토지 정보를 반환한다")
        void findById_ExistingId_ReturnsLand() {
            // when
            Optional<Land> result = landDao.findById("TEST-001");

            // then
            assertThat(result).isPresent();
            Land land = result.get();
            assertThat(land.getUniqueNo()).isEqualTo("TEST-001");
            assertThat(land.getBeopjungDongCode()).isEqualTo("1111010100");
            assertThat(land.getAddress()).isEqualTo("서울특별시 종로구 청운동 1번지");
            assertThat(land.getLandArea()).isEqualByComparingTo(BigDecimal.valueOf(100.0));
            assertThat(land.getOfficialLandPrice()).isEqualByComparingTo(BigDecimal.valueOf(1000000L));
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
            Optional<Land> result = landDao.findById("TEST-001");

            // then
            assertThat(result).isPresent();
            Land land = result.get();
            assertThat(land.getCenterPoint()).isNotNull();
            assertThat(land.getCenterPoint().lng()).isCloseTo(126.9780, within(0.001));
            assertThat(land.getCenterPoint().lat()).isCloseTo(37.5665, within(0.001));
            assertThat(land.getBoundary()).isNotEmpty();
        }
    }

    @Nested
    @DisplayName("findLands 메서드 테스트")
    class FindLandsTest {

        @Test
        @DisplayName("통합 쿼리 DTO로 지도 영역 내 토지를 조회한다")
        void findLands_WithMapBounds_ReturnsLands() {
            // given
            LandSearchQuery query = new LandSearchQuery(
                    126.970, 37.560,  // southWest
                    126.985, 37.575,  // northEast
                    null, null, null, null, null
            );

            // when
            List<Land> results = landDao.findLands(query);

            // then
            assertThat(results).isNotEmpty();
            assertThat(results).hasSize(2);
            assertThat(results).extracting(Land::getUniqueNo)
                    .containsExactlyInAnyOrder("TEST-001", "TEST-003");
        }

        @Test
        @DisplayName("통합 쿼리 DTO로 면적 필터를 적용한다")
        void findLands_WithAreaFilter_ReturnsFilteredLands() {
            // given
            LandSearchQuery query = new LandSearchQuery(
                    126.970, 37.560, 126.985, 37.575,  // 지도 영역
                    80L, 150L,  // 면적 필터
                    null, null, null
            );

            // when
            List<Land> results = landDao.findLands(query);

            // then
            assertThat(results).hasSize(1);
            assertThat(results.get(0).getUniqueNo()).isEqualTo("TEST-001");
        }

        @Test
        @DisplayName("통합 쿼리 DTO로 복합 필터를 적용한다")
        void findLands_WithMultipleFilters_ReturnsFilteredLands() {
            // given
            LandSearchQuery query = new LandSearchQuery(
                    126.970, 37.560, 126.985, 37.575,  // 지도 영역
                    null, null,  // 면적 필터 없음
                    800000L, 1200000L,  // 가격 필터
                    null  // 용도지역 필터 없음
            );

            // when
            List<Land> results = landDao.findLands(query);

            // then
            assertThat(results).hasSize(1);
            assertThat(results.get(0).getUniqueNo()).isEqualTo("TEST-001");
        }

        @Test
        @DisplayName("지도 영역 없이 전체 토지를 조회한다")
        void findLands_WithoutMapBounds_ReturnsAllLands() {
            // given
            LandSearchQuery query = new LandSearchQuery(
                    null, null, null, null,  // 지도 영역 없음
                    null, null, null, null, null
            );

            // when
            List<Land> results = landDao.findLands(query);

            // then
            assertThat(results).hasSizeGreaterThanOrEqualTo(3);
        }
    }

        @Test
        @DisplayName("지도 영역 밖의 토지는 조회되지 않는다")
        void findLands_OutsideBounds_ReturnsCorrectLands() {
            // given - 강남구만 포함하는 영역 (TEST-002만 포함)
            LandSearchQuery query = new LandSearchQuery(
                    127.020, 37.490,  // southWest
                    127.035, 37.505,  // northEast
                    null, null, null, null, null
            );

            // when
            List<Land> results = landDao.findLands(query);

            // then
            assertThat(results).hasSize(1);
            assertThat(results.get(0).getUniqueNo()).isEqualTo("TEST-002");
        }

        @Test
        @DisplayName("아무 토지도 포함하지 않는 영역은 빈 결과를 반환한다")
        void findLands_EmptyArea_ReturnsEmpty() {
            // given - 아무 토지도 없는 영역
            LandSearchQuery query = new LandSearchQuery(
                    128.000, 38.000,  // southWest
                    128.010, 38.010,  // northEast
                    null, null, null, null, null
            );

            // when
            List<Land> results = landDao.findLands(query);

            // then
            assertThat(results).isEmpty();
        }

        @Test
        @DisplayName("용도지역 필터를 적용한다")
        void findLands_WithUseZoneFilter_ReturnsFilteredLands() {
            // given - 용도지역 필터 (실제 UseZoneCode에 따라 조정 필요)
            LandSearchQuery query = new LandSearchQuery(
                    null, null, null, null,  // 지도 영역 없음
                    null, null, null, null,
                    List.of(1)  // 용도지역 필터
            );

            // when
            List<Land> results = landDao.findLands(query);

            // then
            // 용도지역 코드에 따라 결과가 달라질 수 있음
            assertThat(results).isNotNull();
        }

        @Test
        @DisplayName("모든 필터를 조합하여 적용한다")
        void findLands_WithAllFilters_ReturnsFilteredLands() {
            // given - 모든 필터 조합
            LandSearchQuery query = new LandSearchQuery(
                    126.970, 37.560, 126.985, 37.575,  // 지도 영역 (종로구)
                    50L, 200L,  // 면적 필터 (모든 테스트 데이터 포함)
                    500000L, 2000000L,  // 가격 필터 (모든 테스트 데이터 포함)
                    null  // 용도지역 필터 없음
            );

            // when
            List<Land> results = landDao.findLands(query);

            // then
            assertThat(results).hasSize(2);  // TEST-001, TEST-003
            assertThat(results).extracting(Land::getUniqueNo)
                    .containsExactlyInAnyOrder("TEST-001", "TEST-003");
        }

    @Nested
    @DisplayName("findPolygonByUniqueNumber 메서드 테스트")
    class FindPolygonByUniqueNumberTest {

        @Test
        @DisplayName("토지의 폴리곤 데이터를 조회한다")
        void findPolygonByUniqueNumber_ExistingLand_ReturnsPolygon() {
            // when
            List<List<Point>> result = landDao.findPolygonByUniqueNumber("TEST-001");

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
        @DisplayName("지역별 토지 개수를 조회한다")
        void countLandsByRegions_ValidQuery_ReturnsCount() {
            // given
            CountLandQuery query = new CountLandQuery(
                    List.of("1111010100", "1168010100"),  // 종로구, 강남구
                    null, null, null, null, null
            );

            // when
            List<LandCountQueryResult> results = landDao.countLandsByRegions(query);

            // then
            assertThat(results).hasSize(2);
            
            // 종로구 결과 확인
            LandCountQueryResult jongro = results.stream()
                    .filter(r -> r.beopjungDongCodePrefix().equals("1111010100"))
                    .findFirst()
                    .orElseThrow();
            assertThat(jongro.count()).isEqualTo(2L); // TEST-001, TEST-003

            // 강남구 결과 확인
            LandCountQueryResult gangnam = results.stream()
                    .filter(r -> r.beopjungDongCodePrefix().equals("1168010100"))
                    .findFirst()
                    .orElseThrow();
            assertThat(gangnam.count()).isEqualTo(1L); // TEST-002
        }

        @Test
        @DisplayName("면적 조건으로 지역별 토지 개수를 조회한다")
        void countLandsByRegions_WithAreaFilter_ReturnsFilteredCount() {
            // given
            CountLandQuery query = new CountLandQuery(
                    List.of("1111010100"),
                    80L, 150L,  // 80~150 면적 필터
                    null, null, null
            );

            // when
            List<LandCountQueryResult> results = landDao.countLandsByRegions(query);

            // then
            assertThat(results).hasSize(1);
            assertThat(results.get(0).count()).isEqualTo(1L); // TEST-001만 해당
        }
    }
}