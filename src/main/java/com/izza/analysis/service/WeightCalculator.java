package com.izza.analysis.service;

import com.izza.analysis.service.dto.LandAnalysisData;
import com.izza.analysis.vo.AnalysisStatisticsType;
import com.izza.analysis.vo.WeightedStatisticsRange;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 가중치 계산 및 정규화를 담당하는 서비스 클래스
 */
@Component
@Slf4j
public class WeightCalculator {
    
    /**
     * LandAnalysisRequest로부터 카테고리 정규화된 가중치 맵 생성
     */
    public Map<AnalysisStatisticsType, Double> createCategoryNormalizedWeights(
            Map<AnalysisStatisticsType, WeightedStatisticsRange> statisticsRanges) {
        
        Map<AnalysisStatisticsType, Double> normalizedWeights = new HashMap<>();
        
        // 카테고리별로 그룹화
        Map<AnalysisStatisticsType.AnalysisCategory, List<AnalysisStatisticsType>> categoryGroups = 
                groupByCategory(statisticsRanges.keySet());
        
        for (Map.Entry<AnalysisStatisticsType.AnalysisCategory, List<AnalysisStatisticsType>> entry : categoryGroups.entrySet()) {
            List<AnalysisStatisticsType> typesInCategory = entry.getValue();
            
            // 카테고리 내 총 가중치 계산
            double totalCategoryWeight = typesInCategory.stream()
                    .mapToDouble(type -> getWeightFromRange(type, statisticsRanges))
                    .sum();
            
            // 카테고리 내에서 정규화 (100% 기준)
            for (AnalysisStatisticsType type : typesInCategory) {
                double originalWeight = getWeightFromRange(type, statisticsRanges);
                double normalizedWeight = totalCategoryWeight > 0 
                    ? (originalWeight / totalCategoryWeight) * 100.0 
                    : 0.0;
                normalizedWeights.put(type, normalizedWeight);
            }
        }
        
        return normalizedWeights;
    }
    
    /**
     * LandAnalysisRequest로부터 전체 정규화된 가중치 맵 생성
     */
    public Map<AnalysisStatisticsType, Double> createGlobalNormalizedWeights(
            Map<AnalysisStatisticsType, WeightedStatisticsRange> statisticsRanges) {
        
        Map<AnalysisStatisticsType, Double> globalWeights = new HashMap<>();
        
        // 카테고리별로 그룹화
        Map<AnalysisStatisticsType.AnalysisCategory, List<AnalysisStatisticsType>> categoryGroups = 
                groupByCategory(statisticsRanges.keySet());
        
        // 각 카테고리의 총 가중치 계산
        Map<AnalysisStatisticsType.AnalysisCategory, Double> categoryTotalWeights = new HashMap<>();
        for (Map.Entry<AnalysisStatisticsType.AnalysisCategory, List<AnalysisStatisticsType>> entry : categoryGroups.entrySet()) {
            AnalysisStatisticsType.AnalysisCategory category = entry.getKey();
            List<AnalysisStatisticsType> types = entry.getValue();
            
            double totalWeight = types.stream()
                    .mapToDouble(type -> getWeightFromRange(type, statisticsRanges))
                    .sum();
            categoryTotalWeights.put(category, totalWeight);
        }
        
        // 전체 가중치 합계
        double totalGlobalWeight = categoryTotalWeights.values().stream()
                .mapToDouble(Double::doubleValue)
                .sum();
        
        if (totalGlobalWeight == 0) {
            return globalWeights;
        }
        
        // 전체에서 재분배
        for (Map.Entry<AnalysisStatisticsType.AnalysisCategory, List<AnalysisStatisticsType>> entry : categoryGroups.entrySet()) {
            AnalysisStatisticsType.AnalysisCategory category = entry.getKey();
            List<AnalysisStatisticsType> types = entry.getValue();
            
            double categoryWeight = categoryTotalWeights.get(category);
            double categoryRatio = categoryWeight / totalGlobalWeight; // 카테고리의 전체 비율
            
            // 카테고리 내 총 가중치
            double totalCategoryWeight = types.stream()
                    .mapToDouble(type -> getWeightFromRange(type, statisticsRanges))
                    .sum();
            
            // 각 타입별로 전체 기준 가중치 계산
            for (AnalysisStatisticsType type : types) {
                double originalWeight = getWeightFromRange(type, statisticsRanges);
                double typeRatioInCategory = totalCategoryWeight > 0 
                    ? originalWeight / totalCategoryWeight 
                    : 0.0;
                double globalWeight = categoryRatio * typeRatioInCategory * 100.0;
                globalWeights.put(type, globalWeight);
            }
        }
        
        return globalWeights;
    }
    
    /**
     * 카테고리별 가중치 정규화 (카테고리 내에서 100% 기준)
     */
    public Map<AnalysisStatisticsType, Double> normalizeCategoryWeights(
            Map<AnalysisStatisticsType, Double> scores,
            LandAnalysisData analysisData) {
        
        Map<AnalysisStatisticsType, Double> normalizedWeights = new HashMap<>();
        
        // 카테고리별로 그룹화
        Map<AnalysisStatisticsType.AnalysisCategory, List<AnalysisStatisticsType>> categoryGroups = 
                groupByCategory(scores);
        
        for (Map.Entry<AnalysisStatisticsType.AnalysisCategory, List<AnalysisStatisticsType>> entry : categoryGroups.entrySet()) {
            List<AnalysisStatisticsType> typesInCategory = entry.getValue();
            
            // 카테고리 내 총 가중치 계산
            double totalCategoryWeight = typesInCategory.stream()
                    .mapToDouble(type -> getOriginalWeight(type, analysisData))
                    .sum();
            
            // 카테고리 내에서 정규화 (100% 기준)
            for (AnalysisStatisticsType type : typesInCategory) {
                double originalWeight = getOriginalWeight(type, analysisData);
                double normalizedWeight = totalCategoryWeight > 0 
                    ? (originalWeight / totalCategoryWeight) * 100.0 
                    : 0.0;
                normalizedWeights.put(type, normalizedWeight);
            }
        }
        
        return normalizedWeights;
    }
    
    /**
     * 전체 가중치 재분배 (전체에서 100% 기준)
     */
    public Map<AnalysisStatisticsType, Double> redistributeGlobalWeights(
            Map<AnalysisStatisticsType, Double> scores,
            LandAnalysisData analysisData) {
        
        Map<AnalysisStatisticsType, Double> globalWeights = new HashMap<>();
        
        // 카테고리별로 그룹화
        Map<AnalysisStatisticsType.AnalysisCategory, List<AnalysisStatisticsType>> categoryGroups = 
                groupByCategory(scores);
        
        // 각 카테고리의 총 가중치 계산
        Map<AnalysisStatisticsType.AnalysisCategory, Double> categoryTotalWeights = new HashMap<>();
        for (Map.Entry<AnalysisStatisticsType.AnalysisCategory, List<AnalysisStatisticsType>> entry : categoryGroups.entrySet()) {
            AnalysisStatisticsType.AnalysisCategory category = entry.getKey();
            List<AnalysisStatisticsType> types = entry.getValue();
            
            double totalWeight = types.stream()
                    .mapToDouble(type -> getOriginalWeight(type, analysisData))
                    .sum();
            categoryTotalWeights.put(category, totalWeight);
        }
        
        // 전체 가중치 합계
        double totalGlobalWeight = categoryTotalWeights.values().stream()
                .mapToDouble(Double::doubleValue)
                .sum();
        
        if (totalGlobalWeight == 0) {
            return globalWeights;
        }
        
        // 전체에서 재분배
        for (Map.Entry<AnalysisStatisticsType.AnalysisCategory, List<AnalysisStatisticsType>> entry : categoryGroups.entrySet()) {
            AnalysisStatisticsType.AnalysisCategory category = entry.getKey();
            List<AnalysisStatisticsType> types = entry.getValue();
            
            double categoryWeight = categoryTotalWeights.get(category);
            double categoryRatio = categoryWeight / totalGlobalWeight; // 카테고리의 전체 비율
            
            // 카테고리 내 총 가중치
            double totalCategoryWeight = types.stream()
                    .mapToDouble(type -> getOriginalWeight(type, analysisData))
                    .sum();
            
            // 각 타입별로 전체 기준 가중치 계산
            for (AnalysisStatisticsType type : types) {
                double originalWeight = getOriginalWeight(type, analysisData);
                double typeRatioInCategory = totalCategoryWeight > 0 
                    ? originalWeight / totalCategoryWeight 
                    : 0.0;
                double globalWeight = categoryRatio * typeRatioInCategory * 100.0;
                globalWeights.put(type, globalWeight);
            }
        }
        
        return globalWeights;
    }
    
    /**
     * 카테고리별 최종 점수 계산 (정규화된 가중치 사용)
     */
    public double calculateWeightedCategoryScore(
            Map<AnalysisStatisticsType, Double> scores,
            Map<AnalysisStatisticsType, Double> normalizedWeights,
            AnalysisStatisticsType.AnalysisCategory category) {
        
        double weightedSum = 0.0;
        
        for (Map.Entry<AnalysisStatisticsType, Double> entry : scores.entrySet()) {
            AnalysisStatisticsType type = entry.getKey();
            Double score = entry.getValue();
            
            if (type.getCategory() == category && score != null) {
                Double weight = normalizedWeights.get(type);
                if (weight != null) {
                    weightedSum += score * (weight / 100.0); // 0~1 범위로 변환
                }
            }
        }
        
        return weightedSum;
    }
    
    /**
     * 전체 최종 점수 계산 (전체 재분배된 가중치 사용)
     */
    public double calculateFinalWeightedScore(
            Map<AnalysisStatisticsType, Double> scores,
            Map<AnalysisStatisticsType, Double> globalWeights) {
        
        double finalScore = 0.0;
        
        for (Map.Entry<AnalysisStatisticsType, Double> entry : scores.entrySet()) {
            AnalysisStatisticsType type = entry.getKey();
            Double score = entry.getValue();
            
            if (score != null) {
                Double weight = globalWeights.get(type);
                if (weight != null) {
                    finalScore += score * (weight / 100.0); // 0~1 범위로 변환
                }
            }
        }
        
        return Math.max(0.0, Math.min(1.0, finalScore));
    }
    
    /**
     * 원본 가중치 조회
     */
    private double getOriginalWeight(AnalysisStatisticsType type, LandAnalysisData analysisData) {
        WeightedStatisticsRange range = analysisData.getStatisticsRanges().get(type);
        if (range != null && range.weight() != null) {
            return range.weight().doubleValue();
        }
        return 50.0; // 기본값
    }
    
    /**
     * WeightedStatisticsRange 맵에서 가중치 추출
     */
    private double getWeightFromRange(AnalysisStatisticsType type, Map<AnalysisStatisticsType, WeightedStatisticsRange> statisticsRanges) {
        WeightedStatisticsRange range = statisticsRanges.get(type);
        if (range != null && range.weight() != null) {
            return range.weight().doubleValue();
        }
        return 50.0; // 기본값
    }
    
    /**
     * 타입들을 카테고리별로 그룹화 (Set 버전)
     */
    private Map<AnalysisStatisticsType.AnalysisCategory, List<AnalysisStatisticsType>> groupByCategory(
            Set<AnalysisStatisticsType> types) {
        
        Map<AnalysisStatisticsType.AnalysisCategory, List<AnalysisStatisticsType>> categoryGroups = new HashMap<>();
        
        for (AnalysisStatisticsType type : types) {
            AnalysisStatisticsType.AnalysisCategory category = type.getCategory();
            categoryGroups.computeIfAbsent(category, k -> new ArrayList<>()).add(type);
        }
        
        return categoryGroups;
    }
    
    /**
     * 점수를 카테고리별로 그룹화 (Map 버전)
     */
    private Map<AnalysisStatisticsType.AnalysisCategory, List<AnalysisStatisticsType>> groupByCategory(
            Map<AnalysisStatisticsType, Double> scores) {
        
        Map<AnalysisStatisticsType.AnalysisCategory, List<AnalysisStatisticsType>> categoryGroups = new HashMap<>();
        
        for (AnalysisStatisticsType type : scores.keySet()) {
            AnalysisStatisticsType.AnalysisCategory category = type.getCategory();
            categoryGroups.computeIfAbsent(category, k -> new ArrayList<>()).add(type);
        }
        
        return categoryGroups;
    }
}