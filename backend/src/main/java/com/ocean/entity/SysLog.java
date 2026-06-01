package com.ocean.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统操作日志实体
 */
@Data
@TableName("sys_logs")
public class SysLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String username;

    private String action;

    private String description;

    private String ip;

    private Long duration;

    private String result;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
