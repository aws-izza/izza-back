package com.izza.search.persistent.dao;

import com.izza.search.persistent.model.BeopjungDong;
import com.izza.search.persistent.dto.query.MapSearchQuery;
import com.izza.utils.GisUtils;
import com.izza.utils.ResultSetUtils;
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
    public List<BeopjungDong> findAreasByZoomLevel(MapSearchQuery mapSearchQuery) {
        String sql = """
                SELECT full_code,
                       beopjung_dong_name as korean_name,
                       dong_type as type,
                       ST_X(center_point) as center_lng,
                       ST_Y(center_point) as center_lat
                FROM beopjeong_dong
                WHERE ST_Contains(ST_MakeEnvelope(?, ?, ?, ?, 4326), center_point) 
                AND dong_type = ?
                """;

        List<Object> params = new ArrayList<>();

        params.add(mapSearchQuery.southWest().lng());
        params.add(mapSearchQuery.southWest().lat());
        params.add(mapSearchQuery.northEast().lng());
        params.add(mapSearchQuery.northEast().lat());
        params.add(mapSearchQuery.zoomLevel().getType());

        return jdbcTemplate.query(sql, new BeopjungDongRowMapper(), params.toArray());
    }

    /**
     * 특정 행정구역 코드로 조회
     */
    public Optional<BeopjungDong> findByFullCode(String fullCode) {
        String sql = """
                SELECT full_code,
                       beopjung_dong_name as korean_name,
                       dong_type as type,
                       ST_X(center_point) as center_lng,
                       ST_Y(center_point) as center_lat,
                       ST_AsText(ST_Transform(boundary, 4326)) as boundary_wkt
                FROM beopjeong_dong
                WHERE full_code = ?
                """;
        List<BeopjungDong> results = jdbcTemplate.query(sql, new BeopjungDongRowMapper(), fullCode);

        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    /**
     * 특정 행정구역 폴리곤 데이터 조회 (멀티폴리곤 지원)
     */
    public List<List<Point>> findPolygonByFullCode(String full_code) {
        String sql = "SELECT ST_AsText(boundary) as boundary_wkt FROM beopjeong_dong WHERE full_code = ?";
        List<List<Point>> results = jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
            String wkt = rs.getString("boundary_wkt");
            return GisUtils.parsePolygonToMultiPointList(wkt);
        }, full_code);
        return results;
    }

    private static class BeopjungDongRowMapper implements RowMapper<BeopjungDong> {
        @Override
        public BeopjungDong mapRow(ResultSet rs, int rowNum) throws SQLException {
            BeopjungDong areaPolygon = new BeopjungDong();

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