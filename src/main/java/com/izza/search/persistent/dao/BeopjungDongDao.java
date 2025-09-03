package com.izza.search.persistent.dao;

import com.izza.search.persistent.dto.query.MapSearchQuery;
import com.izza.search.persistent.model.BeopjungDong;
import com.izza.search.vo.Point;
import com.izza.utils.GisUtils;
import com.izza.utils.ResultSetUtils;
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

    public List<BeopjungDong> findAllSido() {
        String sql = """
                SELECT *, 
                       ST_X(center_point) as center_lng,
                       ST_Y(center_point) as center_lat
                FROM beopjeong_dong
                WHERE dong_type = ?
                ORDER BY full_code
                """;

        return jdbcTemplate.query(sql, new BeopjungDongRowMapper(),
                "SIDO");
    }

    /**
     * full_code prefix로 시도/시군구 조회
     */
    public List<BeopjungDong> findByParentCode(String parentCode) {
        String sql = """
                SELECT *,
                       ST_X(center_point) as center_lng,
                       ST_Y(center_point) as center_lat
                FROM beopjeong_dong
                WHERE parent_code = ?
                ORDER BY full_code
                """;

        return jdbcTemplate.query(sql, new BeopjungDongRowMapper(), parentCode);
    }

    private static class BeopjungDongRowMapper implements RowMapper<BeopjungDong> {
        @Override
        public BeopjungDong mapRow(ResultSet rs, int rowNum) throws SQLException {
            BeopjungDong beopjungDong = new BeopjungDong();

            ResultSetUtils.getStringSafe(rs, "full_code").ifPresent(beopjungDong::setFullCode);
            ResultSetUtils.getStringSafe(rs, "korean_name").ifPresent(beopjungDong::setKoreanName);
            ResultSetUtils.getStringSafe(rs, "english_name").ifPresent(beopjungDong::setEnglishName);
            ResultSetUtils.getStringSafe(rs, "dong_type").ifPresent(beopjungDong::setType);
            ResultSetUtils.getStringSafe(rs, "sido").ifPresent(beopjungDong::setSidoName);
            ResultSetUtils.getStringSafe(rs, "sig").ifPresent(beopjungDong::setSigName);
            ResultSetUtils.getStringSafe(rs, "emd").ifPresent(beopjungDong::setEmdName);
            ResultSetUtils.getStringSafe(rs, "ri").ifPresent(beopjungDong::setRiName);

            String boundaryWkt = ResultSetUtils.getStringSafe(rs, "boundary_wkt").orElse(null);
            if (boundaryWkt != null && !boundaryWkt.trim().isEmpty()) {
                beopjungDong.setBoundary(GisUtils.parsePolygonToPointList(boundaryWkt));
            } else {
                beopjungDong.setBoundary(new ArrayList<>());
            }

            Optional<Double> centerLng = ResultSetUtils.getDoubleSafe(rs, "center_lng");
            Optional<Double> centerLat = ResultSetUtils.getDoubleSafe(rs, "center_lat");
            if (centerLng.isPresent() && centerLat.isPresent()) {
                beopjungDong.setCenterPoint(new Point(centerLng.get(), centerLat.get()));
            }

            return beopjungDong;
        }
    }
}