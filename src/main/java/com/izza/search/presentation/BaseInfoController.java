package com.izza.search.presentation;

import com.izza.search.persistent.LandDao;
import com.izza.search.presentation.dto.response.BaseApiResponse;
import com.izza.search.presentation.dto.LongRangeDto;
import com.izza.search.presentation.dto.response.UseZoneCategoryResponse;
import com.izza.search.vo.UseZoneCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/v1/base-info")
@RequiredArgsConstructor
@Tag(name = "기본 정보")
public class BaseInfoController {
    
    private final LandDao landDao;

    @GetMapping("/land-area-range")
    @Operation(summary = "토지 면적 범위 조회", 
               description = "전체 토지의 면적 최소값과 최대값을 조회합니다. 소수점은 올림 처리됩니다.")
    public BaseApiResponse<LongRangeDto> getLandAreaRange() {
        return BaseApiResponse.ok(landDao.getLandAreaRange());
    }

    @GetMapping("/official-land-price-range")
    @Operation(summary = "공시지가 범위 조회", 
               description = "전체 토지의 공시지가 최소값과 최대값을 조회합니다.")
    public BaseApiResponse<LongRangeDto> getOfficialLandPriceRange() {
        return BaseApiResponse.ok(landDao.getOfficialLandPriceRange());
    }

    @GetMapping("/use-zone-categories")
    @Operation(summary = "기업 적합 용도지역 카테고리 조회", 
               description = "기업 활동에 적합한 용도지역 카테고리 목록을 조회합니다.")
    public BaseApiResponse<List<UseZoneCategoryResponse>> getEnterpriseUseZoneCategories() {
        List<UseZoneCategoryResponse> categories = Arrays.stream(UseZoneCode.UseZoneCategory.values())
                .filter(UseZoneCode.UseZoneCategory::isEnterpriseFit)
                .map(category -> new UseZoneCategoryResponse(category.name(), category.getDisplayName()))
                .toList();
        
        return BaseApiResponse.ok(categories);
    }
}