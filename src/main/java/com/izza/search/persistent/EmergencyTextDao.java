package com.izza.search.persistent;

import com.izza.search.persistent.utils.ResultSetUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class EmergencyTextDao {

    private final JdbcTemplate jdbcTemplate;

    public EmergencyTextDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 특정 법정동 코드로 자연재해 정보 조회
     */
    public List<EmergencyText> findByFullCode(String fullCode) {
        String sql = """
                SELECT id, 
                       "RCPTN_RGN_NM", 
                       "DST_SE_NM", 
                       count, 
                       full_code
                FROM natural_disasters 
                WHERE full_code = ?
                ORDER BY count DESC
                """;

        return jdbcTemplate.query(sql, new EmergencyTextRowMapper(), fullCode);
    }

    /**
     * 특정 법정동 코드의 총 재해 건수 조회
     */
    public int getTotalDisasterCountByFullCode(String fullCode) {
        String sql = """
                SELECT COALESCE(SUM(count), 0) as total_count
                FROM natural_disasters 
                WHERE full_code = ?
                """;

        Integer result = jdbcTemplate.queryForObject(sql, Integer.class, fullCode);
        return result != null ? result : 0;
    }

    /**
     * 특정 법정동 코드의 주요 재해 유형 조회 (가장 많이 발생한 재해)
     */
    public Optional<EmergencyText> findPrimaryDisasterByFullCode(String fullCode) {
        String sql = """
                SELECT id, 
                       "RCPTN_RGN_NM", 
                       "DST_SE_NM", 
                       count, 
                       full_code
                FROM natural_disasters 
                WHERE full_code = ?
                ORDER BY count DESC
                LIMIT 1
                """;

        List<EmergencyText> results = jdbcTemplate.query(sql, new EmergencyTextRowMapper(), fullCode);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    /**
     * 특정 재해 유형으로 조회
     */
    public List<EmergencyText> findByDisasterType(String disasterType) {
        String sql = """
                SELECT id, 
                       "RCPTN_RGN_NM", 
                       "DST_SE_NM", 
                       count, 
                       full_code
                FROM natural_disasters 
                WHERE "DST_SE_NM" = ?
                ORDER BY count DESC
                """;

        return jdbcTemplate.query(sql, new EmergencyTextRowMapper(), disasterType);
    }

    /**
     * 자연재해 정보 RowMapper
     */
    private static class EmergencyTextRowMapper implements RowMapper<EmergencyText> {
        @Override
        public EmergencyText mapRow(ResultSet rs, int rowNum) throws SQLException {
            EmergencyText emergencyText = new EmergencyText();

            ResultSetUtils.getIntegerSafe(rs, "id").ifPresent(emergencyText::setId);
            ResultSetUtils.getStringSafe(rs, "RCPTN_RGN_NM").ifPresent(emergencyText::setReceptionRegionName);
            ResultSetUtils.getStringSafe(rs, "DST_SE_NM").ifPresent(emergencyText::setDisasterTypeName);
            ResultSetUtils.getIntegerSafe(rs, "count").ifPresent(emergencyText::setCount);
            ResultSetUtils.getStringSafe(rs, "full_code").ifPresent(emergencyText::setFullCode);

            return emergencyText;
        }
    }
}