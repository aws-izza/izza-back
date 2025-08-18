package com.izza.search.vo;

import com.izza.search.persistent.model.Population;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

/**
 * 인구 정보를 나타내는 값 객체
 */
@Schema(description = "인구 정보")
public record PopulationInfo(
        @Schema(description = "총 인구", example = "12500") Integer totalPopulation,

        @Schema(description = "기준 연월", example = "202407") String referenceMonth,

        @Schema(description = "시도명", example = "서울특별시") String sido,

        @Schema(description = "시군구명", example = "강남구") String sig,

        @Schema(description = "연령대별 인구 분포") List<AgeGroupItem> ageGroups,

        @Schema(description = "성별 인구 분포") Map<String, Integer> genderDistribution) {

    /**
     * 연령대별 인구 정보를 나타내는 값 객체
     */
    @Schema(description = "연령대별 인구 정보")
    public record AgeGroupItem(
            @Schema(description = "연령대", example = "20-29세") String ageGroup,
            @Schema(description = "인구 수", example = "13468") Integer count) {
    }

    /**
     * 팩토리 메서드 - Population 엔티티로부터 생성
     */
    public static PopulationInfo of(Population population) {
        if (population == null) {
            return new PopulationInfo(null, null, null, null,
                    new ArrayList<>(), new HashMap<>());
        }

        // 연령대별 인구 리스트 생성 (나이 순으로 정렬)
        List<AgeGroupItem> ageGroups = List.of(
                new AgeGroupItem("0-9세", population.getAge0to9() != null ? population.getAge0to9() : 0),
                new AgeGroupItem("10-19세", population.getAge10to19() != null ? population.getAge10to19() : 0),
                new AgeGroupItem("20-29세", population.getAge20to29() != null ? population.getAge20to29() : 0),
                new AgeGroupItem("30-39세", population.getAge30to39() != null ? population.getAge30to39() : 0),
                new AgeGroupItem("40-49세", population.getAge40to49() != null ? population.getAge40to49() : 0),
                new AgeGroupItem("50-59세", population.getAge50to59() != null ? population.getAge50to59() : 0),
                new AgeGroupItem("60-69세", population.getAge60to69() != null ? population.getAge60to69() : 0),
                new AgeGroupItem("70-79세", population.getAge70to79() != null ? population.getAge70to79() : 0),
                new AgeGroupItem("80세 이상", population.getAge80plus() != null ? population.getAge80plus() : 0));

        Map<String, Integer> genderDistribution = new HashMap<>();
        genderDistribution.put("남자", population.getMale() != null ? population.getMale() : 0);
        genderDistribution.put("여자", population.getFemale() != null ? population.getFemale() : 0);

        return new PopulationInfo(population.getTotal(), population.getReferenceMonth(),
                population.getSido(), population.getSig(),
                ageGroups, genderDistribution);
    }

    /**
     * 총 인구만으로 생성 (기본값 사용)
     */
    public static PopulationInfo of(Integer totalPopulation) {
        return new PopulationInfo(totalPopulation, null, null, null,
                new ArrayList<>(), new HashMap<>());
    }

    /**
     * 특정 연령대의 인구 수 조회
     */
    public Integer getPopulationByAgeGroup(String ageGroup) {
        return ageGroups.stream()
                .filter(item -> ageGroup.equals(item.ageGroup()))
                .mapToInt(item -> item.count() != null ? item.count() : 0)
                .findFirst()
                .orElse(0);
    }

    /**
     * 청년층 인구 수 (20-39세)
     */
    public Integer getYouthPopulation() {
        return getPopulationByAgeGroup("20-29세") + getPopulationByAgeGroup("30-39세");
    }

    /**
     * 중년층 인구 수 (40-59세)
     */
    public Integer getMiddleAgedPopulation() {
        return getPopulationByAgeGroup("40-49세") + getPopulationByAgeGroup("50-59세");
    }

    /**
     * 고령층 인구 수 (60세 이상)
     */
    public Integer getSeniorPopulation() {
        return getPopulationByAgeGroup("60-69세") +
                getPopulationByAgeGroup("70-79세") +
                getPopulationByAgeGroup("80세 이상");
    }
}