package com.ocean.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 导出日志实体
 */
@Data
@TableName("export_logs")
public class ExportLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String exportType;

    private String params;

    private String fileName;

    private String status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
