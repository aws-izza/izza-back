package com.izza.search.persistent;

import com.izza.search.domain.ZoomLevel;
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
public class AreaPolygonDao {

    private final JdbcTemplate jdbcTemplate;

    public AreaPolygonDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 줌 레벨에 따른 행정구역 조회 (토지 개수 포함)
     *
     * @param areaSearchQuery   행정구역 검색 요청
     * @param landFilterRequest 토지 필터 요청
     * @return 행정구역과 해당 구역 내 토지 개수 목록
     */
    public List<AreaPolygonWithLandCount> findAreasWithLandCount(
            AreaSearchQuery areaSearchQuery,
            LandFilterRequest landFilterRequest) {

        // 행정구역 조회 (줌 레벨에 따른 코드 패턴으로)
        List<AreaPolygon> areas = findAreasByZoomLevel(areaSearchQuery);

        // 각 행정구역별 토지 개수 조회
        List<AreaPolygonWithLandCount> result = new ArrayList<>();
        for (AreaPolygon area : areas) {
            long landCount = countLandsByAreaCode(area.getFullCode(), landFilterRequest);
            result.add(AreaPolygonWithLandCount.of(area, landCount));
        }

        return result;
    }

    /**
     * 줌 레벨에 따른 행정구역 코드 패턴으로 조회
     */
    public List<AreaPolygon> findAreasByZoomLevel(AreaSearchQuery areaSearchQuery) {
        String sql = """ 
                SELECT *,
                ST_X(ST_Transform(ST_Centroid(geometry), 4326)) as center_lng,
                ST_Y(ST_Transform(ST_Centroid(geometry), 4326)) as center_lat
                FROM area_polygon WHERE 1=1 \n
                AND type = ?
                AND ST_Intersects(geometry, ST_Transform(ST_MakeEnvelope(?, ?, ?, ?, 4326), 5179))
                """;

        List<Object> params = new ArrayList<>();

        params.add(areaSearchQuery.zoomLevel().getType());
        params.add(areaSearchQuery.southWest().lng());
        params.add(areaSearchQuery.southWest().lat());
        params.add(areaSearchQuery.northEast().lng());
        params.add(areaSearchQuery.northEast().lat());

        return jdbcTemplate.query(sql, new AreaPolygonRowMapper(), params.toArray());
    }

    /**
     * 특정 행정구역 코드로 조회
     */
    public Optional<AreaPolygon> findByFullCode(String fullCode) {
        String sql = "SELECT *, " +
                "ST_X(ST_Transform(ST_Centroid(geometry), 4326)) as center_lng, " +
                "ST_Y(ST_Transform(ST_Centroid(geometry), 4326)) as center_lat " +
                "FROM area_polygon WHERE full_code = ?";

        List<AreaPolygon> results = jdbcTemplate.query(sql, new AreaPolygonRowMapper(), fullCode);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    /**
     * 행정구역 코드에 해당하는 토지 개수 조회 (필터 조건 포함)
     */
    public long countLandsByAreaCode(String areaCode, LandFilterRequest filterRequest) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT COUNT(*) FROM land WHERE beopjung_dong_code LIKE ? ");

        List<Object> params = new ArrayList<>();
        // 행정구역 코드 패턴 매칭을 위한 LIKE 패턴 생성
        params.add(areaCode + "%");

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

        Long count = jdbcTemplate.queryForObject(sql.toString(), Long.class, params.toArray());
        return count != null ? count : 0L;
    }


    /**
     * 지목코드 필터링 조건 추가 (LandDao와 동일한 로직)
     */
    private void addLandCategoryFilters(StringBuilder sql, List<Object> params, LandFilterRequest filterRequest) {
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

    /**
     * AreaPolygon 엔티티 RowMapper
     */
    private static class AreaPolygonRowMapper implements RowMapper<AreaPolygon> {
        @Override
        public AreaPolygon mapRow(ResultSet rs, int rowNum) throws SQLException {
            AreaPolygon areaPolygon = new AreaPolygon();
            areaPolygon.setFullCode(rs.getString("full_code"));
            areaPolygon.setKoreanName(rs.getString("korean_name"));
            areaPolygon.setEnglishName(rs.getString("english_name"));
            areaPolygon.setType(rs.getString("type"));

            // PostGIS Geometry를 Point 리스트로 파싱
            String boundaryWkt = rs.getString("boundary_wkt");
            if (boundaryWkt != null) {
                areaPolygon.setBoundary(parsePolygonToPointList(boundaryWkt));
            }

            // 중심점 설정
            double centerLng = rs.getDouble("center_lng");
            double centerLat = rs.getDouble("center_lat");
            if (!rs.wasNull()) {
                areaPolygon.setCenterPoint(new Point(centerLat, centerLng));
            }

            return areaPolygon;
        }
    }
}