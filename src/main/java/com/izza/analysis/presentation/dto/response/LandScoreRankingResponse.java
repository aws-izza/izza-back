package com.izza.analysis.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 토지 점수 순위 응답 DTO
 * landId가 없는 경우 여러 토지의 점수를 순위별로 반환
 */
@Data
@Builder
@Schema(description = "토지 점수 순위 응답")
public class LandScoreRankingResponse {

    @Schema(description = "분석된 토지 목록 (점수 내림차순 정렬)")
    private List<LandScoreItem> landScores;
}