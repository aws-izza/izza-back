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

    // TODO: multipolygon일 경우를 고려해서 List<List<Point>> 형태로 반환해야함.
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
