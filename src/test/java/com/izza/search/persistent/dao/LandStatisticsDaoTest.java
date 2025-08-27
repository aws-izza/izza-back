package com.izza.search.persistent.dao;

import com.izza.search.persistent.model.LandStatistics;
import com.izza.support.DatabaseTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DisplayName("LandStatisticsDao 테스트")
class LandStatisticsDaoTest extends DatabaseTestSupport {

    @Autowired
    private LandStatisticsDao landStatisticsDao;


    @Nested
    @DisplayName("findByStatType 메서드 테스트")
    class FindByStatTypeTest {

        @Test
        @DisplayName("토지 면적 통계를 조회한다")
        void findByStatType_LandArea_ReturnsStatistics() {
            // when
            Optional<LandStatistics> result = landStatisticsDao.findByStatType("LAND_AREA");

            // then
            assertThat(result).isPresent();
            LandStatistics stats = result.get();
            assertThat(stats.getStatType()).isEqualTo("LAND_AREA");
            assertThat(stats.getMinValue()).isEqualTo(112L);
            assertThat(stats.getMaxValue()).isEqualTo(2037L);
            assertThat(stats.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("공시지가 통계를 조회한다")
        void findByStatType_OfficialLandPrice_ReturnsStatistics() {
            // when
            Optional<LandStatistics> result = landStatisticsDao.findByStatType("OFFICIAL_LAND_PRICE");

            // then
            assertThat(result).isPresent();
            LandStatistics stats = result.get();
            assertThat(stats.getStatType()).isEqualTo("OFFICIAL_LAND_PRICE");
            assertThat(stats.getMinValue()).isEqualTo(293900L);
            assertThat(stats.getMaxValue()).isEqualTo(45010000L);
            assertThat(stats.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("존재하지 않는 통계 타입 조회 시 빈 Optional을 반환한다")
        void findByStatType_NonExistingType_ReturnsEmpty() {
            // when
            Optional<LandStatistics> result = landStatisticsDao.findByStatType("NON_EXISTING");

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findAll 메서드 테스트")
    class FindAllTest {

        @Test
        @DisplayName("모든 통계 데이터를 조회한다")
        void findAll_ReturnsAllStatistics() {
            // when
            List<LandStatistics> results = landStatisticsDao.findAll();

            // then
            assertThat(results).hasSize(2);
            assertThat(results).extracting(LandStatistics::getStatType)
                    .containsExactly("LAND_AREA", "OFFICIAL_LAND_PRICE");  // 정렬된 순서
            
            // 토지 면적 통계 확인
            LandStatistics landAreaStats = results.get(0);
            assertThat(landAreaStats.getMinValue()).isEqualTo(112L);
            assertThat(landAreaStats.getMaxValue()).isEqualTo(2037L);
            
            // 공시지가 통계 확인
            LandStatistics priceStats = results.get(1);
            assertThat(priceStats.getMinValue()).isEqualTo(293900L);
            assertThat(priceStats.getMaxValue()).isEqualTo(45010000L);
        }
    }

    @Nested
    @DisplayName("save 메서드 테스트")
    class SaveTest {

        @Test
        @DisplayName("새로운 통계 데이터를 저장한다")
        void save_NewStatistics_InsertsData() {
            // given
            LandStatistics newStats = LandStatistics.builder()
                    .statType("TEST_STAT")
                    .minValue(100L)
                    .maxValue(1000L)
                    .build();

            // when
            landStatisticsDao.save(newStats);

            // then
            Optional<LandStatistics> result = landStatisticsDao.findByStatType("TEST_STAT");
            assertThat(result).isPresent();
            assertThat(result.get().getMinValue()).isEqualTo(100L);
            assertThat(result.get().getMaxValue()).isEqualTo(1000L);
        }

        @Test
        @DisplayName("기존 통계 데이터를 업데이트한다")
        void save_ExistingStatistics_UpdatesData() {
            // given - 기존 데이터 업데이트
            LandStatistics updatedStats = LandStatistics.builder()
                    .statType("LAND_AREA")
                    .minValue(50L)  // 기존: 112L
                    .maxValue(3000L)  // 기존: 2037L
                    .build();

            // when
            landStatisticsDao.save(updatedStats);

            // then
            Optional<LandStatistics> result = landStatisticsDao.findByStatType("LAND_AREA");
            assertThat(result).isPresent();
            assertThat(result.get().getMinValue()).isEqualTo(50L);
            assertThat(result.get().getMaxValue()).isEqualTo(3000L);
        }
    }

    @Nested
    @DisplayName("deleteByStatType 메서드 테스트")
    class DeleteByStatTypeTest {

        @Test
        @DisplayName("통계 데이터를 삭제한다")
        void deleteByStatType_ExistingType_DeletesData() {
            // given - 삭제 전 존재 확인
            assertThat(landStatisticsDao.findByStatType("LAND_AREA")).isPresent();

            // when
            landStatisticsDao.deleteByStatType("LAND_AREA");

            // then
            assertThat(landStatisticsDao.findByStatType("LAND_AREA")).isEmpty();
            
            // 다른 데이터는 영향 받지 않음
            assertThat(landStatisticsDao.findByStatType("OFFICIAL_LAND_PRICE")).isPresent();
        }

        @Test
        @DisplayName("존재하지 않는 통계 타입 삭제 시 에러가 발생하지 않는다")
        void deleteByStatType_NonExistingType_NoError() {
            // when & then - 에러 없이 실행됨
            assertThatNoException().isThrownBy(() -> 
                landStatisticsDao.deleteByStatType("NON_EXISTING"));
        }
    }

    @Nested
    @DisplayName("통계 데이터 검증 테스트")
    class StatisticsValidationTest {

        @Test
        @DisplayName("land statistics 테스트 - 실제 데이터 범위와 일치하는지 검증")
        void validateLandStatistics_RealDataRange() {
            // given - 실제 토지 데이터 삽입
            cleanupTestData();  // 기존 데이터 정리
            insertTestBeopjungDongs();
            insertIndustrialTestLands();  // 공업지역: 1650㎡, 2037㎡
            insertCommercialTestLands();  // 상업지역: 549.7㎡, 852.5㎡  
            insertResidentialTestLands(); // 주거지역: 112㎡, 595㎡

            // when - 통계 데이터 업데이트
            insertTestStatistics("LAND_AREA", 112L, 2037L);
            insertTestStatistics("OFFICIAL_LAND_PRICE", 293900L, 45010000L);

            // then - 범위 검증
            Optional<LandStatistics> areaStats = landStatisticsDao.findByStatType("LAND_AREA");
            assertThat(areaStats).isPresent();
            assertThat(areaStats.get().getMinValue()).isEqualTo(112L);  // 최소: 5030680 (112㎡)
            assertThat(areaStats.get().getMaxValue()).isEqualTo(2037L); // 최대: 5216238 (2037㎡)

            Optional<LandStatistics> priceStats = landStatisticsDao.findByStatType("OFFICIAL_LAND_PRICE");
            assertThat(priceStats).isPresent();
            assertThat(priceStats.get().getMinValue()).isEqualTo(293900L);   // 최소: 5030678 (293,900원)
            assertThat(priceStats.get().getMaxValue()).isEqualTo(45010000L); // 최대: 5030681 (45,010,000원)
        }
    }
}