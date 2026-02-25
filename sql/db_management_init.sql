-- ============================================
-- 数据库管理功能完整初始化脚本
-- 包含：数据库连接、备份、日志、监控规则等表
-- ============================================

-- 1. 数据库连接配置表
DROP TABLE IF EXISTS `sys_db_conn`;
CREATE TABLE `sys_db_conn` (
  `conn_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '连接ID',
  `conn_name` varchar(100) NOT NULL COMMENT '连接名称',
  `db_type` varchar(20) DEFAULT 'mysql' COMMENT '数据库类型',
  `host` varchar(100) DEFAULT 'localhost' COMMENT '主机地址',
  `port` varchar(10) DEFAULT '3306' COMMENT '端口',
  `username` varchar(100) DEFAULT NULL COMMENT '账号',
  `password` varchar(100) DEFAULT NULL COMMENT '密码',
  `db_name` varchar(100) DEFAULT NULL COMMENT '数据库名',
  `status` char(1) DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`conn_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='数据库连接配置表';

-- 2. 数据库备份记录表
DROP TABLE IF EXISTS `sys_db_backup`;
CREATE TABLE `sys_db_backup` (
  `backup_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '备份ID',
  `conn_id` bigint(20) NOT NULL COMMENT '连接ID',
  `file_name` varchar(500) NOT NULL DEFAULT '' COMMENT '备份文件名',
  `file_path` varchar(500) NOT NULL COMMENT '文件存储路径',
  `backup_type` char(1) DEFAULT '0' COMMENT '备份类型（0手动 1自动）',
  `status` char(1) DEFAULT '0' COMMENT '状态（0成功 1失败）',
  `log_msg` text COMMENT '日志信息',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `db_type` varchar(20) DEFAULT 'mysql' COMMENT '数据库类型(mysql/postgresql/mongodb/redis)',
  `backup_mode` varchar(20) DEFAULT 'full' COMMENT '备份方式(full全量/incremental增量/differential差异)',
  `backup_level` varchar(20) DEFAULT 'database' COMMENT '备份级别(instance实例/database数据库/table表)',
  `target_name` text COMMENT '备份目标',
  `file_size` bigint(20) DEFAULT 0 COMMENT '文件大小(字节)',
  `file_md5` varchar(64) DEFAULT NULL COMMENT '文件MD5校验值',
  `storage_type` varchar(20) DEFAULT 'local' COMMENT '存储类型(local本地/remote远程/cloud云存储)',
  `storage_config` varchar(1000) DEFAULT NULL COMMENT '存储配置(JSON格式)',
  `verify_status` char(1) DEFAULT '0' COMMENT '验证状态(0未验证/1验证成功/2验证失败)',
  `verify_msg` varchar(500) DEFAULT NULL COMMENT '验证信息',
  `expire_time` datetime DEFAULT NULL COMMENT '过期时间',
  `is_deleted` char(1) DEFAULT '0' COMMENT '是否删除(0正常/1已删除)',
  PRIMARY KEY (`backup_id`),
  KEY `idx_db_type` (`db_type`),
  KEY `idx_backup_mode` (`backup_mode`),
  KEY `idx_status` (`status`),
  KEY `idx_expire_time` (`expire_time`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='数据库备份记录表';

-- 3. 数据库操作日志表
DROP TABLE IF EXISTS `sys_db_log`;
CREATE TABLE `sys_db_log` (
  `log_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `conn_id` bigint(20) DEFAULT NULL COMMENT '连接ID',
  `operation_type` varchar(50) DEFAULT 'EXECUTE' COMMENT '操作类型(BACKUP备份/RESTORE恢复/EXECUTE执行)',
  `sql_content` text COMMENT '执行语句',
  `affected_rows` int(11) DEFAULT 0 COMMENT '影响行数',
  `cost_time` bigint(20) DEFAULT NULL COMMENT '耗时(ms)',
  `status` char(1) DEFAULT '0' COMMENT '状态（0成功 1失败 2进行中）',
  `error_msg` varchar(2000) DEFAULT NULL COMMENT '错误信息',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`log_id`),
  KEY `idx_operation_type` (`operation_type`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='数据库操作日志表';

-- 4. 数据库监控规则表
DROP TABLE IF EXISTS `sys_db_monitor_rule`;
CREATE TABLE `sys_db_monitor_rule` (
  `rule_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '规则ID',
  `conn_id` bigint(20) DEFAULT 0 COMMENT '连接ID(0代表全局)',
  `rule_type` varchar(50) NOT NULL COMMENT '规则类型(slow_query/connection/table_space)',
  `metric_name` varchar(50) NOT NULL COMMENT '指标名称',
  `condition` varchar(10) NOT NULL COMMENT '条件(GT, LT, EQ)',
  `threshold` double NOT NULL COMMENT '阈值',
  `enabled` char(1) DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`rule_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='数据库监控规则表';

-- 5. 数据库备份策略表
DROP TABLE IF EXISTS `sys_db_backup_strategy`;
CREATE TABLE `sys_db_backup_strategy` (
  `strategy_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '策略ID',
  `strategy_name` varchar(100) NOT NULL COMMENT '策略名称',
  `conn_id` bigint(20) NOT NULL COMMENT '连接ID',
  `db_type` varchar(20) DEFAULT 'mysql' COMMENT '数据库类型',
  `backup_mode` varchar(20) DEFAULT 'full' COMMENT '备份方式',
  `backup_level` varchar(20) DEFAULT 'database' COMMENT '备份级别',
  `target_name` text COMMENT '备份目标',
  `cron_expression` varchar(100) DEFAULT NULL COMMENT 'Cron表达式(定时备份)',
  `enabled` char(1) DEFAULT '0' COMMENT '状态(0启用/1停用)',
  `retention_days` int(11) DEFAULT 7 COMMENT '保留天数',
  `retention_count` int(11) DEFAULT 10 COMMENT '保留数量',
  `compress_enabled` char(1) DEFAULT '1' COMMENT '是否压缩(0否/1是)',
  `compress_type` varchar(20) DEFAULT 'gzip' COMMENT '压缩类型(gzip/bzip2)',
  `encrypt_enabled` char(1) DEFAULT '0' COMMENT '是否加密(0否/1是)',
  `storage_type` varchar(20) DEFAULT 'local' COMMENT '存储类型',
  `storage_config` varchar(1000) DEFAULT NULL COMMENT '存储配置(JSON)',
  `alert_enabled` char(1) DEFAULT '0' COMMENT '是否告警(0否/1是)',
  `alert_config` varchar(500) DEFAULT NULL COMMENT '告警配置(JSON)',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`strategy_id`),
  KEY `idx_conn_id` (`conn_id`),
  KEY `idx_enabled` (`enabled`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='数据库备份策略表';

-- 6. 数据库备份任务日志表
DROP TABLE IF EXISTS `sys_db_backup_log`;
CREATE TABLE `sys_db_backup_log` (
  `log_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `backup_id` bigint(20) DEFAULT NULL COMMENT '备份ID',
  `strategy_id` bigint(20) DEFAULT NULL COMMENT '策略ID',
  `conn_id` bigint(20) NOT NULL COMMENT '连接ID',
  `db_type` varchar(20) DEFAULT 'mysql' COMMENT '数据库类型',
  `operation_type` varchar(50) NOT NULL COMMENT '操作类型(BACKUP/RESTORE/VERIFY/DELETE)',
  `operation_status` char(1) DEFAULT '0' COMMENT '状态(0成功/1失败/2进行中)',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `duration` int(11) DEFAULT 0 COMMENT '耗时(秒)',
  `file_size` bigint(20) DEFAULT 0 COMMENT '文件大小',
  `message` varchar(2000) DEFAULT NULL COMMENT '详细信息',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`log_id`),
  KEY `idx_backup_id` (`backup_id`),
  KEY `idx_strategy_id` (`strategy_id`),
  KEY `idx_conn_id` (`conn_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='数据库备份任务日志表';

-- ============================================
-- 插入默认数据
-- ============================================

-- 插入默认监控规则
INSERT INTO `sys_db_monitor_rule` VALUES 
(1, 0, 'slow_query', 'query_time', 'GT', 2, '0', 'admin', NOW(), '', NULL, '慢查询阈值：执行时间超过2秒'),
(2, 0, 'connection', 'max_connections', 'GT', 100, '0', 'admin', NOW(), '', NULL, '连接数阈值：最大连接数超过100'),
(3, 0, 'table_space', 'table_size_mb', 'GT', 100, '0', 'admin', NOW(), '', NULL, '表空间阈值：单表大小超过100MB');
