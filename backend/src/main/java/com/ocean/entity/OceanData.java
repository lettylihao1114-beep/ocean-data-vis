package com.ocean.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 海洋观测数据实体
 */
@Data
@TableName("ocean_data")
public class OceanData {

    @TableId(type = IdType.AUTO)
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

    private String source;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
