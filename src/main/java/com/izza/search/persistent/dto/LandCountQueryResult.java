package com.izza.search.persistent.dto;

/**
 * 법정동별 토지 개수 결과 DTO
 */
public record LandCountQueryResult(
        String beopjungDongCode,
        long count
) {
}