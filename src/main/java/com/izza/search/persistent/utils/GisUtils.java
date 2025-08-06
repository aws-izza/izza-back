package com.izza.search.persistent.utils;


import com.izza.search.vo.Point;
import lombok.experimental.UtilityClass;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class GisUtils {
    private static final GeometryFactory geometryFactory = new GeometryFactory();

    /**
     * WKT를 멀티폴리곤을 고려한 List<List<Point>> 형태로 파싱
     */
    public List<List<Point>> parsePolygonToMultiPointList(String wkt) {
        List<List<Point>> polygons = new ArrayList<>();
        WKTReader reader = new WKTReader(geometryFactory);
        Geometry geometry = null;
        try {
            geometry = reader.read(wkt);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        if (geometry instanceof Polygon) {
            Polygon polygon = (Polygon) geometry;
            List<Point> points = new ArrayList<>();
            Coordinate[] coords = polygon.getExteriorRing().getCoordinates();
            for (Coordinate coord : coords) {
                points.add(new Point(coord.x, coord.y));
            }
            polygons.add(points);
        } else if (geometry instanceof MultiPolygon) {
            MultiPolygon multiPolygon = (MultiPolygon) geometry;
            int numGeometries = multiPolygon.getNumGeometries();
            for (int i = 0; i < numGeometries; i++) {
                Polygon polygon = (Polygon) multiPolygon.getGeometryN(i);
                List<Point> points = new ArrayList<>();
                Coordinate[] coords = polygon.getExteriorRing().getCoordinates();
                for (Coordinate coord : coords) {
                    points.add(new Point(coord.x, coord.y));
                }
                polygons.add(points);
            }
        } else {
            throw new IllegalArgumentException("Unsupported geometry type: " + geometry.getGeometryType());
        }

        return polygons;
    }

    /**
     * 기존 호환성을 위한 단일 폴리곤 파싱 메서드 (deprecated)
     * @deprecated 멀티폴리곤을 고려하지 않으므로 parsePolygonToMultiPointList 사용 권장
     */
    @Deprecated
    public List<Point> parsePolygonToPointList(String wkt) {
        List<Point> points = new ArrayList<>();
        WKTReader reader = new WKTReader(geometryFactory);
        Geometry geometry = null;
        try {
            geometry = reader.read(wkt);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }

        if (geometry instanceof Polygon) {
            Polygon polygon = (Polygon) geometry;
            Coordinate[] coords = polygon.getExteriorRing().getCoordinates();
            for (Coordinate coord : coords) {
                points.add(new Point(coord.x, coord.y));
            }
        } else if (geometry instanceof MultiPolygon) {
            MultiPolygon multiPolygon = (MultiPolygon) geometry;
            int numGeometries = multiPolygon.getNumGeometries();
            for (int i = 0; i < numGeometries; i++) {
                Polygon polygon = (Polygon) multiPolygon.getGeometryN(i);
                Coordinate[] coords = polygon.getExteriorRing().getCoordinates();
                for (Coordinate coord : coords) {
                    points.add(new Point(coord.x, coord.y));
                }
            }
        } else {
            throw new IllegalArgumentException("Unsupported geometry type: " + geometry.getGeometryType());
        }

        return points;
    }
}
