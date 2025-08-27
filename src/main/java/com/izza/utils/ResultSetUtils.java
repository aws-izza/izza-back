package com.izza.utils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class ResultSetUtils {
    /**
     * 컬럼이 존재하지 않거나 값이 null일 경우 빈 Optional 반환
     * 정상적으로 값이 있으면 Optional에 담아서 반환
     */
    public static Optional<String> getStringSafe(ResultSet rs, String columnName) {
        try {
            String value = rs.getString(columnName);
            return Optional.ofNullable(value);
        } catch (SQLException e) {
            // 컬럼 없거나 다른 에러 발생 시 무시하고 빈 Optional 반환
            return Optional.empty();
        }
    }

    /**
     * double 타입 값을 안전하게 가져옴 (null 가능)
     */
    public static Optional<Double> getDoubleSafe(ResultSet rs, String columnName) {
        try {
            double value = rs.getDouble(columnName);
            if (rs.wasNull()) {
                return Optional.empty();
            }
            return Optional.of(value);
        } catch (SQLException e) {
            return Optional.empty();
        }
    }

    /**
     * long 타입 값을 안전하게 가져옴 (null 가능)
     */
    public static Optional<Long> getLongSafe(ResultSet rs, String columnName) {
        try {
            long value = rs.getLong(columnName);
            if (rs.wasNull()) {
                return Optional.empty();
            }
            return Optional.of(value);
        } catch (SQLException e) {
            return Optional.empty();
        }
    }

    /**
     * short 타입 값을 안전하게 가져옴 (null 가능)
     */
    public static Optional<Short> getShortSafe(ResultSet rs, String columnName) {
        try {
            short value = rs.getShort(columnName);
            if (rs.wasNull()) {
                return Optional.empty();
            }
            return Optional.of(value);
        } catch (SQLException e) {
            return Optional.empty();
        }
    }

    /**
     * Integer 타입 값을 안전하게 가져옴 (null 가능)
     */
    public static Optional<Integer> getIntegerSafe(ResultSet rs, String columnName) {
        try {
            int value = rs.getInt(columnName);
            if (rs.wasNull()) {
                return Optional.empty();
            }
            return Optional.of(value);
        } catch (SQLException e) {
            return Optional.empty();
        }
    }

    /**
     * BigDecimal 타입 값을 안전하게 가져옴 (null 가능)
     */
    public static Optional<java.math.BigDecimal> getBigDecimalSafe(ResultSet rs, String columnName) {
        try {
            java.math.BigDecimal value = rs.getBigDecimal(columnName);
            return Optional.ofNullable(value);
        } catch (SQLException e) {
            return Optional.empty();
        }
    }

    /**
     * Timestamp 타입 값을 안전하게 가져옴 (null 가능)
     */
    public static Optional<java.sql.Timestamp> getTimestampSafe(ResultSet rs, String columnName) {
        try {
            java.sql.Timestamp value = rs.getTimestamp(columnName);
            return Optional.ofNullable(value);
        } catch (SQLException e) {
            return Optional.empty();
        }
    }
}
