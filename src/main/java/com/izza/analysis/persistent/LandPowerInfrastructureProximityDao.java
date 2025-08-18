package com.izza.analysis.persistent;

import com.izza.search.persistent.utils.ResultSetUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class LandPowerInfrastructureProximityDao {

    private final JdbcTemplate jdbcTemplate;

    public LandPowerInfrastructureProximityDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 토지 ID로 전력 인프라 근접성 정보 조회
     */
    public List<LandPowerInfrastructureProximity> findByLandId(Long landId) {
        String sql = """
                SELECT id, land_id, infrastructure_type, infrastructure_osm_id, 
                       distance_meters, voltage, additional_info, created_at, updated_at
                FROM land_power_infrastructure_proximity 
                WHERE land_id = ?
                ORDER BY distance_meters ASC
                """;
        
        return jdbcTemplate.query(sql, new LandPowerInfrastructureProximityRowMapper(), landId);
    }

    /**
     * 변전소 밀도 최솟값 조회 (15km 내 변전소 수 기준)
     */
    public Optional<Integer> findMinSubstationCount() {
        String sql = """
                SELECT MIN(substation_count) 
                FROM (
                    SELECT land_id, COUNT(*) as substation_count
                    FROM land_power_infrastructure_proximity 
                    WHERE infrastructure_type = 'substation'
                    GROUP BY land_id
                ) substation_stats
                """;
        Integer result = jdbcTemplate.queryForObject(sql, Integer.class);
        return Optional.ofNullable(result);
    }

    /**
     * 변전소 밀도 최댓값 조회 (15km 내 변전소 수 기준)
     */
    public Optional<Integer> findMaxSubstationCount() {
        String sql = """
                SELECT MAX(substation_count) 
                FROM (
                    SELECT land_id, COUNT(*) as substation_count
                    FROM land_power_infrastructure_proximity 
                    WHERE infrastructure_type = 'substation'
                    GROUP BY land_id
                ) substation_stats
                """;
        Integer result = jdbcTemplate.queryForObject(sql, Integer.class);
        return Optional.ofNullable(result);
    }

    /**
     * 송전탑 밀도 최솟값 조회 (15km 내 송전탑 수 기준)
     */
    public Optional<Integer> findMinTransmissionTowerCount() {
        String sql = """
                SELECT MIN(tower_count) 
                FROM (
                    SELECT land_id, COUNT(*) as tower_count
                    FROM land_power_infrastructure_proximity 
                    WHERE infrastructure_type = 'transmission_tower'
                    GROUP BY land_id
                ) tower_stats
                """;
        Integer result = jdbcTemplate.queryForObject(sql, Integer.class);
        return Optional.ofNullable(result);
    }

    /**
     * 송전탑 밀도 최댓값 조회 (15km 내 송전탑 수 기준)
     */
    public Optional<Integer> findMaxTransmissionTowerCount() {
        String sql = """
                SELECT MAX(tower_count) 
                FROM (
                    SELECT land_id, COUNT(*) as tower_count
                    FROM land_power_infrastructure_proximity 
                    WHERE infrastructure_type = 'transmission_tower'
                    GROUP BY land_id
                ) tower_stats
                """;
        Integer result = jdbcTemplate.queryForObject(sql, Integer.class);
        return Optional.ofNullable(result);
    }

    /**
     * 특정 토지의 변전소 개수 조회 (15km 내)
     */
    public int getSubstationCountByLandId(Long landId) {
        String sql = """
                SELECT COUNT(*) 
                FROM land_power_infrastructure_proximity 
                WHERE land_id = ? AND infrastructure_type = 'substation'
                """;
        return jdbcTemplate.queryForObject(sql, Integer.class, landId);
    }

    /**
     * 특정 토지의 송전탑 개수 조회 (15km 내)
     */
    public int getTransmissionTowerCountByLandId(Long landId) {
        String sql = """
                SELECT COUNT(*) 
                FROM land_power_infrastructure_proximity 
                WHERE land_id = ? AND infrastructure_type = 'transmission_tower'
                """;
        return jdbcTemplate.queryForObject(sql, Integer.class, landId);
    }

    /**
     * LandPowerInfrastructureProximity RowMapper
     */
    private static class LandPowerInfrastructureProximityRowMapper implements RowMapper<LandPowerInfrastructureProximity> {
        @Override
        public LandPowerInfrastructureProximity mapRow(ResultSet rs, int rowNum) throws SQLException {
            LandPowerInfrastructureProximity proximity = new LandPowerInfrastructureProximity();
            
            proximity.setId(rs.getLong("id"));
            proximity.setLandId(rs.getLong("land_id"));
            
            ResultSetUtils.getStringSafe(rs, "infrastructure_type").ifPresent(proximity::setInfrastructureType);
            ResultSetUtils.getStringSafe(rs, "infrastructure_osm_id").ifPresent(proximity::setInfrastructureOsmId);
            
            ResultSetUtils.getBigDecimalSafe(rs, "distance_meters").ifPresent(proximity::setDistanceMeters);
            ResultSetUtils.getIntegerSafe(rs, "voltage").ifPresent(proximity::setVoltage);
            
            // Timestamp를 LocalDateTime으로 변환
            if (rs.getTimestamp("created_at") != null) {
                proximity.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            }
            if (rs.getTimestamp("updated_at") != null) {
                proximity.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
            }
            
            return proximity;
        }
    }
}