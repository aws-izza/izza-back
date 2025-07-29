package com.izza.search.persistent;

import com.izza.search.presentation.dto.LandSearchFilterRequest;
import com.izza.search.presentation.dto.MapSearchRequest;
import com.izza.search.vo.Point;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.izza.search.persistent.GisUtils.parsePolygonToPointList;

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
                ST_AsText(ST_Transform(boundary, 4326)) as boundary_wkt,
                ST_X(ST_Transform(ST_Centroid(boundary), 4326)) as center_lng,
                ST_Y(ST_Transform(ST_Centroid(boundary), 4326)) as center_lat\s
                FROM land WHERE 1=1\s
               """;
        sqlBuilder.append(sql);

        List<Object> params = new ArrayList<>();

        // 지도 영역 필터링 (WGS84 좌표를 5186으로 변환하여 비교)
        if (mapRequest.southWestLat() != null && mapRequest.southWestLng() != null &&
            mapRequest.northEastLat() != null && mapRequest.northEastLng() != null) {
            sqlBuilder.append("AND ST_Intersects(boundary, ST_Transform(ST_MakeEnvelope(?, ?, ?, ?, 4326), 5186)) ");
            params.add(mapRequest.southWestLng());
            params.add(mapRequest.southWestLat());
            params.add(mapRequest.northEastLng());
            params.add(mapRequest.northEastLat());
        }

        // 토지 면적 필터링
        if (filterRequest.landAreaMin() != null) {
            sqlBuilder.append("AND land_area >= ? ");
            params.add(filterRequest.landAreaMin());
        }
        if (filterRequest.landAreaMax() != null) {
            sqlBuilder.append("AND land_area <= ? ");
            params.add(filterRequest.landAreaMax());
        }

        // 공시지가 필터링
        if (filterRequest.officialLandPriceMin() != null) {
            sqlBuilder.append("AND official_land_price >= ? ");
            params.add(filterRequest.officialLandPriceMin());
        }
        if (filterRequest.officialLandPriceMax() != null) {
            sqlBuilder.append("AND official_land_price <= ? ");
            params.add(filterRequest.officialLandPriceMax());
        }

        // 지목코드 필터링
        addLandCategoryFilters(sqlBuilder, params, filterRequest);

        sqlBuilder.append("ORDER BY id LIMIT 1000");

        return jdbcTemplate.query(sqlBuilder.toString(), new LandRowMapper(), params.toArray());
    }

    /**
     * 지도 영역 내 토지 중심점 조회 (마커용)
     */
    public List<LandWithPoint> findLandPointsInMapBounds(MapSearchRequest mapRequest, LandSearchFilterRequest filterRequest) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT id, address, ");
        sql.append("ST_X(ST_Transform(ST_Centroid(boundary), 4326)) as lng, ");
        sql.append("ST_Y(ST_Transform(ST_Centroid(boundary), 4326)) as lat ");
        sql.append("FROM land WHERE 1=1 ");

        List<Object> params = new ArrayList<>();

        // 지도 영역 필터링 (WGS84 좌표를 5186으로 변환하여 비교)
        if (mapRequest.southWestLat() != null && mapRequest.southWestLng() != null &&
            mapRequest.northEastLat() != null && mapRequest.northEastLng() != null) {
            sql.append("AND ST_Intersects(boundary, ST_Transform(ST_MakeEnvelope(?, ?, ?, ?, 4326), 5186)) ");
            params.add(mapRequest.southWestLng());
            params.add(mapRequest.southWestLat());
            params.add(mapRequest.northEastLng());
            params.add(mapRequest.northEastLat());
        }

        // 토지 면적 필터링
        if (filterRequest.landAreaMin() != null) {
            sql.append("AND land_area >= ? ");
            params.add(filterRequest.landAreaMin());
        }
        if (filterRequest.landAreaMax() != null) {
            sql.append("AND land_area <= ? ");
            params.add(filterRequest.landAreaMax());
        }

        // 공시지가 필터링
        if (filterRequest.officialLandPriceMin() != null) {
            sql.append("AND official_land_price >= ? ");
            params.add(filterRequest.officialLandPriceMin());
        }
        if (filterRequest.officialLandPriceMax() != null) {
            sql.append("AND official_land_price <= ? ");
            params.add(filterRequest.officialLandPriceMax());
        }

        // 지목코드 필터링
        addLandCategoryFilters(sql, params, filterRequest);

        sql.append("ORDER BY id LIMIT 1000");

        return jdbcTemplate.query(sql.toString(), (rs, rowNum) ->
            new LandWithPoint(
                rs.getLong("id"),
                rs.getString("address"),
                new Point(rs.getDouble("lat"), rs.getDouble("lng"))
            ), params.toArray()
        );
    }

    /**
     * ID로 토지 상세 정보 조회
     */
    public Optional<Land> findById(Long id) {
        String sql = "SELECT *, " +
                    "ST_AsText(ST_Transform(boundary, 4326)) as boundary_wkt, " +
                    "ST_X(ST_Transform(ST_Centroid(boundary), 4326)) as center_lng, " +
                    "ST_Y(ST_Transform(ST_Centroid(boundary), 4326)) as center_lat " +
                    "FROM land WHERE id = ?";
        List<Land> results = jdbcTemplate.query(sql, new LandRowMapper(), id);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    /**
     * 토지 폴리곤 데이터 조회 (Point 리스트 형태)
     */
    public Optional<List<Point>> findPolygonById(Long id) {
        String sql = "SELECT ST_AsText(ST_Transform(boundary, 4326)) as boundary_wkt FROM land WHERE id = ?";
        List<List<Point>> results = jdbcTemplate.query(sql, (rs, rowNum) -> {
                String wkt = rs.getString("boundary_wkt");
                return parsePolygonToPointList(wkt);
            }, id);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    /**
     * 토지 폴리곤 데이터 조회 (GeoJSON 형태) - 기존 호환성을 위해 유지
     */
    public Optional<String> findPolygonGeoJsonById(Long id) {
        String sql = "SELECT ST_AsGeoJSON(ST_Transform(boundary, 4326)) as geojson FROM land WHERE id = ?";
        List<String> results = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString("geojson"), id);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    /**
     * 지역별 토지 개수 집계 (줌 레벨이 낮을 때 사용)
     */
    public List<LandGroupCount> countLandsByRegion(MapSearchRequest mapRequest, LandSearchFilterRequest filterRequest) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT LEFT(beopjung_dong_code, 2) as region_code, ");
        sql.append("COUNT(*) as land_count, ");
        sql.append("ST_X(ST_Transform(ST_Centroid(ST_Union(boundary)), 4326)) as center_lng, ");
        sql.append("ST_Y(ST_Transform(ST_Centroid(ST_Union(boundary)), 4326)) as center_lat ");
        sql.append("FROM land WHERE 1=1 ");

        List<Object> params = new ArrayList<>();

        // 지도 영역 필터링 (WGS84 좌표를 5186으로 변환하여 비교)
        if (mapRequest.southWestLat() != null && mapRequest.southWestLng() != null &&
            mapRequest.northEastLat() != null && mapRequest.northEastLng() != null) {
            sql.append("AND ST_Intersects(boundary, ST_Transform(ST_MakeEnvelope(?, ?, ?, ?, 4326), 5186)) ");
            params.add(mapRequest.southWestLng());
            params.add(mapRequest.southWestLat());
            params.add(mapRequest.northEastLng());
            params.add(mapRequest.northEastLat());
        }

        // 필터 조건들
        if (filterRequest.landAreaMin() != null) {
            sql.append("AND land_area >= ? ");
            params.add(filterRequest.landAreaMin());
        }
        if (filterRequest.landAreaMax() != null) {
            sql.append("AND land_area <= ? ");
            params.add(filterRequest.landAreaMax());
        }
        if (filterRequest.officialLandPriceMin() != null) {
            sql.append("AND official_land_price >= ? ");
            params.add(filterRequest.officialLandPriceMin());
        }
        if (filterRequest.officialLandPriceMax() != null) {
            sql.append("AND official_land_price <= ? ");
            params.add(filterRequest.officialLandPriceMax());
        }

        // 지목코드 필터링
        addLandCategoryFilters(sql, params, filterRequest);

        sql.append("GROUP BY LEFT(beopjung_dong_code, 2) ");
        sql.append("ORDER BY land_count DESC ");

        return jdbcTemplate.query(sql.toString(), (rs, rowNum) ->
            new LandGroupCount(
                rs.getString("region_code"),
                rs.getLong("land_count"),
                new Point(rs.getDouble("center_lat"), rs.getDouble("center_lng"))
            ), params.toArray()
        );
    }

    /**
     * Land 엔티티 RowMapper
     */
    private static class LandRowMapper implements RowMapper<Land> {
        @Override
        public Land mapRow(ResultSet rs, int rowNum) throws SQLException {
            Land land = new Land();
            land.setId(rs.getLong("id"));
            land.setShapeId(rs.getLong("shape_id"));
            land.setUniqueNo(rs.getString("unique_no"));
            land.setBeopjungDongCode(rs.getString("beopjung_dong_code"));
            land.setAddress(rs.getString("address"));
            land.setLedgerDivisionCode(rs.getShort("ledger_division_code"));
            land.setLedgerDivisionName(rs.getString("ledger_division_name"));
            land.setBaseYear(rs.getShort("base_year"));
            land.setBaseMonth(rs.getShort("base_month"));
            land.setLandCategoryCode(rs.getShort("land_category_code"));
            land.setLandCategoryName(rs.getString("land_category_name"));
            land.setLandArea(rs.getBigDecimal("land_area"));
            land.setUseDistrictCode1(rs.getShort("use_district_code1"));
            land.setUseDistrictName1(rs.getString("use_district_name1"));
            land.setUseDistrictCode2(rs.getShort("use_district_code2"));
            land.setUseDistrictName2(rs.getString("use_district_name2"));
            land.setLandUseCode(rs.getShort("land_use_code"));
            land.setLandUseName(rs.getString("land_use_name"));
            land.setTerrainHeightCode(rs.getShort("terrain_height_code"));
            land.setTerrainHeightName(rs.getString("terrain_height_name"));
            land.setTerrainShapeCode(rs.getShort("terrain_shape_code"));
            land.setTerrainShapeName(rs.getString("terrain_shape_name"));
            land.setRoadSideCode(rs.getShort("road_side_code"));
            land.setRoadSideName(rs.getString("road_side_name"));
            land.setOfficialLandPrice(rs.getBigDecimal("official_land_price"));

            // Timestamp를 LocalDateTime으로 변환
            if (rs.getTimestamp("data_standard_date") != null) {
                land.setDataStandardDate(rs.getTimestamp("data_standard_date").toLocalDateTime());
            }
            if (rs.getTimestamp("created_at") != null) {
                land.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            }
            if (rs.getTimestamp("updated_at") != null) {
                land.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
            }

            // PostGIS Geometry를 Point 리스트로 파싱
            String boundaryWkt = rs.getString("boundary_wkt");
            if (boundaryWkt != null) {
                land.setBoundary(parsePolygonToPointList(boundaryWkt));
            }

            // 중심점 설정
            double centerLng = rs.getDouble("center_lng");
            double centerLat = rs.getDouble("center_lat");
            if (!rs.wasNull()) {
                land.setCenterPoint(new Point(centerLng, centerLat));
            }

            return land;
        }
    }

    /**
     * 토지와 중심점 정보를 담는 DTO
     */
    public record LandWithPoint(Long id, String address, Point point) {}

    /**
     * 지역별 토지 개수 집계 결과를 담는 DTO
     */
    public record LandGroupCount(String regionCode, Long count, Point centerPoint) {}

    /**
     * 지목코드 필터링 조건 추가
     */
    private void addLandCategoryFilters(StringBuilder sql, List<Object> params, LandSearchFilterRequest filterRequest) {
        // 특정 지목코드 목록 필터링
        if (filterRequest.landCategoryCodes() != null && !filterRequest.landCategoryCodes().isEmpty()) {
            sql.append("AND land_category_code IN (");
            for (int i = 0; i < filterRequest.landCategoryCodes().size(); i++) {
                if (i > 0) sql.append(", ");
                sql.append("?");
                params.add(filterRequest.landCategoryCodes().get(i));
            }
            sql.append(") ");
        }

        // 농업용 토지만 필터링
        if (Boolean.TRUE.equals(filterRequest.agriculturalOnly())) {
            sql.append("AND land_category_code IN (1, 2, 3, 4) "); // 전, 답, 과수원, 목장용지
        }

        // 건축 가능 토지만 필터링
        if (Boolean.TRUE.equals(filterRequest.buildableOnly())) {
            sql.append("AND land_category_code IN (8, 9, 10, 12, 13) "); // 대, 공장용지, 학교용지, 주유소용지, 창고용지
        }
    }
}