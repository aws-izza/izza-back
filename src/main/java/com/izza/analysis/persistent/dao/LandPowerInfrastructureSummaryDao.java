package com.izza.analysis.persistent.dao;

import com.izza.analysis.persistent.model.LandPowerInfrastructureSummary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * 토지별 전력 인프라 요약 정보 DAO
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class LandPowerInfrastructureSummaryDao {
    
    private final JdbcTemplate jdbcTemplate;
    
    /**
     * 토지 ID로 전력 인프라 요약 정보 조회
     */
    public Optional<LandPowerInfrastructureSummary> findByLandId(Long landId) {
        String sql = """
                SELECT land_id, substation_count, substation_closest_distance_meters,
                       transmission_line_count, transmission_line_closest_distance_meters,
                       transmission_tower_count, transmission_tower_closest_distance_meters,
                       total_infrastructure_count, has_high_voltage, created_at, updated_at
                FROM land_power_infrastructure_summary
                WHERE land_id = ?
                """;
        
        List<LandPowerInfrastructureSummary> results = jdbcTemplate.query(
                sql, new LandPowerInfrastructureSummaryRowMapper(), landId);
        
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }
    
    /**
     * 여러 토지 ID로 전력 인프라 요약 정보 일괄 조회
     */
    public List<LandPowerInfrastructureSummary> findByLandIds(List<Long> landIds) {
        if (landIds.isEmpty()) {
            return List.of();
        }
        
        String placeholders = String.join(",", landIds.stream().map(id -> "?").toList());
        String sql = """
                SELECT land_id, substation_count, substation_closest_distance_meters,
                       transmission_line_count, transmission_line_closest_distance_meters,
                       transmission_tower_count, transmission_tower_closest_distance_meters,
                       total_infrastructure_count, has_high_voltage, created_at, updated_at
                FROM land_power_infrastructure_summary
                WHERE land_id IN (%s)
                """.formatted(placeholders);
        
        return jdbcTemplate.query(sql, new LandPowerInfrastructureSummaryRowMapper(), landIds.toArray());
    }
    
    /**
     * 변전소 개수 통계 (최솟값)
     */
    public Optional<Integer> findMinSubstationCount() {
        String sql = "SELECT MIN(substation_count) FROM land_power_infrastructure_summary WHERE substation_count IS NOT NULL";
        Integer result = jdbcTemplate.queryForObject(sql, Integer.class);
        return Optional.ofNullable(result);
    }
    
    /**
     * 변전소 개수 통계 (최댓값)
     */
    public Optional<Integer> findMaxSubstationCount() {
        String sql = "SELECT MAX(substation_count) FROM land_power_infrastructure_summary WHERE substation_count IS NOT NULL";
        Integer result = jdbcTemplate.queryForObject(sql, Integer.class);
        return Optional.ofNullable(result);
    }
    
    /**
     * 송전탑 개수 통계 (최솟값)
     */
    public Optional<Integer> findMinTransmissionTowerCount() {
        String sql = "SELECT MIN(transmission_tower_count) FROM land_power_infrastructure_summary WHERE transmission_tower_count IS NOT NULL";
        Integer result = jdbcTemplate.queryForObject(sql, Integer.class);
        return Optional.ofNullable(result);
    }
    
    /**
     * 송전탑 개수 통계 (최댓값)
     */
    public Optional<Integer> findMaxTransmissionTowerCount() {
        String sql = "SELECT MAX(transmission_tower_count) FROM land_power_infrastructure_summary WHERE transmission_tower_count IS NOT NULL";
        Integer result = jdbcTemplate.queryForObject(sql, Integer.class);
        return Optional.ofNullable(result);
    }
    
    /**
     * 전기선 개수 통계 (최솟값)
     */
    public Optional<Integer> findMinTransmissionLineCount() {
        String sql = "SELECT MIN(transmission_line_count) FROM land_power_infrastructure_summary WHERE transmission_line_count IS NOT NULL";
        Integer result = jdbcTemplate.queryForObject(sql, Integer.class);
        return Optional.ofNullable(result);
    }
    
    /**
     * 전기선 개수 통계 (최댓값)
     */
    public Optional<Integer> findMaxTransmissionLineCount() {
        String sql = "SELECT MAX(transmission_line_count) FROM land_power_infrastructure_summary WHERE transmission_line_count IS NOT NULL";
        Integer result = jdbcTemplate.queryForObject(sql, Integer.class);
        return Optional.ofNullable(result);
    }
    
    /**
     * RowMapper 구현
     */
    private static class LandPowerInfrastructureSummaryRowMapper implements RowMapper<LandPowerInfrastructureSummary> {
        @Override
        public LandPowerInfrastructureSummary mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new LandPowerInfrastructureSummary(
                    rs.getLong("land_id"),
                    rs.getObject("substation_count", Integer.class),
                    rs.getBigDecimal("substation_closest_distance_meters"),
                    rs.getObject("transmission_line_count", Integer.class),
                    rs.getBigDecimal("transmission_line_closest_distance_meters"),
                    rs.getObject("transmission_tower_count", Integer.class),
                    rs.getBigDecimal("transmission_tower_closest_distance_meters"),
                    rs.getObject("total_infrastructure_count", Integer.class),
                    rs.getObject("has_high_voltage", Boolean.class),
                    rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null,
                    rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toLocalDateTime() : null
            );
        }
    }
}