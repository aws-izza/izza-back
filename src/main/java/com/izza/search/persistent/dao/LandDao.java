package com.izza.search.persistent.dao;

import com.izza.search.persistent.dto.LandCountQueryResult;
import com.izza.search.persistent.dto.query.CountLandQuery;
import com.izza.search.persistent.dto.query.FullCodeLandCountQuery;
import com.izza.search.persistent.dto.query.FullCodeLandSearchQuery;
import com.izza.search.persistent.dto.query.LandSearchQuery;
import com.izza.search.persistent.model.Land;
import com.izza.utils.GisUtils;
import com.izza.utils.ResultSetUtils;
import com.izza.utils.SqlConditionUtils;
import com.izza.search.presentation.dto.LongRangeDto;
import com.izza.search.vo.Point;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
@Slf4j
public class LandDao {

    private final JdbcTemplate jdbcTemplate;

    public LandDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 토지 검색 (통합 쿼리 DTO 사용) - land_gis와 JOIN
     */
    public List<Land> findLands(LandSearchQuery query) {
        StringBuilder sqlBuilder = new StringBuilder();
        String sql = """
                  SELECT l.*,
                  ST_AsText(lg.boundary) as boundary_wkt,
                  ST_X(lg.center_point) as center_lng,
                  ST_Y(lg.center_point) as center_lat
                  FROM land l
                  LEFT JOIN land_gis lg ON l.id = lg.land_id
                  WHERE 1=1
                """;
        sqlBuilder.append(sql);

        List<Object> params = new ArrayList<>();

        // 지도 영역 필터링 (center_point 기준)
        if (query.hasMapBounds()) {
            sqlBuilder.append(" AND ST_Contains(ST_MakeEnvelope(?, ?, ?, ?, 4326), lg.center_point)");
            params.add(query.southWestLng());
            params.add(query.southWestLat());
            params.add(query.northEastLng());
            params.add(query.northEastLat());
        }

        // 용도지역 카테고리 필터링 (IN 쿼리 사용)
        SqlConditionUtils.in(sqlBuilder, params, "l.use_zone_category", query.useZoneCategories());

        // 토지 면적 필터링
        SqlConditionUtils.between(sqlBuilder, params,
                "l.land_area",
                BigDecimal.valueOf(query.landAreaMin()),
                BigDecimal.valueOf(query.landAreaMax()));

        // 공시지가 필터링
        SqlConditionUtils.between(sqlBuilder, params,
                "l.official_land_price",
                BigDecimal.valueOf(query.officialLandPriceMin()),
                BigDecimal.valueOf(query.officialLandPriceMax()));

        // 제외할 토지 이용 코드 필터링
        sqlBuilder.append(" AND l.land_use_code NOT IN (910, 920, 930, 940, 950, 960, 970, 990, 850, 860, 870, 880, 881, 890, 891, 892, 893)");

        return jdbcTemplate.query(sqlBuilder.toString(), new LandRowMapper(), params.toArray());
    }

    public List<LandCountQueryResult> countLandsByRegions(CountLandQuery query) {
        if (query.fullCodePrefixes().isEmpty()) {
            return new ArrayList<>();
        }

        int prefixLength = query.fullCodePrefixes().getFirst().length();

        // 시도 레벨(길이 2)일 때 prefix sum 테이블 사용
        if (prefixLength == 2) {
            return countLandsByRegionsWithPrefixSum(query);
        }

        // 기존 로직 (길이 2가 아닌 경우)
        return countLandsByRegionsLegacy(query);
    }

    private List<LandCountQueryResult> countLandsByRegionsWithPrefixSum(CountLandQuery query) {
        int areaBucketMin = Math.max(0, (int) (query.landAreaMin() / 500));
        int areaBucketMax = Math.min(1999, (int) (query.landAreaMax() / 500));
        int priceBucketMin = Math.max(0, (int) (query.officialLandPriceMin() / 500000));
        int priceBucketMax = Math.min(360, (int) (query.officialLandPriceMax() / 500000));

        Map<String, Long> regionCountMap = new HashMap<>();

        // 각 지역별, 카테고리별로 개별 계산 후 합산
        for (String regionCode : query.fullCodePrefixes()) {
            long totalCountForRegion = 0;
            
            for (String useZoneCategory : query.useZoneCategories()) {
                // UNION ALL로 p1, p2, p3, p4를 한번에 조회
                StringBuilder unionSql = new StringBuilder();
                unionSql.append("SELECT 'p1' as query_name, COALESCE(cumulative_count, 0) as count_value ")
                        .append("FROM land_statistics_prefix_sum ")
                        .append("WHERE key_prefix = ? AND use_zone_category = ? AND area_bucket = ? AND price_bucket = ? ");
                
                if (areaBucketMin > 0) {
                    unionSql.append("UNION ALL ")
                            .append("SELECT 'p2' as query_name, COALESCE(cumulative_count, 0) as count_value ")
                            .append("FROM land_statistics_prefix_sum ")
                            .append("WHERE key_prefix = ? AND use_zone_category = ? AND area_bucket = ? AND price_bucket = ? ");
                }
                
                if (priceBucketMin > 0) {
                    unionSql.append("UNION ALL ")
                            .append("SELECT 'p3' as query_name, COALESCE(cumulative_count, 0) as count_value ")
                            .append("FROM land_statistics_prefix_sum ")
                            .append("WHERE key_prefix = ? AND use_zone_category = ? AND area_bucket = ? AND price_bucket = ? ");
                }
                
                if (areaBucketMin > 0 && priceBucketMin > 0) {
                    unionSql.append("UNION ALL ")
                            .append("SELECT 'p4' as query_name, COALESCE(cumulative_count, 0) as count_value ")
                            .append("FROM land_statistics_prefix_sum ")
                            .append("WHERE key_prefix = ? AND use_zone_category = ? AND area_bucket = ? AND price_bucket = ? ");
                }

                List<Object> params = new ArrayList<>();
                // p1 파라미터
                params.add(regionCode);
                params.add(useZoneCategory);
                params.add(areaBucketMax);
                params.add(priceBucketMax);
                
                // p2 파라미터 (areaBucketMin > 0일 때만)
                if (areaBucketMin > 0) {
                    params.add(regionCode);
                    params.add(useZoneCategory);
                    params.add(areaBucketMin - 1);
                    params.add(priceBucketMax);
                }
                
                // p3 파라미터 (priceBucketMin > 0일 때만)
                if (priceBucketMin > 0) {
                    params.add(regionCode);
                    params.add(useZoneCategory);
                    params.add(areaBucketMax);
                    params.add(priceBucketMin - 1);
                }
                
                // p4 파라미터 (둘 다 > 0일 때만)
                if (areaBucketMin > 0 && priceBucketMin > 0) {
                    params.add(regionCode);
                    params.add(useZoneCategory);
                    params.add(areaBucketMin - 1);
                    params.add(priceBucketMin - 1);
                }

                // 쿼리 실행 및 결과 파싱
                List<Map<String, Object>> results = jdbcTemplate.queryForList(unionSql.toString(), params.toArray());
                
                long p1Count = 0L, p2Count = 0L, p3Count = 0L, p4Count = 0L;
                for (Map<String, Object> row : results) {
                    String queryName = (String) row.get("query_name");
                    Long countValue = ((Number) row.get("count_value")).longValue();
                    
                    switch (queryName) {
                        case "p1" -> p1Count = countValue;
                        case "p2" -> p2Count = countValue;
                        case "p3" -> p3Count = countValue;
                        case "p4" -> p4Count = countValue;
                    }
                }
                
                // 2D prefix sum 공식: p1 - p2 - p3 + p4
                long categoryCount = p1Count - p2Count - p3Count + p4Count;
                
                log.debug("지역: {}, 카테고리: {}, p1: {}, p2: {}, p3: {}, p4: {}, 결과: {}", 
                        regionCode, useZoneCategory, p1Count, p2Count, p3Count, p4Count, categoryCount);
                
                if (categoryCount > 0) {
                    totalCountForRegion += categoryCount;
                }
            }
            
            regionCountMap.put(regionCode, totalCountForRegion);
        }

        return regionCountMap.entrySet().stream()
                .map(entry -> new LandCountQueryResult(entry.getKey(), entry.getValue()))
                .toList();
    }

    private List<LandCountQueryResult> countLandsByRegionsLegacy(CountLandQuery query) {
        int prefixLength = query.fullCodePrefixes().getFirst().length();

        StringBuilder sql = new StringBuilder();
        List<Object> params = new ArrayList<>();

        Map<String, Long> map = new HashMap<>();

        for (String useZoneCategory : query.useZoneCategories()) {
            String leftQuery = "LEFT(full_code, " + prefixLength + ") ";
            sql.append("SELECT " + leftQuery + "as region_code, COUNT(*) as land_count ")
                    .append("FROM land WHERE 1=1 ");

            SqlConditionUtils.in(sql, params, leftQuery, query.fullCodePrefixes());
            sql.append(" AND use_zone_category = ?");
            params.add(useZoneCategory);

            SqlConditionUtils.between(sql, params,
                    "land_area",
                    BigDecimal.valueOf(query.landAreaMin()),
                    BigDecimal.valueOf(query.landAreaMax()));

            SqlConditionUtils.between(sql, params,
                    "official_land_price",
                    BigDecimal.valueOf(query.officialLandPriceMin()),
                    BigDecimal.valueOf(query.officialLandPriceMax()));

            sql.append(" GROUP BY ").append(leftQuery);

            List<LandCountQueryResult> queryResult = jdbcTemplate.query(sql.toString(), (rs, rowNum) -> new LandCountQueryResult(
                    rs.getString("region_code"),
                    rs.getLong("land_count")), params.toArray());

            for (LandCountQueryResult result : queryResult) {
                Long value = map.getOrDefault(result.beopjungDongCodePrefix(), 0L);
                value += result.count();
                map.put(result.beopjungDongCodePrefix(), value);
            }

            sql = new StringBuilder();
            params.clear();
        }

        List<LandCountQueryResult> results = new ArrayList<>();
        for (Map.Entry<String, Long> entry : map.entrySet()) {
            String key = entry.getKey();
            Long value = entry.getValue();
            results.add(new LandCountQueryResult(key, value));
        }

        return results;
    }

    /**
     * ID로 토지 상세 정보 조회 - land_gis와 JOIN
     */
    public Optional<Land> findById(Long id) {
        String sql = """
                SELECT l.*,
                ST_AsText(lg.boundary) as boundary_wkt,
                ST_X(lg.center_point) as center_lng,
                ST_Y(lg.center_point) as center_lat
                FROM land l
                LEFT JOIN land_gis lg ON l.id = lg.land_id
                WHERE l.id = ?
                """;
        List<Land> results = jdbcTemplate.query(sql, new LandRowMapper(), id);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    /**
     * 토지 폴리곤 데이터 조회 (멀티폴리곤 지원)
     */
    public List<List<Point>> findPolygonByUniqueNumber(String id) {
        String sql = "SELECT ST_AsText(ST_Transform(boundary, 4326)) as boundary_wkt FROM land WHERE unique_no = ?";
        List<List<Point>> results = jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
            String wkt = rs.getString("boundary_wkt");
            return GisUtils.parsePolygonToMultiPointList(wkt);
        }, id);
        return results;
    }

    /**
     * 토지 면적의 최소값과 최대값 조회 (소수점 올림 처리)
     */
    public LongRangeDto getLandAreaRange() {
        String sql = "SELECT MIN(CEIL(land_area)) as min_area, MAX(CEIL(land_area)) as max_area FROM land WHERE land_area IS NOT NULL";
        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
            long min = rs.getLong("min_area");
            long max = rs.getLong("max_area");
            return new LongRangeDto(min, max);
        });
    }

    /**
     * 지역별 토지 면적의 최소값과 최대값 조회
     */
    public LongRangeDto getLandAreaRangeByRegion(String regionCode) {
        String sql = "SELECT MIN(FLOOR(land_area)) as min_area, MAX(CEIL(land_area)) as max_area FROM land WHERE LEFT(full_code, 5) = ? AND land_area IS NOT NULL";
        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
            long min = rs.getLong("min_area");
            long max = rs.getLong("max_area");
            return new LongRangeDto(min, max);
        }, regionCode);
    }

    /**
     * 지역별 공시지가의 최소값과 최대값 조회
     */
    public LongRangeDto getOfficialLandPriceRangeByRegion(String regionCode) {
        String sql = "SELECT MIN(FLOOR(official_land_price)) as min_price, MAX(CEIL(official_land_price)) as max_price FROM land WHERE LEFT(full_code, 5) = ? AND official_land_price IS NOT NULL";
        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
            long min = rs.getLong("min_price");
            long max = rs.getLong("max_price");
            return new LongRangeDto(min, max);
        }, regionCode);
    }

    /**
     * fullCode와 범위 조건으로 토지 목록 조회
     */
    public Long countLandsByFullCode(FullCodeLandCountQuery query) {
        int fullCodeLength = query.fullCode().length();
        String leftQuery = "LEFT(full_code, " + fullCodeLength + ") = ? ";

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(*) ")
                .append("FROM land WHERE ").append(leftQuery);

        List<Object> params = new ArrayList<>();
        params.add(query.fullCode());

        SqlConditionUtils.in(sql, params, "use_zone_category", query.useZoneCategories());

        // 토지 면적 필터
        SqlConditionUtils.between(sql, params,
                "land_area",
                BigDecimal.valueOf(query.landAreaMin()),
                BigDecimal.valueOf(query.landAreaMax()));

        // 공시지가 필터
        SqlConditionUtils.between(sql, params,
                "official_land_price",
                BigDecimal.valueOf(query.officialLandPriceMin()),
                BigDecimal.valueOf(query.officialLandPriceMax()));

        // 제외할 토지 이용 코드 필터링
        sql.append(" AND land_use_code NOT IN (910, 920, 930, 940, 950, 960, 970, 990, 850, 860, 870, 880, 881, 890, 891, 892, 893)");

        return jdbcTemplate.queryForObject(sql.toString(), Long.class, params.toArray());
    }
    
    /**
     * fullCode와 범위 조건으로 토지 목록 조회 (전체 데이터를 2000건씩 페이지네이션)
     */
    public List<Land> findLandsByFullCode(FullCodeLandSearchQuery query) {
        int fullCodeLength = query.fullCode().length();
        String leftQuery = "LEFT(full_code, " + fullCodeLength + ") = ? ";
        
        List<Land> allResults = new ArrayList<>();
        Long lastId = 0L;
        int batchSize = 2000;
        
        while (true) {
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT * ")
               .append("FROM land WHERE ").append(leftQuery)
               .append(" AND id > ? ");

            List<Object> params = new ArrayList<>();
            params.add(query.fullCode());
            params.add(lastId);

            SqlConditionUtils.eq(sql, params, "use_zone_category", query.useZoneCategories());

            // 토지 면적 필터
            SqlConditionUtils.between(sql, params,
                    "land_area",
                    BigDecimal.valueOf(query.landAreaMin()),
                    BigDecimal.valueOf(query.landAreaMax()));

            // 공시지가 필터
            SqlConditionUtils.between(sql, params,
                    "official_land_price",
                    BigDecimal.valueOf(query.officialLandPriceMin()),
                    BigDecimal.valueOf(query.officialLandPriceMax()));

            // 제외할 토지 이용 코드 필터링
            sql.append(" AND land_use_code NOT IN (910, 920, 930, 940, 950, 960, 970, 990, 850, 860, 870, 880, 881, 890, 891, 892, 893)");
            
            sql.append(" ORDER BY id LIMIT ?");
            params.add(batchSize);

            List<Land> batchResults = jdbcTemplate.query(sql.toString(), new LandRowMapper(), params.toArray());
            
            if (batchResults.isEmpty()) {
                break;
            }
            
            allResults.addAll(batchResults);
            lastId = batchResults.get(batchResults.size() - 1).getId();
            
            if (batchResults.size() < batchSize) {
                break;
            }
        }
        
        return allResults;
    }

    /**
     * 주소로 토지 검색 (정확히 일치)
     */
    public Optional<Land> findByAddress(String address) {
        String sql = """
                SELECT l.*,
                ST_AsText(lg.boundary) as boundary_wkt,
                ST_X(lg.center_point) as center_lng,
                ST_Y(lg.center_point) as center_lat
                FROM land l
                LEFT JOIN land_gis lg ON l.id = lg.land_id
                WHERE l.address = ?
                """;
        List<Land> results = jdbcTemplate.query(sql, new LandRowMapper(), address);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }


    /**
     * Land 엔티티 RowMapper
     */
    private static class LandRowMapper implements RowMapper<Land> {
        @Override
        public Land mapRow(ResultSet rs, int rowNum) throws SQLException {
            Land land = new Land();

            // 기본 필드들을 ResultSetUtils를 사용하여 안전하게 설정
            ResultSetUtils.getLongSafe(rs, "id").ifPresent(land::setId);
            ResultSetUtils.getLongSafe(rs, "shape_id").ifPresent(land::setShapeId);
            ResultSetUtils.getStringSafe(rs, "unique_no").ifPresent(land::setUniqueNo);
            ResultSetUtils.getStringSafe(rs, "full_code").ifPresent(land::setBeopjungDongCode);
            ResultSetUtils.getStringSafe(rs, "address").ifPresent(land::setAddress);

            ResultSetUtils.getShortSafe(rs, "ledger_division_code").ifPresent(land::setLedgerDivisionCode);
            ResultSetUtils.getStringSafe(rs, "ledger_division_name").ifPresent(land::setLedgerDivisionName);
            ResultSetUtils.getShortSafe(rs, "base_year").ifPresent(land::setBaseYear);
            ResultSetUtils.getShortSafe(rs, "base_month").ifPresent(land::setBaseMonth);

            ResultSetUtils.getShortSafe(rs, "land_category_code").ifPresent(land::setLandCategoryCode);
            ResultSetUtils.getStringSafe(rs, "land_category_name").ifPresent(land::setLandCategoryName);
            ResultSetUtils.getBigDecimalSafe(rs, "land_area").ifPresent(land::setLandArea);

            ResultSetUtils.getShortSafe(rs, "use_district_code1").ifPresent(land::setUseDistrictCode1);
            ResultSetUtils.getStringSafe(rs, "use_district_name1").ifPresent(land::setUseDistrictName1);

            ResultSetUtils.getShortSafe(rs, "land_use_code").ifPresent(land::setLandUseCode);
            ResultSetUtils.getStringSafe(rs, "land_use_name").ifPresent(land::setLandUseName);
            ResultSetUtils.getShortSafe(rs, "terrain_height_code").ifPresent(land::setTerrainHeightCode);
            ResultSetUtils.getStringSafe(rs, "terrain_height_name").ifPresent(land::setTerrainHeightName);
            ResultSetUtils.getShortSafe(rs, "terrain_shape_code").ifPresent(land::setTerrainShapeCode);
            ResultSetUtils.getStringSafe(rs, "terrain_shape_name").ifPresent(land::setTerrainShapeName);
            ResultSetUtils.getShortSafe(rs, "road_side_code").ifPresent(land::setRoadSideCode);
            ResultSetUtils.getStringSafe(rs, "road_side_name").ifPresent(land::setRoadSideName);
            ResultSetUtils.getStringSafe(rs, "use_zone_category").ifPresent(land::setUseZoneCategory);

            ResultSetUtils.getBigDecimalSafe(rs, "official_land_price").ifPresent(land::setOfficialLandPrice);

            // Timestamp를 LocalDateTime으로 변환
            ResultSetUtils.getTimestampSafe(rs, "data_standard_date")
                    .ifPresent(timestamp -> land.setDataStandardDate(timestamp.toLocalDateTime()));


            // PostGIS Geometry를 Point 리스트로 파싱
            String boundaryWkt = ResultSetUtils.getStringSafe(rs, "boundary_wkt").orElse(null);
            if (boundaryWkt != null && !boundaryWkt.trim().isEmpty()) {
                land.setBoundary(GisUtils.parsePolygonToPointList(boundaryWkt));
            } else {
                land.setBoundary(new ArrayList<>());
            }

            // 중심점 설정 (미리 계산된 center_point 사용)
            Optional<Double> centerLng = ResultSetUtils.getDoubleSafe(rs, "center_lng");
            Optional<Double> centerLat = ResultSetUtils.getDoubleSafe(rs, "center_lat");
            if (centerLng.isPresent() && centerLat.isPresent()) {
                land.setCenterPoint(new Point(centerLng.get(), centerLat.get()));
            }

            return land;
        }
    }
}