package com.ocean.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 海洋数据返回视图
 */
@Data
public class OceanDataVO {

    private Long id;

    private LocalDateTime dataTime;

    private BigDecimal longitude;

    private BigDecimal latitude;

    private String seaArea;

    private BigDecimal temperature;

    private BigDecimal salinity;

    private BigDecimal oxygen;

    private BigDecimal ph;

    private BigDecimal currentSpeed;

    private String currentDir;

    private BigDecimal tideLevel;

    private BigDecimal waveHeight;

    private BigDecimal pressure;
}
