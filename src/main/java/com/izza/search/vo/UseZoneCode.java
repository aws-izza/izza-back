package com.izza.search.vo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import java.util.List;

/**
 * 용도지역 코드 enum
 * 국토의 계획 및 이용에 관한 법률에 따른 용도지역 분류
 */
@Getter
public enum UseZoneCode {
    
    UNSPECIFIED(100, "지정되지않음", "분류되지 않은 용도지역", UseZoneCategory.UNSPECIFIED),
    
    // 주거지역 (10번대)
    EXCLUSIVE_RESIDENTIAL_1(11, "제1종전용주거지역", "단독주택 중심의 양호한 주거환경 보호", UseZoneCategory.RESIDENTIAL),
    EXCLUSIVE_RESIDENTIAL_2(12, "제2종전용주거지역", "공동주택 중심의 양호한 주거환경 보호", UseZoneCategory.RESIDENTIAL),
    GENERAL_RESIDENTIAL_1(13, "제1종일반주거지역", "저층주택 중심의 편리한 주거환경 조성", UseZoneCategory.RESIDENTIAL),
    GENERAL_RESIDENTIAL_2(14, "제2종일반주거지역", "중층주택 중심의 편리한 주거환경 조성", UseZoneCategory.RESIDENTIAL),
    GENERAL_RESIDENTIAL_3(15, "제3종일반주거지역", "중고층주택 중심의 편리한 주거환경 조성", UseZoneCategory.RESIDENTIAL),
    SEMI_RESIDENTIAL(16, "준주거지역", "주거기능 위주, 상업·업무기능 보완", UseZoneCategory.RESIDENTIAL),
    
    // 상업지역 (20번대)
    CENTRAL_COMMERCIAL(21, "중심상업지역", "도심·부도심의 상업·업무기능 집중", UseZoneCategory.COMMERCIAL),
    GENERAL_COMMERCIAL(22, "일반상업지역", "일반적인 상업·업무기능 담당", UseZoneCategory.COMMERCIAL),
    NEIGHBORHOOD_COMMERCIAL(23, "근린상업지역", "근린지역의 일용품 공급 등 근린생활 편의", UseZoneCategory.COMMERCIAL),
    DISTRIBUTION_COMMERCIAL(24, "유통상업지역", "도매·물류기능 중심의 상업활동", UseZoneCategory.COMMERCIAL),
    
    // 공업지역 (30번대)
    EXCLUSIVE_INDUSTRIAL(31, "전용공업지역", "주로 중화학공업 등 공해성 공업배치", UseZoneCategory.INDUSTRIAL),
    GENERAL_INDUSTRIAL(32, "일반공업지역", "환경악화 우려가 적은 공업배치", UseZoneCategory.INDUSTRIAL),
    SEMI_INDUSTRIAL(33, "준공업지역", "경공업 및 주거·상업기능 보완", UseZoneCategory.INDUSTRIAL),
    
    // 녹지지역 (40번대)
    CONSERVATION_GREEN(41, "보전녹지지역", "자연환경·경관·산림 등 보전", UseZoneCategory.GREEN),
    PRODUCTION_GREEN(42, "생산녹지지역", "농업적 생산을 위한 보전", UseZoneCategory.GREEN),
    NATURAL_GREEN(43, "자연녹지지역", "자연환경 보전과 제한적 개발", UseZoneCategory.GREEN),
    DEVELOPMENT_RESTRICTION(44, "개발제한구역", "도시의 무질서한 확산 방지", UseZoneCategory.GREEN),
    
    // 기타 (50번대 이상)
    UNDESIGNATED_USE(51, "용도미지정", "용도지역이 지정되지 않은 지역", UseZoneCategory.OTHER),
    MANAGEMENT_AREA(61, "관리지역", "도시지역과 자연환경보전지역의 중간성격", UseZoneCategory.MANAGEMENT),
    CONSERVATION_MANAGEMENT(62, "보전관리지역", "자연환경 보전을 위한 관리", UseZoneCategory.MANAGEMENT),
    PRODUCTION_MANAGEMENT(63, "생산관리지역", "농·임업 등 생산기능 위한 관리", UseZoneCategory.MANAGEMENT),
    PLANNING_MANAGEMENT(64, "계획관리지역", "계획적·체계적 개발·보전을 위한 관리", UseZoneCategory.MANAGEMENT),
    AGRICULTURAL_FOREST(71, "농림지역", "도시지역에 속하지 않는 농림업 진흥지역", UseZoneCategory.AGRICULTURAL),
    NATURAL_ENVIRONMENT_CONSERVATION(81, "자연환경보전지역", "자연환경·수자원·문화재 등의 보전", UseZoneCategory.CONSERVATION);
    
    private final int code;
    private final String name;
    private final String description;
    private final UseZoneCategory category;
    
    UseZoneCode(int code, String name, String description, UseZoneCategory category) {
        this.code = code;
        this.name = name;
        this.description = description;
        this.category = category;
    }
    
    public static UseZoneCode fromCode(int code) {
        for (UseZoneCode zone : values()) {
            if (zone.code == code) {
                return zone;
            }
        }
        return UNSPECIFIED;
    }
    
    public boolean isIndustrialSuitable() {
        return category == UseZoneCategory.INDUSTRIAL || 
               this == SEMI_RESIDENTIAL ||
               this == DISTRIBUTION_COMMERCIAL ||
               this == PLANNING_MANAGEMENT;
    }

    /**
     * 용도지역 카테고리 이름들을 실제 UseZoneCode 값들로 변환
     */
    public static List<Integer> convertCategoryNamesToZoneCodes(List<String> categoryNames) {
        if (categoryNames == null || categoryNames.isEmpty()) {
            return null;
        }

        return categoryNames.stream()
                .flatMap(categoryName -> java.util.Arrays.stream(UseZoneCode.values())
                        .filter(useZone -> useZone.getCategory().name().equals(categoryName))
                        .map(UseZoneCode::getCode))
                .toList();
    }
    
    @Getter
    @RequiredArgsConstructor
    public enum UseZoneCategory {
        UNSPECIFIED("미분류", false),
        RESIDENTIAL("주거지역", false),
        COMMERCIAL("상업지역", true),
        INDUSTRIAL("공업지역", true),
        GREEN("녹지지역", false),
        MANAGEMENT("관리지역", true),
        AGRICULTURAL("농림지역", false),
        CONSERVATION("자연환경보전지역", false),
        OTHER("기타", false);

        private final String displayName;
        private final boolean enterpriseFit;

    }
}