package com.izza.search.persistent.utils;

import lombok.experimental.UtilityClass;

import java.util.List;

/**
 * SQL 조건문 작성을 위한 유틸리티 클래스
 */
@UtilityClass
public class SqlConditionUtils {

    /**
     * Greater Than or Equal (>=) 조건 추가
     */
    public static void gte(StringBuilder sql, List<Object> params, String field, Object value) {
        if (value != null) {
            sql.append(" AND ").append(field).append(" >= ? ");
            params.add(value);
        }
    }

    /**
     * Less Than or Equal (<=) 조건 추가
     */
    public static void lte(StringBuilder sql, List<Object> params, String field, Object value) {
        if (value != null) {
            sql.append(" AND ").append(field).append(" <= ? ");
            params.add(value);
        }
    }

    /**
     * Greater Than (>) 조건 추가
     */
    public static void gt(StringBuilder sql, List<Object> params, String field, Object value) {
        if (value != null) {
            sql.append(" AND ").append(field).append(" > ? ");
            params.add(value);
        }
    }

    /**
     * Less Than (<) 조건 추가
     */
    public static void lt(StringBuilder sql, List<Object> params, String field, Object value) {
        if (value != null) {
            sql.append(" AND ").append(field).append(" < ? ");
            params.add(value);
        }
    }

    /**
     * Equal (=) 조건 추가
     */
    public static void eq(StringBuilder sql, List<Object> params, String field, Object value) {
        if (value != null) {
            sql.append(" AND ").append(field).append(" = ? ");
            params.add(value);
        }
    }

    /**
     * Not Equal (!=) 조건 추가
     */
    public static void ne(StringBuilder sql, List<Object> params, String field, Object value) {
        if (value != null) {
            sql.append(" AND ").append(field).append(" != ? ");
            params.add(value);
        }
    }

    /**
     * IN 조건 추가
     */
    public static void in(StringBuilder sql, List<Object> params, String field, List<?> values) {
        if (values != null && !values.isEmpty()) {
            sql.append(" AND ").append(field).append(" IN (");
            for (int i = 0; i < values.size(); i++) {
                if (i > 0) {
                    sql.append(", ");
                }
                sql.append("?");
                params.add(values.get(i));
            }
            sql.append(") ");
        }
    }

    /**
     * LIKE 조건 추가
     */
    public static void like(StringBuilder sql, List<Object> params, String field, String pattern) {
        if (pattern != null && !pattern.trim().isEmpty()) {
            sql.append(" AND ").append(field).append(" LIKE ? ");
            params.add(pattern);
        }
    }

    /**
     * BETWEEN 조건 추가
     */
    public static void between(StringBuilder sql, List<Object> params, String field, Object minValue, Object maxValue) {
        if (minValue != null && maxValue != null) {
            sql.append(" AND ").append(field).append(" BETWEEN ? AND ? ");
            params.add(minValue);
            params.add(maxValue);
        } else if (minValue != null) {
            gte(sql, params, field, minValue);
        } else if (maxValue != null) {
            lte(sql, params, field, maxValue);
        }
    }

    /**
     * IS NULL 조건 추가
     */
    public static void isNull(StringBuilder sql, String field) {
        sql.append(" AND ").append(field).append(" IS NULL ");
    }

    /**
     * IS NOT NULL 조건 추가
     */
    public static void isNotNull(StringBuilder sql, String field) {
        sql.append(" AND ").append(field).append(" IS NOT NULL ");
    }
}