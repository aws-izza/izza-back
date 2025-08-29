package com.izza.search.persistent.model;

import com.izza.search.vo.Point;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 토지 GIS 정보 엔티티
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LandGis {
    
    private Long landId;
    private List<Point> boundary;
    private Point centerPoint;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}