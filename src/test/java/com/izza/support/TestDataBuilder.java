package com.izza.support;

import com.izza.search.persistent.model.Land;
import com.izza.search.vo.Point;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 테스트 데이터 생성을 위한 빌더 클래스
 */
public class TestDataBuilder {

    /**
     * 기본 테스트 토지 데이터 생성
     */
    public static Land createTestLand() {
        return createTestLandBuilder().build();
    }

    /**
     * 테스트 토지 데이터 빌더
     */
    public static LandBuilder createTestLandBuilder() {
        return new LandBuilder();
    }

    public static class LandBuilder {
        private Land land = new Land();

        public LandBuilder() {
            // 기본값 설정
            land.setId(1L);
            land.setUniqueNo("TEST-001");
            land.setBeopjungDongCode("1111010100");
            land.setAddress("서울특별시 종로구 청운동 1번지");
            land.setLandArea(BigDecimal.valueOf(100.50));
            land.setOfficialLandPrice(BigDecimal.valueOf(1000000));
            land.setUseDistrictCode1((short) 110);
            land.setUseDistrictName1("제1종일반주거지역");
            land.setDataStandardDate(LocalDateTime.now());
            land.setCreatedAt(LocalDateTime.now());
            land.setUpdatedAt(LocalDateTime.now());
            
            // 기본 중심점 (서울 시청 근처)
            land.setCenterPoint(new Point(126.9780, 37.5665));
            
            // 기본 경계 (사각형)
            List<Point> boundary = new ArrayList<>();
            boundary.add(new Point(126.9770, 37.5660));
            boundary.add(new Point(126.9790, 37.5660));
            boundary.add(new Point(126.9790, 37.5670));
            boundary.add(new Point(126.9770, 37.5670));
            boundary.add(new Point(126.9770, 37.5660)); // 닫힌 폴리곤
            land.setBoundary(boundary);
        }

        public LandBuilder uniqueNo(String uniqueNo) {
            land.setUniqueNo(uniqueNo);
            return this;
        }

        public LandBuilder beopjungDongCode(String code) {
            land.setBeopjungDongCode(code);
            return this;
        }

        public LandBuilder address(String address) {
            land.setAddress(address);
            return this;
        }

        public LandBuilder landArea(BigDecimal area) {
            land.setLandArea(area);
            return this;
        }

        public LandBuilder landArea(double area) {
            land.setLandArea(BigDecimal.valueOf(area));
            return this;
        }

        public LandBuilder officialLandPrice(BigDecimal price) {
            land.setOfficialLandPrice(price);
            return this;
        }

        public LandBuilder officialLandPrice(long price) {
            land.setOfficialLandPrice(BigDecimal.valueOf(price));
            return this;
        }

        public LandBuilder useDistrictCode1(short code) {
            land.setUseDistrictCode1(code);
            return this;
        }

        public LandBuilder centerPoint(double lng, double lat) {
            land.setCenterPoint(new Point(lng, lat));
            return this;
        }

        public LandBuilder boundary(List<Point> boundary) {
            land.setBoundary(boundary);
            return this;
        }

        public Land build() {
            return land;
        }
    }

    /**
     * 서울 지역 테스트 데이터 생성
     */
    public static List<Land> createSeoulTestLands() {
        List<Land> lands = new ArrayList<>();
        
        // 종로구 토지
        lands.add(createTestLandBuilder()
                .uniqueNo("SEOUL-JONGRO-001")
                .beopjungDongCode("1111010100")
                .address("서울특별시 종로구 청운동 1번지")
                .landArea(150.0)
                .officialLandPrice(2000000L)
                .useDistrictCode1((short) 110)
                .centerPoint(126.9780, 37.5665)
                .build());

        // 강남구 토지
        lands.add(createTestLandBuilder()
                .uniqueNo("SEOUL-GANGNAM-001")
                .beopjungDongCode("1168010100")
                .address("서울특별시 강남구 역삼동 1번지")
                .landArea(200.0)
                .officialLandPrice(5000000L)
                .useDistrictCode1((short) 130)
                .centerPoint(127.0276, 37.4979)
                .build());

        return lands;
    }

    /**
     * 다양한 면적의 테스트 토지 데이터 생성
     */
    public static List<Land> createVariousAreaLands() {
        List<Land> lands = new ArrayList<>();
        
        double[] areas = {50.0, 100.0, 200.0, 500.0, 1000.0};
        long[] prices = {500000L, 1000000L, 2000000L, 5000000L, 10000000L};
        
        for (int i = 0; i < areas.length; i++) {
            lands.add(createTestLandBuilder()
                    .uniqueNo("AREA-TEST-" + String.format("%03d", i + 1))
                    .landArea(areas[i])
                    .officialLandPrice(prices[i])
                    .centerPoint(126.9780 + (i * 0.001), 37.5665 + (i * 0.001))
                    .build());
        }
        
        return lands;
    }
}