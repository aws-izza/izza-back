package com.izza.search.persistent.dao;

import com.izza.search.persistent.model.Population;
import com.izza.search.persistent.utils.ResultSetUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
public class PopulationDao {

    private final JdbcTemplate jdbcTemplate;

    public PopulationDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 특정 법정동 코드의 모든 인구 이력 조회
     */
    public List<Population> findByFullCode(String fullCode) {
        String sql = """
                SELECT full_code,
                       기준연월,
                       시도명,
                       시군구명,
                       읍면동명,
                       리명,
                       계,
                       "0~9세",
                       "10~19세",
                       "20~29세",
                       "30~39세",
                       "40~49세",
                       "50~59세",
                       "60~69세",
                       "70~79세",
                       "80세~",
                       남자,
                       여자
                FROM population_simple
                WHERE full_code = ?
                ORDER BY 기준연월 DESC
                """;

        return jdbcTemplate.query(sql, new PopulationRowMapper(), fullCode);
    }

    /**
     * 특정 법정동 코드의 상위 지역 인구 집계 조회
     */
    public Population findAggregatedByFullCode(String fullCode) {
        String sql = """
                SELECT ? as full_code,
                       기준연월,
                       시도명,
                       시군구명,
                       null as 읍면동명,
                       null as 리명,
                       SUM(계) as grand_total,
                       SUM("0~9세") as "0-9_total",
                       SUM("10~19세") as "10-19_total",
                       SUM("20~29세") as "20-29_total",
                       SUM("30~39세") as "30-39_total",
                       SUM("40~49세") as "40-49_total",
                       SUM("50~59세") as "50-59_total",
                       SUM("60~69세") as "60-69_total",
                       SUM("70~79세") as "70-79_total",
                       SUM("80세~") as "80+_total",
                       SUM("남자") as "male_total",
                       SUM("여자") as "female_total"
                FROM public.population_simple
                WHERE full_code LIKE ?
                GROUP BY 시도명, 시군구명, 기준연월
                """;

        // Extract prefix from full_code (first 5 characters)
        String codePrefix = fullCode.length() >= 5 ? fullCode.substring(0, 5) : fullCode;
        String likePattern = codePrefix + "%";

        List<Population> results = jdbcTemplate.query(sql, new PopulationRowMapper(),
                fullCode, likePattern);

        return results.isEmpty() ? null : results.get(0);
    }

    /**
     * 인구 정보 RowMapper
     */
    private static class PopulationRowMapper implements RowMapper<Population> {
        @Override
        public Population mapRow(ResultSet rs, int rowNum) throws SQLException {
            Population population = new Population();

            // 기본 정보
            ResultSetUtils.getStringSafe(rs, "full_code").ifPresent(population::setFullCode);
            ResultSetUtils.getStringSafe(rs, "기준연월").ifPresent(population::setReferenceMonth);
            ResultSetUtils.getStringSafe(rs, "시도명").ifPresent(population::setSido);
            ResultSetUtils.getStringSafe(rs, "시군구명").ifPresent(population::setSig);
            ResultSetUtils.getStringSafe(rs, "읍면동명").ifPresent(population::setEmd);
            ResultSetUtils.getStringSafe(rs, "리명").ifPresent(population::setRi);

            // 총 인구
            ResultSetUtils.getIntegerSafe(rs, "grand_total").ifPresent(population::setTotal);

            // 연령대별 인구
            ResultSetUtils.getIntegerSafe(rs, "0-9_total").ifPresent(population::setAge0to9);
            ResultSetUtils.getIntegerSafe(rs, "10-19_total").ifPresent(population::setAge10to19);
            ResultSetUtils.getIntegerSafe(rs, "20-29_total").ifPresent(population::setAge20to29);
            ResultSetUtils.getIntegerSafe(rs, "30-39_total").ifPresent(population::setAge30to39);
            ResultSetUtils.getIntegerSafe(rs, "40-49_total").ifPresent(population::setAge40to49);
            ResultSetUtils.getIntegerSafe(rs, "50-59_total").ifPresent(population::setAge50to59);
            ResultSetUtils.getIntegerSafe(rs, "60-69_total").ifPresent(population::setAge60to69);
            ResultSetUtils.getIntegerSafe(rs, "70-79_total").ifPresent(population::setAge70to79);
            ResultSetUtils.getIntegerSafe(rs, "80+_total").ifPresent(population::setAge80plus);

            // 성별 인구
            ResultSetUtils.getIntegerSafe(rs, "male_total").ifPresent(population::setMale);
            ResultSetUtils.getIntegerSafe(rs, "female_total").ifPresent(population::setFemale);

            return population;
        }
    }

    // /**
    //  * 집계된 인구 정보 RowMapper
    //  */
    // private static class AggregatedPopulationRowMapper implements RowMapper<Population> {
    //     @Override
    //     public Population mapRow(ResultSet rs, int rowNum) throws SQLException {
    //         Population population = new Population();

    //         // 기본 정보
    //         ResultSetUtils.getStringSafe(rs, "full_code").ifPresent(population::setFullCode);
    //         ResultSetUtils.getStringSafe(rs, "기준연월").ifPresent(population::setReferenceMonth);
    //         ResultSetUtils.getStringSafe(rs, "시도명").ifPresent(population::setSido);
    //         ResultSetUtils.getStringSafe(rs, "시군구명").ifPresent(population::setSig);

    //         // 총 인구 (집계된 값)
    //         ResultSetUtils.getIntegerSafe(rs, "grand_total").ifPresent(population::setTotal);

    //         // 연령대별 인구 (집계된 값)
    //         ResultSetUtils.getIntegerSafe(rs, "0~9세_total").ifPresent(population::setAge0to9);
    //         ResultSetUtils.getIntegerSafe(rs, "10~19세_total").ifPresent(population::setAge10to19);
    //         ResultSetUtils.getIntegerSafe(rs, "20~29세_total").ifPresent(population::setAge20to29);
    //         ResultSetUtils.getIntegerSafe(rs, "30~39세_total").ifPresent(population::setAge30to39);
    //         ResultSetUtils.getIntegerSafe(rs, "40~49세_total").ifPresent(population::setAge40to49);
    //         ResultSetUtils.getIntegerSafe(rs, "50~59세_total").ifPresent(population::setAge50to59);
    //         ResultSetUtils.getIntegerSafe(rs, "60~69세_total").ifPresent(population::setAge60to69);
    //         ResultSetUtils.getIntegerSafe(rs, "70~79세_total").ifPresent(population::setAge70to79);
    //         ResultSetUtils.getIntegerSafe(rs, "80세~_total").ifPresent(population::setAge80plus);

    //         // 성별 인구
    //         ResultSetUtils.getIntegerSafe(rs, "남자").ifPresent(population::setMale);
    //         ResultSetUtils.getIntegerSafe(rs, "여자").ifPresent(population::setFemale);

    //         return population;
    //     }
    // }
}