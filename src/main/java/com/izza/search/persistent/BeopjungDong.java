package com.izza.search.persistent;

import com.izza.search.vo.Point;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 행정구역 폴리곤 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BeopjungDong {

    private String fullCode;

    private String koreanName;

    private String englishName;

    private String type;

    private List<Point> boundary;

    private Point centerPoint;


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

    public String getSimpleName() {
        String[] splitName = koreanName.split(" ");
        return splitName[splitName.length - 1];
    }
}