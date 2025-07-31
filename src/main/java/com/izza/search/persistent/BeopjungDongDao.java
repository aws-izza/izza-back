package com.izza.search.persistent;

import com.izza.search.persistent.query.MapSearchQuery;
import com.izza.search.persistent.utils.GisUtils;
import com.izza.search.persistent.utils.ResultSetUtils;
import com.izza.search.vo.Point;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Repository
public class BeopjungDongDao {

    private final JdbcTemplate jdbcTemplate;

    public BeopjungDongDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 줌 레벨에 따른 행정구역 코드 패턴으로 조회
     */
    public List<AreaPolygon> findAreasByZoomLevel(MapSearchQuery mapSearchQuery) {
        String sql = """
                SELECT full_code,
                       beopjung_dong_name as korean_name,
                       dong_type as type,
                       ST_X(center_point) as center_lng,
                       ST_Y(center_point) as center_lat
                FROM beopjeong_dong
                WHERE dong_type = ?
                AND ST_Intersects(boundary, ST_MakeEnvelope(?, ?, ?, ?, 4326))
                """;

        List<Object> params = new ArrayList<>();

        params.add(mapSearchQuery.zoomLevel().getType());
        params.add(mapSearchQuery.southWest().lng());
        params.add(mapSearchQuery.southWest().lat());
        params.add(mapSearchQuery.northEast().lng());
        params.add(mapSearchQuery.northEast().lat());

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
    /**
     * AreaPolygon 엔티티 RowMapper
     */
    private static class AreaPolygonRowMapper implements RowMapper<AreaPolygon> {
        @Override
        public AreaPolygon mapRow(ResultSet rs, int rowNum) throws SQLException {
            AreaPolygon areaPolygon = new AreaPolygon();

            ResultSetUtils.getStringSafe(rs, "full_code").ifPresent(areaPolygon::setFullCode);
            ResultSetUtils.getStringSafe(rs, "korean_name").ifPresent(areaPolygon::setKoreanName);
            ResultSetUtils.getStringSafe(rs, "english_name").ifPresent(areaPolygon::setEnglishName);
            ResultSetUtils.getStringSafe(rs, "type").ifPresent(areaPolygon::setType);

            String boundaryWkt = ResultSetUtils.getStringSafe(rs, "boundary_wkt").orElse(null);
            if (boundaryWkt != null && !boundaryWkt.trim().isEmpty()) {
                areaPolygon.setBoundary(GisUtils.parsePolygonToPointList(boundaryWkt));
            } else {
                areaPolygon.setBoundary(new ArrayList<>());
            }

            Optional<Double> centerLng = ResultSetUtils.getDoubleSafe(rs, "center_lng");
            Optional<Double> centerLat = ResultSetUtils.getDoubleSafe(rs, "center_lat");
            if (centerLng.isPresent() && centerLat.isPresent()) {
                areaPolygon.setCenterPoint(new Point(centerLng.get(), centerLat.get()));
            }

            return areaPolygon;
        }
    }
}