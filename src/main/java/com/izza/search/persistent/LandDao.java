package com.izza.search.persistent;

import com.izza.search.persistent.dto.LandCountQueryResult;
import com.izza.search.persistent.query.CountLandQuery;
import com.izza.search.persistent.utils.GisUtils;
import com.izza.search.persistent.utils.ResultSetUtils;
import com.izza.search.persistent.utils.SqlConditionUtils;
import com.izza.search.presentation.dto.request.LandSearchFilterRequest;
import com.izza.search.presentation.dto.LongRangeDto;
import com.izza.search.presentation.dto.request.MapSearchRequest;
import com.izza.search.vo.Point;
import com.izza.search.vo.UseZoneCode;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class LandDao {

    private final JdbcTemplate jdbcTemplate;

    public LandDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 지도 영역 내 토지 검색 (필터 조건 포함)
     */
    public List<Land> findLandsInMapBounds(MapSearchRequest mapRequest, LandSearchFilterRequest filterRequest) {
        StringBuilder sqlBuilder = new StringBuilder();
        String sql = """
                  SELECT *,
                  ST_AsText(boundary) as boundary_wkt,\s
                  ST_X(center_point) as center_lng,\s
                  ST_Y(center_point) as center_lat\s
                  FROM land WHERE 1=1\s
                """;
        sqlBuilder.append(sql);

        List<Object> params = new ArrayList<>();

        // 지도 영역 필터링 (4326 좌표계로 직접 비교)
        if (mapRequest.southWestLat() != null && mapRequest.southWestLng() != null &&
                mapRequest.northEastLat() != null && mapRequest.northEastLng() != null) {
            sqlBuilder.append("AND ST_Intersects(boundary, ST_MakeEnvelope(?, ?, ?, ?, 4326)) ");
            params.add(mapRequest.southWestLng());
            params.add(mapRequest.southWestLat());
            params.add(mapRequest.northEastLng());
            params.add(mapRequest.northEastLat());
        }

        // 토지 면적 필터링 (Long 타입으로 변경)
        if (filterRequest.landAreaMin() != null) {
            sqlBuilder.append("AND land_area >= ? ");
            params.add(filterRequest.landAreaMin());
        }
        if (filterRequest.landAreaMax() != null) {
            sqlBuilder.append("AND land_area <= ? ");
            params.add(filterRequest.landAreaMax());
        }

        // 공시지가 필터링 (Long 타입으로 변경)
        if (filterRequest.officialLandPriceMin() != null) {
            sqlBuilder.append("AND official_land_price >= ? ");
            params.add(filterRequest.officialLandPriceMin());
        }
        if (filterRequest.officialLandPriceMax() != null) {
            sqlBuilder.append("AND official_land_price <= ? ");
            params.add(filterRequest.officialLandPriceMax());
        }

        // 용도지역 카테고리 필터링
        addUseZoneCategoryFilters(sqlBuilder, params, filterRequest);

        sqlBuilder.append("ORDER BY id LIMIT 1000");

        return jdbcTemplate.query(sqlBuilder.toString(), new LandRowMapper(), params.toArray());
    }

    /**
     * ID로 토지 상세 정보 조회
     */
    public Optional<Land> findById(String id) {
        String sql = "SELECT *, " +
                "ST_AsText(ST_Transform(boundary, 4326)) as boundary_wkt, " +
                "ST_X(ST_Transform(ST_Centroid(boundary), 4326)) as center_lng, " +
                "ST_Y(ST_Transform(ST_Centroid(boundary), 4326)) as center_lat " +
                "FROM land WHERE unique_no = ?";
        List<Land> results = jdbcTemplate.query(sql, new LandRowMapper(), id);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    /**
     * full_code로 토지 상세 정보 조회
     */
    public Optional<Land> findByFullCode(String fullCode) {
        String sql = "SELECT *, " +
                "ST_AsText(ST_Transform(boundary, 4326)) as boundary_wkt, " +
                "ST_X(ST_Transform(ST_Centroid(boundary), 4326)) as center_lng, " +
                "ST_Y(ST_Transform(ST_Centroid(boundary), 4326)) as center_lat " +
                "FROM land WHERE full_code = ?";
        List<Land> results = jdbcTemplate.query(sql, new LandRowMapper(), fullCode);
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

    public List<LandCountQueryResult> countLandsByRegions(CountLandQuery query) {
        List<String> unionQueries = new ArrayList<>();
        List<Object> params = new ArrayList<>();

        for (String prefix : query.fullCodePrefixes()) {
            StringBuilder subQuery = new StringBuilder();
            subQuery.append("SELECT '").append(prefix)
                    .append("' as region_code, COUNT(*) as land_count FROM land WHERE full_code LIKE ? ");

            params.add(prefix + "%");

            // SqlConditionUtils를 사용한 조건들 추가 (BETWEEN 사용)
            SqlConditionUtils.between(subQuery, params,
                    "land_area", query.landAreaMin(), query.landAreaMax());
            SqlConditionUtils.between(subQuery, params,
                    "official_land_price", query.officialLandPriceMin(), query.officialLandPriceMax());
            SqlConditionUtils.in(subQuery, params, "use_district_code1", query.useZoneIds());

            unionQueries.add(subQuery.toString());
        }

        String finalQuery = String.join(" UNION ALL ", unionQueries);


        return jdbcTemplate.query(finalQuery, (rs, rowNum) -> new LandCountQueryResult(
                rs.getString("region_code"),
                rs.getLong("land_count")), params.toArray());
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
     * 공시지가의 최소값과 최대값 조회
     */
    public LongRangeDto getOfficialLandPriceRange() {
        String sql = "SELECT MIN(official_land_price) as min_price, MAX(official_land_price) as max_price FROM land WHERE official_land_price IS NOT NULL";
        return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
            long min = rs.getLong("min_price");
            long max = rs.getLong("max_price");
            return new LongRangeDto(min, max);
        });
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
            ResultSetUtils.getShortSafe(rs, "use_district_code2").ifPresent(land::setUseDistrictCode2);
            ResultSetUtils.getStringSafe(rs, "use_district_name2").ifPresent(land::setUseDistrictName2);

            ResultSetUtils.getShortSafe(rs, "land_use_code").ifPresent(land::setLandUseCode);
            ResultSetUtils.getStringSafe(rs, "land_use_name").ifPresent(land::setLandUseName);
            ResultSetUtils.getShortSafe(rs, "terrain_height_code").ifPresent(land::setTerrainHeightCode);
            ResultSetUtils.getStringSafe(rs, "terrain_height_name").ifPresent(land::setTerrainHeightName);
            ResultSetUtils.getShortSafe(rs, "terrain_shape_code").ifPresent(land::setTerrainShapeCode);
            ResultSetUtils.getStringSafe(rs, "terrain_shape_name").ifPresent(land::setTerrainShapeName);
            ResultSetUtils.getShortSafe(rs, "road_side_code").ifPresent(land::setRoadSideCode);
            ResultSetUtils.getStringSafe(rs, "road_side_name").ifPresent(land::setRoadSideName);

            ResultSetUtils.getBigDecimalSafe(rs, "official_land_price").ifPresent(land::setOfficialLandPrice);

            // Timestamp를 LocalDateTime으로 변환
            ResultSetUtils.getTimestampSafe(rs, "data_standard_date")
                    .ifPresent(timestamp -> land.setDataStandardDate(timestamp.toLocalDateTime()));
            ResultSetUtils.getTimestampSafe(rs, "created_at")
                    .ifPresent(timestamp -> land.setCreatedAt(timestamp.toLocalDateTime()));
            ResultSetUtils.getTimestampSafe(rs, "updated_at")
                    .ifPresent(timestamp -> land.setUpdatedAt(timestamp.toLocalDateTime()));

            // PostGIS Geometry를 Point 리스트로 파싱 (이미 4326 좌표계)
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

    /**
     * 용도지역 카테고리 필터링 조건 추가
     */
    private void addUseZoneCategoryFilters(StringBuilder sql, List<Object> params,
            LandSearchFilterRequest filterRequest) {
        if (filterRequest.useZoneCategories() != null && !filterRequest.useZoneCategories().isEmpty()) {
            List<Integer> useZoneCodes = UseZoneCode.convertCategoryNamesToZoneCodes(filterRequest.useZoneCategories());

            if (useZoneCodes != null && !useZoneCodes.isEmpty()) {
                sql.append("AND use_district_code1 IN (");
                for (int i = 0; i < useZoneCodes.size(); i++) {
                    if (i > 0) {
                        sql.append(", ");
                    }
                    sql.append("?");
                    params.add(useZoneCodes.get(i));
                }
                sql.append(") ");
            }
        }
    }
}