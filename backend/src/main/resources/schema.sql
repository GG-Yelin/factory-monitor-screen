-- 工厂监控大屏数据库建表语句
-- 数据库: factory_monitor

CREATE DATABASE IF NOT EXISTS factory_monitor
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_unicode_ci;

USE factory_monitor;

-- =============================================
-- 生产记录表 - 记录每日生产数据
-- =============================================
CREATE TABLE IF NOT EXISTS production_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    record_date DATE NOT NULL COMMENT '记录日期',
    device_id VARCHAR(64) DEFAULT NULL COMMENT '设备ID（NULL表示汇总数据）',
    device_name VARCHAR(128) DEFAULT NULL COMMENT '设备名称',
    item_id VARCHAR(64) DEFAULT NULL COMMENT '项目ID',
    item_name VARCHAR(128) DEFAULT NULL COMMENT '项目名称',
    production INT NOT NULL DEFAULT 0 COMMENT '生产数量',
    plan_quantity INT DEFAULT NULL COMMENT '计划数量',
    qualified_quantity INT DEFAULT NULL COMMENT '良品数量',
    defective_quantity INT DEFAULT NULL COMMENT '不良品数量',
    completion_rate DOUBLE DEFAULT NULL COMMENT '完成率',
    quality_rate DOUBLE DEFAULT NULL COMMENT '良品率',
    running_minutes INT DEFAULT NULL COMMENT '设备运行时长(分钟)',
    down_time_minutes INT DEFAULT NULL COMMENT '设备停机时长(分钟)',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_record_date (record_date),
    INDEX idx_device_id (device_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='生产记录表';

-- =============================================
-- 报警记录表
-- =============================================
CREATE TABLE IF NOT EXISTS alarm_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    device_id VARCHAR(64) NOT NULL COMMENT '设备ID',
    device_name VARCHAR(128) DEFAULT NULL COMMENT '设备名称',
    alarm_type VARCHAR(64) DEFAULT NULL COMMENT '报警类型',
    alarm_content VARCHAR(500) DEFAULT NULL COMMENT '报警内容',
    level INT DEFAULT 1 COMMENT '报警等级 (1-低, 2-中, 3-高)',
    status INT NOT NULL DEFAULT 0 COMMENT '状态 (0-未处理, 1-已处理, 2-已忽略)',
    alarm_time DATETIME NOT NULL COMMENT '报警时间',
    handle_time DATETIME DEFAULT NULL COMMENT '处理时间',
    handler VARCHAR(64) DEFAULT NULL COMMENT '处理人',
    handle_remark VARCHAR(500) DEFAULT NULL COMMENT '处理备注',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_alarm_time (alarm_time),
    INDEX idx_alarm_device (device_id),
    INDEX idx_alarm_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='报警记录表';

-- =============================================
-- 设备状态日志表 - 记录设备状态变化
-- =============================================
CREATE TABLE IF NOT EXISTS device_status_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    device_id VARCHAR(64) NOT NULL COMMENT '设备ID',
    device_name VARCHAR(128) DEFAULT NULL COMMENT '设备名称',
    status INT NOT NULL COMMENT '状态 (0-离线, 1-在线, 2-报警)',
    log_time DATETIME NOT NULL COMMENT '记录时间',
    INDEX idx_status_device (device_id),
    INDEX idx_status_time (log_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='设备状态日志表';

-- =============================================
-- 每日统计汇总表
-- =============================================
CREATE TABLE IF NOT EXISTS daily_statistics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    statistics_date DATE NOT NULL COMMENT '统计日期',
    total_production INT NOT NULL DEFAULT 0 COMMENT '总生产数量',
    total_plan INT DEFAULT NULL COMMENT '总计划数量',
    completion_rate DOUBLE DEFAULT NULL COMMENT '完成率',
    qualified_quantity INT DEFAULT NULL COMMENT '良品数量',
    quality_rate DOUBLE DEFAULT NULL COMMENT '良品率',
    total_devices INT DEFAULT NULL COMMENT '设备总数',
    online_devices INT DEFAULT NULL COMMENT '在线设备数',
    alarm_count INT DEFAULT NULL COMMENT '报警次数',
    avg_oee DOUBLE DEFAULT NULL COMMENT '平均设备效率 OEE',
    total_running_minutes INT DEFAULT NULL COMMENT '总运行时长(分钟)',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE INDEX idx_stats_date (statistics_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='每日统计汇总表';

-- =============================================
-- 月度统计汇总表
-- =============================================
CREATE TABLE IF NOT EXISTS monthly_statistics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    year INT NOT NULL COMMENT '年份',
    month INT NOT NULL COMMENT '月份',
    total_production INT NOT NULL DEFAULT 0 COMMENT '总生产数量',
    total_plan INT DEFAULT NULL COMMENT '总计划数量',
    completion_rate DOUBLE DEFAULT NULL COMMENT '完成率',
    qualified_quantity INT DEFAULT NULL COMMENT '良品数量',
    quality_rate DOUBLE DEFAULT NULL COMMENT '良品率',
    avg_daily_production DOUBLE DEFAULT NULL COMMENT '平均日产量',
    max_daily_production INT DEFAULT NULL COMMENT '最高日产量',
    min_daily_production INT DEFAULT NULL COMMENT '最低日产量',
    total_alarm_count INT DEFAULT NULL COMMENT '报警总次数',
    avg_oee DOUBLE DEFAULT NULL COMMENT '平均设备效率 OEE',
    work_days INT DEFAULT NULL COMMENT '工作天数',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE INDEX idx_monthly_stats (year, month)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='月度统计汇总表';

-- =============================================
-- 插入一些示例数据（可选）
-- =============================================

-- 插入最近7天的每日统计示例数据
INSERT INTO daily_statistics (statistics_date, total_production, total_plan, completion_rate, quality_rate, total_devices, online_devices, alarm_count, avg_oee)
VALUES
    (DATE_SUB(CURDATE(), INTERVAL 6 DAY), 850, 1000, 85.0, 98.5, 10, 8, 2, 82.5),
    (DATE_SUB(CURDATE(), INTERVAL 5 DAY), 920, 1000, 92.0, 99.0, 10, 9, 1, 85.0),
    (DATE_SUB(CURDATE(), INTERVAL 4 DAY), 780, 1000, 78.0, 97.5, 10, 7, 3, 78.0),
    (DATE_SUB(CURDATE(), INTERVAL 3 DAY), 950, 1000, 95.0, 99.2, 10, 10, 0, 88.0),
    (DATE_SUB(CURDATE(), INTERVAL 2 DAY), 880, 1000, 88.0, 98.8, 10, 9, 1, 84.0),
    (DATE_SUB(CURDATE(), INTERVAL 1 DAY), 910, 1000, 91.0, 98.9, 10, 9, 2, 86.0),
    (CURDATE(), 650, 1000, 65.0, 98.5, 10, 8, 1, 80.0)
ON DUPLICATE KEY UPDATE
    total_production = VALUES(total_production),
    total_plan = VALUES(total_plan),
    completion_rate = VALUES(completion_rate);

-- 插入最近几个月的月度统计示例数据
INSERT INTO monthly_statistics (year, month, total_production, total_plan, completion_rate, quality_rate, avg_daily_production, max_daily_production, min_daily_production, total_alarm_count, avg_oee, work_days)
VALUES
    (YEAR(CURDATE()), MONTH(DATE_SUB(CURDATE(), INTERVAL 5 MONTH)), 22000, 25000, 88.0, 98.2, 880, 1050, 720, 45, 82.0, 25),
    (YEAR(CURDATE()), MONTH(DATE_SUB(CURDATE(), INTERVAL 4 MONTH)), 24500, 25000, 98.0, 99.0, 980, 1100, 850, 30, 86.0, 25),
    (YEAR(CURDATE()), MONTH(DATE_SUB(CURDATE(), INTERVAL 3 MONTH)), 23000, 25000, 92.0, 98.5, 920, 1080, 780, 38, 84.0, 25),
    (YEAR(CURDATE()), MONTH(DATE_SUB(CURDATE(), INTERVAL 2 MONTH)), 25200, 25000, 100.8, 99.1, 1008, 1150, 900, 25, 88.0, 25),
    (YEAR(CURDATE()), MONTH(DATE_SUB(CURDATE(), INTERVAL 1 MONTH)), 24000, 25000, 96.0, 98.8, 960, 1100, 820, 32, 85.0, 25),
    (YEAR(CURDATE()), MONTH(CURDATE()), 15000, 25000, 60.0, 98.5, 882, 950, 650, 20, 83.0, 17)
ON DUPLICATE KEY UPDATE
    total_production = VALUES(total_production),
    total_plan = VALUES(total_plan),
    completion_rate = VALUES(completion_rate);
