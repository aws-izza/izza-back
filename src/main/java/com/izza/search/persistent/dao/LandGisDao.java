package com.izza.search.persistent.dao;

import com.izza.search.persistent.model.LandGis;
import com.izza.search.vo.Point;
import com.izza.utils.GisUtils;
import com.izza.utils.ResultSetUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 토지 GIS 정보 DAO
 * land_gis 테이블에서 지리 정보를 처리
 */
@Repository
@RequiredArgsConstructor
public class LandGisDao {

    private final JdbcTemplate jdbcTemplate;

    /**
     * 토지 ID로 GIS 정보 조회
     */
    public Optional<LandGis> findByLandId(Long landId) {
        String sql = """
                SELECT land_id,
                       ST_AsText(boundary) as boundary_wkt,
                       ST_X(center_point) as center_lng,
                       ST_Y(center_point) as center_lat,
                       created_at, updated_at
                FROM land_gis 
                WHERE land_id = ?
                """;
        
        List<LandGis> results = jdbcTemplate.query(sql, new LandGisRowMapper(), landId);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    /**
     * 여러 토지 ID로 GIS 정보 일괄 조회
     */
    public List<LandGis> findByLandIds(List<Long> landIds) {
        if (landIds.isEmpty()) {
            return new ArrayList<>();
        }
        
        String placeholders = String.join(",", landIds.stream().map(id -> "?").toList());
        String sql = """
                SELECT land_id,
                       ST_AsText(boundary) as boundary_wkt,
                       ST_X(center_point) as center_lng,
                       ST_Y(center_point) as center_lat,
                       created_at, updated_at
                FROM land_gis 
                WHERE land_id IN (%s)
                """.formatted(placeholders);
        
        return jdbcTemplate.query(sql, new LandGisRowMapper(), landIds.toArray());
    }

    /**
     * 토지 폴리곤 데이터 조회 (멀티폴리곤 지원)
     */
    public List<List<Point>> findPolygonByLandId(Long landId) {
        String sql = "SELECT ST_AsText(boundary) as boundary_wkt FROM land_gis WHERE land_id = ?";
        List<List<Point>> results = jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
            String wkt = rs.getString("boundary_wkt");
            return GisUtils.parsePolygonToMultiPointList(wkt);
        }, landId);
        return results != null ? results : new ArrayList<>();
    }

    /**
     * 지도 영역 내 토지 ID 조회 (center_point 기준)
     */
    public List<Long> findLandIdsByMapBounds(Double southWestLng, Double southWestLat, 
                                           Double northEastLng, Double northEastLat) {
        String sql = """
                SELECT land_id
                FROM land_gis 
                WHERE ST_Contains(ST_MakeEnvelope(?, ?, ?, ?, 4326), center_point)
                """;
        
        return jdbcTemplate.queryForList(sql, Long.class, 
                southWestLng, southWestLat, northEastLng, northEastLat);
    }

    /**
     * LandGis RowMapper
     */
    private static class LandGisRowMapper implements RowMapper<LandGis> {
        @Override
        public LandGis mapRow(ResultSet rs, int rowNum) throws SQLException {
            LandGis landGis = new LandGis();
            
            ResultSetUtils.getLongSafe(rs, "land_id").ifPresent(landGis::setLandId);
            
            // 경계 정보 파싱
            String boundaryWkt = ResultSetUtils.getStringSafe(rs, "boundary_wkt").orElse(null);
            if (boundaryWkt != null && !boundaryWkt.trim().isEmpty()) {
                landGis.setBoundary(GisUtils.parsePolygonToPointList(boundaryWkt));
            } else {
                landGis.setBoundary(new ArrayList<>());
            }
            
            // 중심점 설정
            Optional<Double> centerLng = ResultSetUtils.getDoubleSafe(rs, "center_lng");
            Optional<Double> centerLat = ResultSetUtils.getDoubleSafe(rs, "center_lat");
            if (centerLng.isPresent() && centerLat.isPresent()) {
                landGis.setCenterPoint(new Point(centerLng.get(), centerLat.get()));
            }
            
            // 시간 정보
            ResultSetUtils.getTimestampSafe(rs, "created_at")
                    .ifPresent(timestamp -> landGis.setCreatedAt(timestamp.toLocalDateTime()));
            ResultSetUtils.getTimestampSafe(rs, "updated_at")
                    .ifPresent(timestamp -> landGis.setUpdatedAt(timestamp.toLocalDateTime()));
            
            return landGis;
        }
    }
}