package com.izza.search.persistent.utils;

import com.izza.search.vo.Point;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@UtilityClass
public class GisUtils {
    /**
     * WKT POLYGON을 PointDto 리스트로 파싱
     * 예: "POLYGON((lng1 lat1,lng2 lat2,lng3 lat3,lng1 lat1))" -> List<PointDto>
     */
    public List<Point> parsePolygonToPointList(String wkt) {
        List<Point> points = new ArrayList<>();

        if (wkt == null || !wkt.startsWith("POLYGON")) {
            return points;
        }

        // POLYGON((lng1 lat1,lng2 lat2,...)) 형태에서 좌표 부분 추출
        Pattern pattern = Pattern.compile("POLYGON\\(\\(([^)]+)\\)\\)");
        Matcher matcher = pattern.matcher(wkt);

        if (matcher.find()) {
            String coordinatesStr = matcher.group(1);
            String[] coordinatePairs = coordinatesStr.split(",");

            for (String pair : coordinatePairs) {
                String[] coords = pair.trim().split("\\s+");
                if (coords.length >= 2) {
                    try {
                        double lng = Double.parseDouble(coords[0]);
                        double lat = Double.parseDouble(coords[1]);
                        points.add(new Point(lng, lat));
                    } catch (NumberFormatException e) {
                        // 파싱 실패한 좌표는 무시
                        continue;
                    }
                }
            }
        }

        return points;
    }
}
