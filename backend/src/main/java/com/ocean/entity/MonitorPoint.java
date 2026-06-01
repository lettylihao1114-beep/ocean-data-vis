package com.ocean.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 监测点位实体
 */
@Data
@TableName("monitor_points")
public class MonitorPoint {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private BigDecimal longitude;

    private BigDecimal latitude;

    private String type;

    private String status;

    private String description;

    private String seaArea;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
