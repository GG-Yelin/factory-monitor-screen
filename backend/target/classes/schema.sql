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
-- 用户表
-- =============================================
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    employee_no VARCHAR(50) UNIQUE COMMENT '工号',
    real_name VARCHAR(50) NOT NULL COMMENT '姓名',
    phone VARCHAR(20) DEFAULT NULL COMMENT '手机号',
    password VARCHAR(100) NOT NULL COMMENT '密码（加密存储）',
    role VARCHAR(20) NOT NULL COMMENT '角色：ADMIN-管理员, MANAGER-厂长/主管, LEADER-班组长, OPERATOR-操作工, INSPECTOR-质检员',
    team_id BIGINT DEFAULT NULL COMMENT '班组ID',
    avatar VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    status INT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用, 1-启用',
    last_login_time DATETIME DEFAULT NULL COMMENT '最后登录时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_employee_no (employee_no),
    INDEX idx_phone (phone),
    INDEX idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- =============================================
-- 班组表
-- =============================================
CREATE TABLE IF NOT EXISTS sys_team (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    name VARCHAR(100) NOT NULL COMMENT '班组名称',
    code VARCHAR(50) UNIQUE COMMENT '班组编码',
    leader_id BIGINT DEFAULT NULL COMMENT '班组长ID',
    leader_name VARCHAR(50) DEFAULT NULL COMMENT '班组长姓名',
    description VARCHAR(500) DEFAULT NULL COMMENT '描述',
    status INT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用, 1-启用',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='班组表';

-- =============================================
-- 产品表
-- =============================================
CREATE TABLE IF NOT EXISTS mes_product (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    code VARCHAR(50) NOT NULL UNIQUE COMMENT '产品编码',
    name VARCHAR(200) NOT NULL COMMENT '产品名称',
    specification VARCHAR(200) DEFAULT NULL COMMENT '规格型号',
    unit VARCHAR(20) DEFAULT NULL COMMENT '单位',
    template_id BIGINT DEFAULT NULL COMMENT '绑定的工序模板ID',
    image_url VARCHAR(500) DEFAULT NULL COMMENT '产品图片URL',
    description VARCHAR(1000) DEFAULT NULL COMMENT '描述',
    status INT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用, 1-启用',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_product_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='产品表';

-- =============================================
-- 工序模板表
-- =============================================
CREATE TABLE IF NOT EXISTS mes_process_template (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    name VARCHAR(200) NOT NULL COMMENT '模板名称',
    code VARCHAR(50) UNIQUE COMMENT '模板编码',
    description VARCHAR(1000) DEFAULT NULL COMMENT '描述',
    status INT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用, 1-启用',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='工序模板表';

-- =============================================
-- 工序步骤表
-- =============================================
CREATE TABLE IF NOT EXISTS mes_process_step (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    template_id BIGINT NOT NULL COMMENT '所属模板ID',
    name VARCHAR(200) NOT NULL COMMENT '工序名称',
    code VARCHAR(50) DEFAULT NULL COMMENT '工序编码',
    sort_order INT NOT NULL COMMENT '工序顺序',
    unit_price DECIMAL(10, 2) DEFAULT NULL COMMENT '计件单价',
    standard_time INT DEFAULT NULL COMMENT '标准工时（分钟）',
    need_inspection INT NOT NULL DEFAULT 0 COMMENT '是否需要质检：0-否, 1-是',
    inspection_standard VARCHAR(2000) DEFAULT NULL COMMENT '质检标准/要求',
    requirement VARCHAR(2000) DEFAULT NULL COMMENT '工艺要求',
    drawing_url VARCHAR(500) DEFAULT NULL COMMENT '图纸链接',
    custom_field1_name VARCHAR(50) DEFAULT NULL COMMENT '自定义字段1名称',
    custom_field1_type VARCHAR(20) DEFAULT NULL COMMENT '自定义字段1类型：text, number, select',
    custom_field1_options VARCHAR(500) DEFAULT NULL COMMENT '自定义字段1选项（JSON数组）',
    custom_field2_name VARCHAR(50) DEFAULT NULL COMMENT '自定义字段2名称',
    custom_field2_type VARCHAR(20) DEFAULT NULL COMMENT '自定义字段2类型',
    custom_field2_options VARCHAR(500) DEFAULT NULL COMMENT '自定义字段2选项',
    custom_field3_name VARCHAR(50) DEFAULT NULL COMMENT '自定义字段3名称',
    custom_field3_type VARCHAR(20) DEFAULT NULL COMMENT '自定义字段3类型',
    custom_field3_options VARCHAR(500) DEFAULT NULL COMMENT '自定义字段3选项',
    status INT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用, 1-启用',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_template_id (template_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='工序步骤表';

-- =============================================
-- 销售订单表
-- =============================================
CREATE TABLE IF NOT EXISTS mes_sales_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    order_no VARCHAR(50) NOT NULL UNIQUE COMMENT '订单号',
    product_id BIGINT NOT NULL COMMENT '产品ID',
    product_name VARCHAR(200) NOT NULL COMMENT '产品名称',
    product_code VARCHAR(50) DEFAULT NULL COMMENT '产品编码',
    quantity INT NOT NULL COMMENT '订单数量',
    completed_quantity INT NOT NULL DEFAULT 0 COMMENT '已完成数量',
    qualified_quantity INT NOT NULL DEFAULT 0 COMMENT '合格数量',
    delivery_date DATE NOT NULL COMMENT '交货日期',
    customer_name VARCHAR(200) DEFAULT NULL COMMENT '客户名称',
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT' COMMENT '订单状态：DRAFT-草稿, PENDING-待排产, SCHEDULED-已排产, IN_PROGRESS-生产中, COMPLETED-已完成, CANCELLED-已取消',
    priority INT NOT NULL DEFAULT 2 COMMENT '优先级：1-低, 2-中, 3-高, 4-紧急',
    remark VARCHAR(1000) DEFAULT NULL COMMENT '备注',
    created_by BIGINT DEFAULT NULL COMMENT '创建人ID',
    created_by_name VARCHAR(50) DEFAULT NULL COMMENT '创建人姓名',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_order_no (order_no),
    INDEX idx_order_status (status),
    INDEX idx_delivery_date (delivery_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='销售订单表';

-- =============================================
-- 派工单表
-- =============================================
CREATE TABLE IF NOT EXISTS mes_work_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    work_order_no VARCHAR(50) NOT NULL UNIQUE COMMENT '派工单号',
    sales_order_id BIGINT NOT NULL COMMENT '关联销售订单ID',
    order_no VARCHAR(50) DEFAULT NULL COMMENT '订单号（冗余）',
    product_id BIGINT NOT NULL COMMENT '产品ID',
    product_name VARCHAR(200) NOT NULL COMMENT '产品名称',
    product_code VARCHAR(50) DEFAULT NULL COMMENT '产品编码',
    plan_quantity INT NOT NULL COMMENT '计划数量',
    completed_quantity INT NOT NULL DEFAULT 0 COMMENT '已完成数量',
    qualified_quantity INT NOT NULL DEFAULT 0 COMMENT '合格数量',
    scrap_quantity INT NOT NULL DEFAULT 0 COMMENT '报废数量',
    plan_start_date DATE DEFAULT NULL COMMENT '计划开始日期',
    plan_end_date DATE DEFAULT NULL COMMENT '计划结束日期',
    actual_start_time DATETIME DEFAULT NULL COMMENT '实际开始时间',
    actual_end_time DATETIME DEFAULT NULL COMMENT '实际结束时间',
    team_id BIGINT DEFAULT NULL COMMENT '分配班组ID',
    team_name VARCHAR(100) DEFAULT NULL COMMENT '班组名称',
    device_id VARCHAR(50) DEFAULT NULL COMMENT '分配设备ID',
    device_name VARCHAR(100) DEFAULT NULL COMMENT '设备名称',
    qr_code VARCHAR(500) DEFAULT NULL COMMENT '派工单总二维码内容',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING-待开工, IN_PROGRESS-进行中, PAUSED-暂停, COMPLETED-已完成, CANCELLED-已取消',
    remark VARCHAR(1000) DEFAULT NULL COMMENT '备注',
    created_by BIGINT DEFAULT NULL COMMENT '创建人ID',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_work_order_no (work_order_no),
    INDEX idx_sales_order_id (sales_order_id),
    INDEX idx_work_order_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='派工单表';

-- =============================================
-- 派工单工序表
-- =============================================
CREATE TABLE IF NOT EXISTS mes_work_order_process (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    work_order_id BIGINT NOT NULL COMMENT '派工单ID',
    work_order_no VARCHAR(50) DEFAULT NULL COMMENT '派工单号（冗余）',
    process_step_id BIGINT NOT NULL COMMENT '工序模板步骤ID',
    process_name VARCHAR(200) NOT NULL COMMENT '工序名称',
    sort_order INT NOT NULL COMMENT '工序顺序',
    plan_quantity INT NOT NULL COMMENT '计划数量',
    completed_quantity INT NOT NULL DEFAULT 0 COMMENT '已完成数量',
    qualified_quantity INT NOT NULL DEFAULT 0 COMMENT '合格数量',
    scrap_quantity INT NOT NULL DEFAULT 0 COMMENT '报废数量',
    unit_price DECIMAL(10, 2) DEFAULT NULL COMMENT '计件单价',
    operator_id BIGINT DEFAULT NULL COMMENT '负责人ID（操作工）',
    operator_name VARCHAR(50) DEFAULT NULL COMMENT '负责人姓名',
    need_inspection INT NOT NULL DEFAULT 0 COMMENT '是否需要质检',
    inspector_id BIGINT DEFAULT NULL COMMENT '质检员ID',
    inspector_name VARCHAR(50) DEFAULT NULL COMMENT '质检员姓名',
    inspection_status VARCHAR(20) DEFAULT NULL COMMENT '质检状态：PENDING-待质检, PASSED-已通过, FAILED-不通过',
    qr_code VARCHAR(500) DEFAULT NULL COMMENT '工序二维码内容',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态：PENDING-待开工, IN_PROGRESS-进行中, COMPLETED-已完成, PAUSED-暂停',
    actual_start_time DATETIME DEFAULT NULL COMMENT '实际开始时间',
    actual_end_time DATETIME DEFAULT NULL COMMENT '实际结束时间',
    total_minutes INT DEFAULT 0 COMMENT '总工时（分钟）',
    remark VARCHAR(1000) DEFAULT NULL COMMENT '备注',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_wop_work_order_id (work_order_id),
    INDEX idx_wop_operator_id (operator_id),
    INDEX idx_wop_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='派工单工序表';

-- =============================================
-- 报工记录表
-- =============================================
CREATE TABLE IF NOT EXISTS mes_work_report (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    work_order_process_id BIGINT NOT NULL COMMENT '派工单工序ID',
    work_order_id BIGINT NOT NULL COMMENT '派工单ID',
    work_order_no VARCHAR(50) DEFAULT NULL COMMENT '派工单号',
    process_name VARCHAR(200) DEFAULT NULL COMMENT '工序名称',
    operator_id BIGINT NOT NULL COMMENT '操作工ID',
    operator_name VARCHAR(50) NOT NULL COMMENT '操作工姓名',
    completed_quantity INT NOT NULL COMMENT '完成数量（合格数量）',
    scrap_quantity INT NOT NULL DEFAULT 0 COMMENT '报废数量',
    work_minutes INT NOT NULL DEFAULT 0 COMMENT '工时（分钟）',
    unit_price DECIMAL(10, 2) DEFAULT NULL COMMENT '计件单价',
    wage DECIMAL(10, 2) DEFAULT NULL COMMENT '应得工资',
    start_time DATETIME DEFAULT NULL COMMENT '开始时间（计时报工）',
    end_time DATETIME DEFAULT NULL COMMENT '结束时间（计时报工）',
    report_time DATETIME NOT NULL COMMENT '报工时间',
    custom_field1_value VARCHAR(200) DEFAULT NULL COMMENT '自定义字段1值',
    custom_field2_value VARCHAR(200) DEFAULT NULL COMMENT '自定义字段2值',
    custom_field3_value VARCHAR(200) DEFAULT NULL COMMENT '自定义字段3值',
    remark VARCHAR(1000) DEFAULT NULL COMMENT '备注',
    source VARCHAR(20) DEFAULT 'SCAN' COMMENT '报工来源：SCAN-扫码, MANUAL-手动',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_wr_process_id (work_order_process_id),
    INDEX idx_wr_operator_id (operator_id),
    INDEX idx_wr_report_time (report_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='报工记录表';

-- =============================================
-- 质检记录表
-- =============================================
CREATE TABLE IF NOT EXISTS mes_quality_inspection (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    work_order_id BIGINT NOT NULL COMMENT '派工单ID',
    work_order_no VARCHAR(50) DEFAULT NULL COMMENT '派工单号',
    work_order_process_id BIGINT DEFAULT NULL COMMENT '派工单工序ID（工序质检时使用）',
    process_name VARCHAR(200) DEFAULT NULL COMMENT '工序名称',
    inspection_type VARCHAR(20) NOT NULL COMMENT '质检类型：PROCESS-工序质检, FINAL-最终质检',
    inspector_id BIGINT NOT NULL COMMENT '质检员ID',
    inspector_name VARCHAR(50) NOT NULL COMMENT '质检员姓名',
    inspected_quantity INT NOT NULL COMMENT '送检数量',
    qualified_quantity INT NOT NULL COMMENT '合格数量',
    unqualified_quantity INT NOT NULL DEFAULT 0 COMMENT '不合格数量',
    unqualified_reason VARCHAR(1000) DEFAULT NULL COMMENT '不合格原因',
    image_urls VARCHAR(2000) DEFAULT NULL COMMENT '不合格品图片URL（JSON数组）',
    result VARCHAR(20) NOT NULL COMMENT '质检结果：PASSED-通过, FAILED-不通过',
    inspection_time DATETIME NOT NULL COMMENT '质检时间',
    remark VARCHAR(1000) DEFAULT NULL COMMENT '备注',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_qi_work_order_id (work_order_id),
    INDEX idx_qi_inspector_id (inspector_id),
    INDEX idx_qi_inspection_time (inspection_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='质检记录表';

-- =============================================
-- 物料表
-- =============================================
CREATE TABLE IF NOT EXISTS mes_material (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    code VARCHAR(50) NOT NULL UNIQUE COMMENT '物料编码',
    name VARCHAR(200) NOT NULL COMMENT '物料名称',
    specification VARCHAR(200) DEFAULT NULL COMMENT '规格型号',
    unit VARCHAR(20) DEFAULT NULL COMMENT '单位',
    stock_quantity DECIMAL(12, 2) NOT NULL DEFAULT 0 COMMENT '当前库存数量',
    warning_threshold DECIMAL(12, 2) DEFAULT NULL COMMENT '库存预警阈值',
    unit_price DECIMAL(10, 2) DEFAULT NULL COMMENT '单价',
    location VARCHAR(200) DEFAULT NULL COMMENT '存放位置',
    description VARCHAR(1000) DEFAULT NULL COMMENT '描述',
    status INT NOT NULL DEFAULT 1 COMMENT '状态：0-禁用, 1-启用',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_material_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='物料表';

-- =============================================
-- 物料BOM表（物料清单）
-- =============================================
CREATE TABLE IF NOT EXISTS mes_material_bom (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    product_id BIGINT NOT NULL COMMENT '产品ID',
    material_id BIGINT NOT NULL COMMENT '物料ID',
    material_name VARCHAR(200) DEFAULT NULL COMMENT '物料名称（冗余）',
    material_code VARCHAR(50) DEFAULT NULL COMMENT '物料编码（冗余）',
    quantity DECIMAL(12, 4) NOT NULL COMMENT '单位用量',
    unit VARCHAR(20) DEFAULT NULL COMMENT '单位',
    remark VARCHAR(500) DEFAULT NULL COMMENT '备注',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_bom_product_id (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='物料BOM表';

-- =============================================
-- 工资记录表
-- =============================================
CREATE TABLE IF NOT EXISTS mes_wage_record (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '员工ID',
    user_name VARCHAR(50) NOT NULL COMMENT '员工姓名',
    employee_no VARCHAR(50) DEFAULT NULL COMMENT '员工工号',
    work_date DATE NOT NULL COMMENT '工作日期',
    work_report_id BIGINT DEFAULT NULL COMMENT '关联报工记录ID',
    work_order_no VARCHAR(50) DEFAULT NULL COMMENT '派工单号',
    product_name VARCHAR(200) DEFAULT NULL COMMENT '产品名称',
    process_name VARCHAR(200) DEFAULT NULL COMMENT '工序名称',
    quantity INT NOT NULL COMMENT '完成数量',
    unit_price DECIMAL(10, 2) NOT NULL COMMENT '计件单价',
    wage DECIMAL(10, 2) NOT NULL COMMENT '应得工资',
    wage_type VARCHAR(20) DEFAULT 'PIECE' COMMENT '工资类型：PIECE-计件, TIME-计时',
    status VARCHAR(20) DEFAULT 'PENDING' COMMENT '状态：PENDING-待结算, SETTLED-已结算',
    settled_at DATETIME DEFAULT NULL COMMENT '结算时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_wage_user_id (user_id),
    INDEX idx_wage_date (work_date),
    INDEX idx_wage_user_date (user_id, work_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='工资记录表';

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
