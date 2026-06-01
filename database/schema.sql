# 南海海洋环境数据可视化系统 - 数据库设计

-- ============================================
-- 1. 用户表
-- ============================================
CREATE TABLE IF NOT EXISTS `users` (
    `id`          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `username`    VARCHAR(50)   NOT NULL COMMENT '用户名',
    `password`    VARCHAR(255)  NOT NULL COMMENT '密码（BCrypt加密）',
    `email`       VARCHAR(100)  DEFAULT NULL COMMENT '邮箱',
    `phone`       VARCHAR(20)   DEFAULT NULL COMMENT '手机号',
    `avatar`      VARCHAR(255)  DEFAULT NULL COMMENT '头像URL',
    `role`        VARCHAR(20)   NOT NULL DEFAULT 'USER' COMMENT '角色：USER/ADMIN',
    `status`      TINYINT       NOT NULL DEFAULT 1 COMMENT '状态：1启用 0禁用',
    `create_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除：1删除 0未删除',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_email` (`email`),
    KEY `idx_role` (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ============================================
-- 2. 海洋观测数据表
-- ============================================
CREATE TABLE IF NOT EXISTS `ocean_data` (
    `id`           BIGINT        NOT NULL AUTO_INCREMENT COMMENT '数据ID',
    `data_time`    DATETIME      NOT NULL COMMENT '观测时间',
    `longitude`    DECIMAL(10,6) NOT NULL COMMENT '经度',
    `latitude`     DECIMAL(9,6)  NOT NULL COMMENT '纬度',
    `sea_area`     VARCHAR(50)   DEFAULT NULL COMMENT '海域名称（如：南海北部、湛江海域）',
    `temperature`  DECIMAL(6,2)  DEFAULT NULL COMMENT '海水温度（℃）',
    `salinity`     DECIMAL(6,2)  DEFAULT NULL COMMENT '盐度（PSU）',
    `oxygen`       DECIMAL(6,2)  DEFAULT NULL COMMENT '溶解氧（mg/L）',
    `ph`           DECIMAL(4,2)  DEFAULT NULL COMMENT 'pH值',
    `current_speed` DECIMAL(6,2) DEFAULT NULL COMMENT '洋流速度（m/s）',
    `current_dir`  VARCHAR(20)   DEFAULT NULL COMMENT '洋流方向',
    `tide_level`   DECIMAL(6,2)  DEFAULT NULL COMMENT '潮汐高度（m）',
    `wave_height`  DECIMAL(6,2)  DEFAULT NULL COMMENT '浪高（m）',
    `pressure`     DECIMAL(8,2)  DEFAULT NULL COMMENT '气压（hPa）',
    `source`       VARCHAR(100)  DEFAULT NULL COMMENT '数据来源',
    `create_time`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '入库时间',
    PRIMARY KEY (`id`),
    KEY `idx_data_time` (`data_time`),
    KEY `idx_location` (`longitude`, `latitude`),
    KEY `idx_sea_area` (`sea_area`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='海洋观测数据表';

-- ============================================
-- 3. 监测点位表
-- ============================================
CREATE TABLE IF NOT EXISTS `monitor_points` (
    `id`           BIGINT        NOT NULL AUTO_INCREMENT COMMENT '点位ID',
    `name`         VARCHAR(100)  NOT NULL COMMENT '监测点名称',
    `longitude`    DECIMAL(10,6) NOT NULL COMMENT '经度',
    `latitude`     DECIMAL(9,6)  NOT NULL COMMENT '纬度',
    `type`         VARCHAR(30)   NOT NULL COMMENT '监测类型：WATER_QUALITY/POLLUTION/CURRENT/TIDE',
    `status`       VARCHAR(20)   NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE/INACTIVE/MAINTENANCE',
    `description`  VARCHAR(500)  DEFAULT NULL COMMENT '点位描述',
    `sea_area`     VARCHAR(50)   DEFAULT NULL COMMENT '所属海域',
    `create_time`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_type` (`type`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='监测点位表';

-- ============================================
-- 4. 预警记录表
-- ============================================
CREATE TABLE IF NOT EXISTS `alerts` (
    `id`           BIGINT        NOT NULL AUTO_INCREMENT COMMENT '预警ID',
    `type`         VARCHAR(30)   NOT NULL COMMENT '预警类型：EXTREME_TEMP/TYPHOON/TIDE/POLLUTION/WAVE',
    `level`        VARCHAR(10)   NOT NULL DEFAULT 'INFO' COMMENT '预警等级：INFO/YELLOW/ORANGE/RED',
    `title`        VARCHAR(200)  NOT NULL COMMENT '预警标题',
    `content`      TEXT          NOT NULL COMMENT '预警详情',
    `sea_area`     VARCHAR(100)  DEFAULT NULL COMMENT '影响海域',
    `start_time`   DATETIME      NOT NULL COMMENT '预警开始时间',
    `end_time`     DATETIME      DEFAULT NULL COMMENT '预警结束时间',
    `status`       VARCHAR(20)   NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE/RESOLVED/EXPIRED',
    `create_by`    BIGINT        DEFAULT NULL COMMENT '创建人ID',
    `create_time`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_type_level` (`type`, `level`),
    KEY `idx_status` (`status`),
    KEY `idx_time` (`start_time`, `end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预警记录表';

-- ============================================
-- 5. 科普文章表
-- ============================================
CREATE TABLE IF NOT EXISTS `knowledge` (
    `id`           BIGINT        NOT NULL AUTO_INCREMENT COMMENT '文章ID',
    `title`        VARCHAR(200)  NOT NULL COMMENT '文章标题',
    `category`     VARCHAR(30)   NOT NULL COMMENT '分类：OCEAN_CURRENT/TIDE/ECOLOGY/POLLUTION/CLIMATE/GENERAL',
    `cover_url`    VARCHAR(255)  DEFAULT NULL COMMENT '封面图URL',
    `summary`      VARCHAR(500)  DEFAULT NULL COMMENT '文章摘要',
    `content`      LONGTEXT      NOT NULL COMMENT '文章内容（Markdown）',
    `author`       VARCHAR(50)   DEFAULT '管理员' COMMENT '作者',
    `view_count`   INT           NOT NULL DEFAULT 0 COMMENT '浏览次数',
    `status`       VARCHAR(20)   NOT NULL DEFAULT 'PUBLISHED' COMMENT '状态：DRAFT/PUBLISHED',
    `create_time`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`      TINYINT       NOT NULL DEFAULT 0 COMMENT '逻辑删除',
    PRIMARY KEY (`id`),
    KEY `idx_category` (`category`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='科普文章表';

-- ============================================
-- 6. 导出日志表
-- ============================================
CREATE TABLE IF NOT EXISTS `export_logs` (
    `id`           BIGINT        NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    `user_id`      BIGINT        NOT NULL COMMENT '用户ID',
    `export_type`  VARCHAR(30)   NOT NULL COMMENT '导出类型：EXCEL/CSV/PNG',
    `params`       VARCHAR(500)  DEFAULT NULL COMMENT '导出参数（JSON）',
    `file_name`    VARCHAR(200)  DEFAULT NULL COMMENT '导出文件名',
    `status`       VARCHAR(20)   NOT NULL DEFAULT 'SUCCESS' COMMENT '状态：SUCCESS/FAILED',
    `create_time`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '导出时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='导出日志表';

-- ============================================
-- 7. 系统操作日志表
-- ============================================
CREATE TABLE IF NOT EXISTS `sys_logs` (
    `id`           BIGINT        NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    `user_id`      BIGINT        DEFAULT NULL COMMENT '操作用户ID',
    `username`     VARCHAR(50)   DEFAULT NULL COMMENT '操作用户名',
    `action`       VARCHAR(100)  NOT NULL COMMENT '操作类型（LOGIN/QUERY/EXPORT/CRUD）',
    `description`  VARCHAR(500)  DEFAULT NULL COMMENT '操作描述',
    `ip`           VARCHAR(50)   DEFAULT NULL COMMENT 'IP地址',
    `duration`     BIGINT        DEFAULT NULL COMMENT '耗时（ms）',
    `result`       VARCHAR(20)   DEFAULT 'SUCCESS' COMMENT '操作结果',
    `create_time`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_action` (`action`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统操作日志表';
