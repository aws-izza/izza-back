package com.izza.search.presentation.utils;

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
}
