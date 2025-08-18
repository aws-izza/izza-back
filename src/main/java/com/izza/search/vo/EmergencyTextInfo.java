package com.izza.search.vo;

import com.izza.search.persistent.model.EmergencyText;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

/**
 * 자연재해 정보를 나타내는 값 객체
 */
@Schema(description = "자연재해 정보")
public record EmergencyTextInfo(
        @Schema(description = "총 재해 발생 건수", example = "15") int totalDisasterCount,

        @Schema(description = "주요 재해 유형", example = "태풍") String primaryDisasterType,

        @Schema(description = "재해 유형별 발생 건수") List<DisasterBreakdownItem> disasterTypeBreakdown) {

    /**
     * 재해 유형별 건수 정보를 나타내는 값 객체
     */
    @Schema(description = "재해 유형별 건수 정보")
    public record DisasterBreakdownItem(
            @Schema(description = "재해 유형", example = "태풍") String disasterType,
            @Schema(description = "발생 건수", example = "4") int count) {
    }

    /**
     * 팩토리 메서드 - 재해 목록으로부터 생성
     */
    public static EmergencyTextInfo fromDisasterList(List<EmergencyText> disasters) {
        if (disasters.isEmpty()) {
            return none();
        }

        // 총 재해 건수 계산
        int totalCount = disasters.stream()
                .mapToInt(d -> d.getCount() != null ? d.getCount() : 0)
                .sum();

        // 주요 재해 유형 (가장 많이 발생한 재해)
        EmergencyText primaryDisaster = disasters.stream()
                .max((d1, d2) -> Integer.compare(
                        d1.getCount() != null ? d1.getCount() : 0,
                        d2.getCount() != null ? d2.getCount() : 0))
                .orElse(null);

        String primaryType = primaryDisaster != null ? primaryDisaster.getDisasterTypeName() : null;

        // 재해 유형별 건수 맵 생성 후 리스트로 변환
        Map<String, Integer> breakdownMap = new HashMap<>();
        for (EmergencyText disaster : disasters) {
            String type = disaster.getDisasterTypeName();
            Integer count = disaster.getCount();
            if (type != null && count != null) {
                breakdownMap.put(type, breakdownMap.getOrDefault(type, 0) + count);
            }
        }

        List<DisasterBreakdownItem> breakdown = breakdownMap.entrySet().stream()
                .map(entry -> new DisasterBreakdownItem(entry.getKey(), entry.getValue()))
                .toList();

        return new EmergencyTextInfo(totalCount, primaryType, breakdown);
    }

    /**
     * 단일 재해 정보로부터 생성
     */
    public static EmergencyTextInfo fromSingleDisaster(EmergencyText disaster) {
        if (disaster == null) {
            return none();
        }

        int count = disaster.getCount() != null ? disaster.getCount() : 0;

        List<DisasterBreakdownItem> breakdown = new ArrayList<>();
        if (disaster.getDisasterTypeName() != null) {
            breakdown.add(new DisasterBreakdownItem(disaster.getDisasterTypeName(), count));
        }

        return new EmergencyTextInfo(
                count,
                disaster.getDisasterTypeName(),
                breakdown);
    }

    /**
     * 재해 정보 없음으로 생성
     */
    public static EmergencyTextInfo none() {
        return new EmergencyTextInfo(0, null, new ArrayList<>());
    }

    /**
     * 특정 재해 유형의 발생 건수 조회
     */
    public int getDisasterCountByType(String disasterType) {
        return disasterTypeBreakdown.stream()
                .filter(item -> disasterType.equals(item.disasterType()))
                .mapToInt(DisasterBreakdownItem::count)
                .findFirst()
                .orElse(0);
    }

    /**
     * 가장 위험한 재해 유형 조회
     */
    public String getMostFrequentDisasterType() {
        return disasterTypeBreakdown.stream()
                .max((item1, item2) -> Integer.compare(item1.count(), item2.count()))
                .map(DisasterBreakdownItem::disasterType)
                .orElse(null);
    }
}