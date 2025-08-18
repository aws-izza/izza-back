package com.izza.search.vo;

import com.izza.search.persistent.model.ElectricityCost;
import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.YearMonth;

/**
 * 전기 요금 정보를 나타내는 값 객체
 */
@Schema(description = "전기 요금 정보")
public record ElectricityCostInfo(
        @Schema(description = "단위 요금 (원/kWh)", example = "120.50") BigDecimal unitCost,

        @Schema(description = "기준 연월", example = "2024-07") YearMonth referenceMonth) {
    /**
     * 팩토리 메서드 - 데이터베이스에서 조회한 값으로 생성
     */
    public static ElectricityCostInfo of(ElectricityCost electricityCost) {
        if (electricityCost == null) {
            return none();
        }

        YearMonth referenceMonth = (electricityCost.getYear() != null && electricityCost.getMonth() != null)
                ? YearMonth.of(electricityCost.getYear(), electricityCost.getMonth())
                : null;

        return new ElectricityCostInfo(
                electricityCost.getUnitCost(),
                referenceMonth);
    }

    /**
     * 단위 요금만으로 생성 (기본값 사용)
     */
    public static ElectricityCostInfo of(BigDecimal unitCost) {
        return new ElectricityCostInfo(unitCost, null);
    }

    /**
     * 전기 요금 정보 없음으로 생성
     */
    public static ElectricityCostInfo none() {
        return new ElectricityCostInfo(null, null);
    }
}