package com.izza.search.persistent;

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
public class ElectricityCostDao {

    private final JdbcTemplate jdbcTemplate;

    public ElectricityCostDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 특정 법정동 코드로 전기 요금 정보 조회
     * 가장 최근 데이터를 반환 (year, month 기준 내림차순)
     */
    public Optional<ElectricityCost> findByFullCode(String fullCode) {
        String sql = """
                SELECT full_code, 
                       year, 
                       month, 
                       metro, 
                       city, 
                       "unitCost"
                FROM electricity 
                WHERE full_code = ? 
                ORDER BY year DESC, month DESC 
                LIMIT 1
                """;

        List<ElectricityCost> results = jdbcTemplate.query(sql, new ElectricityCostRowMapper(), fullCode);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    /**
     * 특정 법정동 코드의 모든 전기 요금 이력 조회
     */
    public List<ElectricityCost> findAllByFullCode(String fullCode) {
        String sql = """
                SELECT full_code, 
                       year, 
                       month, 
                       metro, 
                       city, 
                       "unitCost"
                FROM electricity 
                WHERE full_code = ? 
                ORDER BY year DESC, month DESC
                """;

        return jdbcTemplate.query(sql, new ElectricityCostRowMapper(), fullCode);
    }

    /**
     * 전체 전기 요금 데이터에서 최소 단위 요금 조회
     */
    public Optional<BigDecimal> findMinUnitCost() {
        String sql = """
                SELECT MIN("unitCost") as min_unit_cost
                FROM electricity
                WHERE "unitCost" IS NOT NULL
                """;

        List<BigDecimal> results = jdbcTemplate.query(sql,
                (rs, rowNum) -> ResultSetUtils.getBigDecimalSafe(rs, "min_unit_cost").orElse(null));

        return results.isEmpty() || results.get(0) == null ? Optional.empty() : Optional.of(results.get(0));
    }

    /**
     * 전체 전기 요금 데이터에서 최대 단위 요금 조회
     */
    public Optional<BigDecimal> findMaxUnitCost() {
        String sql = """
                SELECT MAX("unitCost") as max_unit_cost
                FROM electricity
                WHERE "unitCost" IS NOT NULL
                """;

        List<BigDecimal> results = jdbcTemplate.query(sql,
                (rs, rowNum) -> ResultSetUtils.getBigDecimalSafe(rs, "max_unit_cost").orElse(null));

        return results.isEmpty() || results.get(0) == null ? Optional.empty() : Optional.of(results.get(0));
    }

    /**
     * 전기 요금 정보 RowMapper
     */
    private static class ElectricityCostRowMapper implements RowMapper<ElectricityCost> {
        @Override
        public ElectricityCost mapRow(ResultSet rs, int rowNum) throws SQLException {
            ElectricityCost electricityCost = new ElectricityCost();

            // 필수 필드
            ResultSetUtils.getStringSafe(rs, "full_code").ifPresent(electricityCost::setFullCode);

            // 선택적 필드들
            ResultSetUtils.getIntegerSafe(rs, "year").ifPresent(electricityCost::setYear);
            ResultSetUtils.getIntegerSafe(rs, "month").ifPresent(electricityCost::setMonth);
            ResultSetUtils.getStringSafe(rs, "metro").ifPresent(electricityCost::setMetro);
            ResultSetUtils.getStringSafe(rs, "city").ifPresent(electricityCost::setCity);

            // BigDecimal 처리
            ResultSetUtils.getBigDecimalSafe(rs, "unitCost").ifPresent(electricityCost::setUnitCost);

            return electricityCost;
        }
    }
}