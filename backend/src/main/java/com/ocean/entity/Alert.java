package com.ocean.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 预警记录实体
 */
@Data
@TableName("alerts")
public class Alert {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String type;

    private String level;

    private String title;

    private String content;

    private String seaArea;

    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private String status;

    private Long createBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
