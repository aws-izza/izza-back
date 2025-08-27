package com.izza.analysis.presentation;

import com.izza.analysis.presentation.dto.request.LandAnalysisRequest;
import com.izza.analysis.presentation.dto.response.LandScoreRankingResponse;
import com.izza.analysis.service.LandAnalysisService;
import com.izza.search.presentation.dto.response.BaseApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 토지 분석 API 컨트롤러
 * 토지 점수 계산 및 순위 분석 기능 제공
 */
@RestController
@RequestMapping("${app.base-path}/api/v1/land-analysis")
@RequiredArgsConstructor
@Tag(name = "토지 분석")
public class LandAnalysisController {
    
    private final LandAnalysisService landAnalysisService;

    @PostMapping("/analyze")
    @Operation(summary = "토지 점수 분석 및 순위 조회",
            description = """
                    fullCode 기반으로 토지를 검색하고, 각 토지별로 다양한 지표를 기반으로 점수를 계산하여 순위를 제공합니다.
                    
                    분석 지표:
                    - 필수지표: 토지면적, 공시지가, 용도지역, 전기요금
                    - 선택지표(인프라): 변전소, 송전탑, 전기선, 인구밀도
                    - 선택지표(안정성): 연간 재난문자, 정책 지원 건수
                    
                    각 지표별로 가중치를 설정할 수 있으며, 카테고리별 점수와 전체 기여도를 모두 제공합니다.
                    """)
    public BaseApiResponse<LandScoreRankingResponse> analyzeLand(
            @RequestBody LandAnalysisRequest request
    ) {
        LandScoreRankingResponse response = landAnalysisService.analyzeLand(request);
        return BaseApiResponse.ok(response);
    }
}