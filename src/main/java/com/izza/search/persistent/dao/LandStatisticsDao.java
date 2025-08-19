package com.izza.search.persistent.dao;

import com.izza.search.persistent.model.LandStatistics;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class LandStatisticsDao {

    private final JdbcTemplate jdbcTemplate;

    public LandStatisticsDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<LandStatistics> findByStatType(String statType) {
        String sql = "SELECT stat_type, min_value, max_value, updated_at FROM land_statistics WHERE stat_type = ?";
        List<LandStatistics> results = jdbcTemplate.query(sql, new LandStatisticsRowMapper(), statType);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public List<LandStatistics> findAll() {
        String sql = "SELECT stat_type, min_value, max_value, updated_at FROM land_statistics ORDER BY stat_type";
        return jdbcTemplate.query(sql, new LandStatisticsRowMapper());
    }

    public void save(LandStatistics landStatistics) {
        String sql = """
            INSERT INTO land_statistics (stat_type, min_value, max_value, updated_at) 
            VALUES (?, ?, ?, CURRENT_TIMESTAMP)
            ON CONFLICT (stat_type) 
            DO UPDATE SET min_value = EXCLUDED.min_value, 
                         max_value = EXCLUDED.max_value, 
                         updated_at = CURRENT_TIMESTAMP
            """;
        
        jdbcTemplate.update(sql, 
                landStatistics.getStatType(),
                landStatistics.getMinValue(),
                landStatistics.getMaxValue());
    }

    public void deleteByStatType(String statType) {
        String sql = "DELETE FROM land_statistics WHERE stat_type = ?";
        jdbcTemplate.update(sql, statType);
    }

    private static class LandStatisticsRowMapper implements RowMapper<LandStatistics> {
        @Override
        public LandStatistics mapRow(ResultSet rs, int rowNum) throws SQLException {
            return LandStatistics.builder()
                    .statType(rs.getString("stat_type"))
                    .minValue(rs.getLong("min_value"))
                    .maxValue(rs.getLong("max_value"))
                    .updatedAt(rs.getTimestamp("updated_at").toLocalDateTime())
                    .build();
        }
    }
}