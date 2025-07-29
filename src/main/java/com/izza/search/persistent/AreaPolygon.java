package com.izza.search.persistent;

import com.izza.search.vo.Point;
import lombok.Data;

import java.util.List;

/**
 * 행정구역 폴리곤 DTO
 */
@Data
public class AreaPolygon {
    
    // 행정구역 코드
    private String fullCode;
    
    // 한국어 명칭
    private String koreanName;
    
    // 영어 명칭
    private String englishName;
    
    // 행정구역 타입
    private String type;
    
    // 경계 정보 (PostGIS Geometry를 Point 리스트로 처리)
    private List<Point> boundary;
    
    // 행정구역 중심점
    private Point centerPoint;
    
    // 기본 생성자
    public AreaPolygon() {}

    @Override
    public String toString() {
        return "AreaPolygon{" +
                "fullCode='" + fullCode + '\'' +
                ", koreanName='" + koreanName + '\'' +
                ", englishName='" + englishName + '\'' +
                ", type='" + type + '\'' +
                ", boundary=" + boundary +
                ", centerPoint=" + centerPoint +
                '}';
    }
}