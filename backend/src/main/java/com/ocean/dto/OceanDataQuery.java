package com.ocean.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 海洋数据查询条件
 */
@Data
public class OceanDataQuery {

    private String seaArea;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Double minLat;

    private Double maxLat;

    private Double minLng;

    private Double maxLng;
}
