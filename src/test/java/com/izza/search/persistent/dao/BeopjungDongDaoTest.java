package com.izza.search.persistent.dao;

import com.izza.search.domain.ZoomLevel;
import com.izza.search.persistent.dto.query.MapSearchQuery;
import com.izza.search.persistent.model.BeopjungDong;
import com.izza.search.vo.Point;
import com.izza.support.DatabaseTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DisplayName("BeopjungDongDao 테스트")
class BeopjungDongDaoTest extends DatabaseTestSupport {

    @Autowired
    private BeopjungDongDao beopjungDongDao;


    @Nested
    @DisplayName("findAreasByZoomLevel 메서드 테스트")
    class FindAreasByZoomLevelTest {

        @Test
        @DisplayName("SIDO 레벨로 시도 단위 조회")
        void findAreasByZoomLevel_SIDOLevel_ReturnsProvinces() {
            // given - 한국 전체 영역으로 SIDO 레벨 조회
            MapSearchQuery query = new MapSearchQuery(
                    ZoomLevel.SIDO,
                    new Point(126.0, 35.0),  // southWest (한국 전체)
                    new Point(130.0, 38.0)   // northEast
            );

            // when
            List<BeopjungDong> results = beopjungDongDao.findAreasByZoomLevel(query);

            // then
            assertThat(results).isNotNull();
            if (!results.isEmpty()) {
                assertThat(results).allMatch(area -> "SIDO".equals(area.getType()));
                assertThat(results).extracting(BeopjungDong::getFullCode)
                        .contains("1100000000", "2700000000"); // 서울, 대구
            }
        }

        @Test
        @DisplayName("SIG 레벨로 시군구 단위 조회")
        void findAreasByZoomLevel_SIGLevel_ReturnsDistricts() {
            // given - 서울 지역으로 SIG 레벨 조회
            MapSearchQuery query = new MapSearchQuery(
                    ZoomLevel.SIG,
                    new Point(126.8, 37.3),  // southWest (서울 지역)
                    new Point(127.2, 37.7)   // northEast
            );

            // when
            List<BeopjungDong> results = beopjungDongDao.findAreasByZoomLevel(query);

            // then
            assertThat(results).isNotNull();
            if (!results.isEmpty()) {
                assertThat(results).allMatch(area -> "SIG".equals(area.getType()));
                assertThat(results).extracting(BeopjungDong::getFullCode)
                        .contains("1159000000", "1165000000"); // 동작구, 서초구
            }
        }

        @Test
        @DisplayName("EMD 레벨로 읍면동 단위 조회")
        void findAreasByZoomLevel_EMDLevel_ReturnsDongs() {
            // given - 서울 지역으로 EMD 레벨 조회
            MapSearchQuery query = new MapSearchQuery(
                    ZoomLevel.EMD,
                    new Point(126.9, 37.4),  // southWest (서울 특정 지역)
                    new Point(127.1, 37.6)   // northEast
            );

            // when
            List<BeopjungDong> results = beopjungDongDao.findAreasByZoomLevel(query);

            // then
            assertThat(results).isNotNull();
            if (!results.isEmpty()) {
                assertThat(results).allMatch(area -> "EMD".equals(area.getType()));
                assertThat(results).extracting(BeopjungDong::getFullCode)
                        .contains("1159010500", "1165010700"); // 흑석동, 반포동
            }
        }
    }

    @Nested  
    @DisplayName("findByFullCode 메서드 테스트")
    class FindByFullCodeTest {

        @Test
        @DisplayName("서울특별시 조회")
        void findByFullCode_Seoul_ReturnsSeoulData() {
            // when
            Optional<BeopjungDong> result = beopjungDongDao.findByFullCode("1100000000");

            // then
            assertThat(result).isPresent();
            BeopjungDong seoul = result.get();
            assertThat(seoul.getFullCode()).isEqualTo("1100000000");
            assertThat(seoul.getKoreanName()).isEqualTo("서울특별시");
            assertThat(seoul.getType()).isEqualTo("SIDO");
            assertThat(seoul.getCenterPoint()).isNotNull();
            assertThat(seoul.getCenterPoint().lng()).isCloseTo(126.99, within(0.01));
            assertThat(seoul.getCenterPoint().lat()).isCloseTo(37.55, within(0.01));
        }

        @Test
        @DisplayName("대구광역시 동구 조회")
        void findByFullCode_DaeguDongGu_ReturnsDaeguData() {
            // when
            Optional<BeopjungDong> result = beopjungDongDao.findByFullCode("2714000000");

            // then
            assertThat(result).isPresent();
            BeopjungDong donggu = result.get();
            assertThat(donggu.getFullCode()).isEqualTo("2714000000");
            assertThat(donggu.getKoreanName()).isEqualTo("대구광역시 동구");
            assertThat(donggu.getType()).isEqualTo("SIG");
        }

        @Test
        @DisplayName("서초구 반포동 조회")
        void findByFullCode_BanpoDong_ReturnsBanpoData() {
            // when
            Optional<BeopjungDong> result = beopjungDongDao.findByFullCode("1165010700");

            // then
            assertThat(result).isPresent();
            BeopjungDong banpo = result.get();
            assertThat(banpo.getFullCode()).isEqualTo("1165010700");
            assertThat(banpo.getKoreanName()).isEqualTo("서울특별시 서초구 반포동");
            assertThat(banpo.getType()).isEqualTo("EMD");
        }

        @Test
        @DisplayName("존재하지 않는 행정구역 코드로 조회 시 빈 Optional을 반환한다")
        void findByFullCode_NonExistingCode_ReturnsEmpty() {
            // when
            Optional<BeopjungDong> result = beopjungDongDao.findByFullCode("9999999999");

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findPolygonByFullCode 메서드 테스트")
    class FindPolygonByFullCodeTest {

        @Test
        @DisplayName("서울특별시 폴리곤 데이터 조회")
        void findPolygonByFullCode_Seoul_ReturnsPolygonData() {
            // when
            List<List<Point>> result = beopjungDongDao.findPolygonByFullCode("1100000000");

            // then
            assertThat(result).isNotNull();
            assertThat(result).isNotEmpty();
            assertThat(result.get(0)).hasSizeGreaterThanOrEqualTo(3);
            
            // 서울 지역의 좌표 범위 대략적 검증
            List<Point> polygon = result.get(0);
            assertThat(polygon).allMatch(point -> 
                point.lng() >= 126.0 && point.lng() <= 128.0 &&
                point.lat() >= 37.0 && point.lat() <= 38.0
            );
        }

        @Test
        @DisplayName("반포동 폴리곤 데이터 조회")
        void findPolygonByFullCode_BanpoDong_ReturnsPolygonData() {
            // when
            List<List<Point>> result = beopjungDongDao.findPolygonByFullCode("1165010700");

            // then
            assertThat(result).isNotNull();
            assertThat(result).isNotEmpty();
            assertThat(result.get(0)).hasSizeGreaterThanOrEqualTo(3);
            
            // 반포동 지역의 좌표 범위 검증
            List<Point> polygon = result.get(0);
            assertThat(polygon).allMatch(point -> 
                point.lng() >= 126.9 && point.lng() <= 127.1 &&
                point.lat() >= 37.4 && point.lat() <= 37.6
            );
        }
    }

    @Nested
    @DisplayName("행정구역 계층 구조 검증")
    class AdministrativeHierarchyTest {

        @Test
        @DisplayName("서울 행정구역 계층 구조 검증")
        void validateSeoulHierarchy() {
            // given & when
            Optional<BeopjungDong> sido = beopjungDongDao.findByFullCode("1100000000");     // 서울특별시
            Optional<BeopjungDong> sig = beopjungDongDao.findByFullCode("1165000000");      // 서초구
            Optional<BeopjungDong> emd = beopjungDongDao.findByFullCode("1165010700");      // 반포동

            // then
            assertThat(sido).isPresent();
            assertThat(sig).isPresent();
            assertThat(emd).isPresent();

            // 계층 구조 검증
            assertThat(sido.get().getType()).isEqualTo("SIDO");
            assertThat(sig.get().getType()).isEqualTo("SIG");
            assertThat(emd.get().getType()).isEqualTo("EMD");

            // 이름 계층 구조 검증
            assertThat(sig.get().getKoreanName()).contains("서울특별시");
            assertThat(emd.get().getKoreanName()).contains("서초구");
        }

        @Test
        @DisplayName("대구 행정구역 계층 구조 검증")
        void validateDaeguHierarchy() {
            // given & when
            Optional<BeopjungDong> sido = beopjungDongDao.findByFullCode("2700000000");     // 대구광역시
            Optional<BeopjungDong> sig = beopjungDongDao.findByFullCode("2714000000");      // 동구

            // then
            assertThat(sido).isPresent();
            assertThat(sig).isPresent();

            assertThat(sido.get().getKoreanName()).isEqualTo("대구광역시");
            assertThat(sig.get().getKoreanName()).isEqualTo("대구광역시 동구");
        }
    }
}