-- ============================================
-- 数据库备份功能升级脚本
-- 支持多数据库类型、备份策略、存储配置等
-- ============================================

-- 1. 扩展备份记录表
ALTER TABLE `sys_db_backup` 
MODIFY COLUMN `file_name` VARCHAR(500) NOT NULL DEFAULT '' COMMENT '备份文件名',
ADD COLUMN `db_type` VARCHAR(20) DEFAULT 'mysql' COMMENT '数据库类型(mysql/postgresql/mongodb/redis)',
ADD COLUMN `backup_mode` VARCHAR(20) DEFAULT 'full' COMMENT '备份方式(full全量/incremental增量/differential差异)',
ADD COLUMN `backup_level` VARCHAR(20) DEFAULT 'database' COMMENT '备份级别(instance实例/database数据库/table表)',
ADD COLUMN `target_name` TEXT COMMENT '备份目标(数据库名/表名，多个用逗号分隔)',
ADD COLUMN `file_size` BIGINT DEFAULT 0 COMMENT '文件大小(字节)',
ADD COLUMN `file_md5` VARCHAR(64) DEFAULT NULL COMMENT '文件MD5校验值',
ADD COLUMN `storage_type` VARCHAR(20) DEFAULT 'local' COMMENT '存储类型(local本地/remote远程/cloud云存储)',
ADD COLUMN `storage_config` VARCHAR(1000) DEFAULT NULL COMMENT '存储配置(JSON格式)',
ADD COLUMN `verify_status` CHAR(1) DEFAULT '0' COMMENT '验证状态(0未验证/1验证成功/2验证失败)',
ADD COLUMN `verify_msg` VARCHAR(500) DEFAULT NULL COMMENT '验证信息',
ADD COLUMN `expire_time` DATETIME DEFAULT NULL COMMENT '过期时间',
ADD COLUMN `is_deleted` CHAR(1) DEFAULT '0' COMMENT '是否删除(0正常/1已删除)',
ADD INDEX `idx_db_type` (`db_type`),
ADD INDEX `idx_backup_mode` (`backup_mode`),
ADD INDEX `idx_status` (`status`),
ADD INDEX `idx_expire_time` (`expire_time`);

-- 2. 创建备份策略表
DROP TABLE IF EXISTS `sys_db_backup_strategy`;
CREATE TABLE `sys_db_backup_strategy` (
  `strategy_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '策略ID',
  `strategy_name` VARCHAR(100) NOT NULL COMMENT '策略名称',
  `conn_id` BIGINT NOT NULL COMMENT '连接ID',
  `db_type` VARCHAR(20) DEFAULT 'mysql' COMMENT '数据库类型',
  `backup_mode` VARCHAR(20) DEFAULT 'full' COMMENT '备份方式',
  `backup_level` VARCHAR(20) DEFAULT 'database' COMMENT '备份级别',
  `target_name` TEXT COMMENT '备份目标',
  `cron_expression` VARCHAR(100) DEFAULT NULL COMMENT 'Cron表达式(定时备份)',
  `enabled` CHAR(1) DEFAULT '0' COMMENT '状态(0启用/1停用)',
  `retention_days` INT DEFAULT 7 COMMENT '保留天数',
  `retention_count` INT DEFAULT 10 COMMENT '保留数量',
  `compress_enabled` CHAR(1) DEFAULT '1' COMMENT '是否压缩(0否/1是)',
  `compress_type` VARCHAR(20) DEFAULT 'gzip' COMMENT '压缩类型(gzip/bzip2)',
  `encrypt_enabled` CHAR(1) DEFAULT '0' COMMENT '是否加密(0否/1是)',
  `storage_type` VARCHAR(20) DEFAULT 'local' COMMENT '存储类型',
  `storage_config` VARCHAR(1000) DEFAULT NULL COMMENT '存储配置(JSON)',
  `alert_enabled` CHAR(1) DEFAULT '0' COMMENT '是否告警(0否/1是)',
  `alert_config` VARCHAR(500) DEFAULT NULL COMMENT '告警配置(JSON)',
  `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `create_by` VARCHAR(64) DEFAULT '' COMMENT '创建者',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  `update_by` VARCHAR(64) DEFAULT '' COMMENT '更新者',
  `update_time` DATETIME DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`strategy_id`),
  INDEX `idx_conn_id` (`conn_id`),
  INDEX `idx_enabled` (`enabled`)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='数据库备份策略表';

-- 3. 创建备份任务日志表
DROP TABLE IF EXISTS `sys_db_backup_log`;
CREATE TABLE `sys_db_backup_log` (
  `log_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `backup_id` BIGINT DEFAULT NULL COMMENT '备份ID',
  `strategy_id` BIGINT DEFAULT NULL COMMENT '策略ID',
  `conn_id` BIGINT NOT NULL COMMENT '连接ID',
  `db_type` VARCHAR(20) DEFAULT 'mysql' COMMENT '数据库类型',
  `operation_type` VARCHAR(50) NOT NULL COMMENT '操作类型(BACKUP/RESTORE/VERIFY/DELETE)',
  `operation_status` CHAR(1) DEFAULT '0' COMMENT '状态(0成功/1失败/2进行中)',
  `start_time` DATETIME DEFAULT NULL COMMENT '开始时间',
  `end_time` DATETIME DEFAULT NULL COMMENT '结束时间',
  `duration` INT DEFAULT 0 COMMENT '耗时(秒)',
  `file_size` BIGINT DEFAULT 0 COMMENT '文件大小',
  `message` VARCHAR(2000) DEFAULT NULL COMMENT '详细信息',
  `error_msg` VARCHAR(2000) DEFAULT NULL COMMENT '错误信息',
  `create_by` VARCHAR(64) DEFAULT '' COMMENT '操作人',
  `create_time` DATETIME DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`log_id`),
  INDEX `idx_backup_id` (`backup_id`),
  INDEX `idx_conn_id` (`conn_id`),
  INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='数据库备份日志表';

-- 4. 添加字典数据
INSERT INTO `sys_dict_type` (`dict_name`, `dict_type`, `status`, `create_by`, `create_time`, `remark`) VALUES
('数据库类型', 'db_type', '0', 'admin', NOW(), '支持的数据库类型'),
('备份方式', 'backup_mode', '0', 'admin', NOW(), '备份方式类型'),
('备份级别', 'backup_level', '0', 'admin', NOW(), '备份级别类型'),
('存储类型', 'storage_type', '0', 'admin', NOW(), '备份文件存储类型'),
('压缩类型', 'compress_type', '0', 'admin', NOW(), '备份文件压缩类型');

-- 数据库类型
INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`) VALUES
(1, 'MySQL/MariaDB', 'mysql', 'db_type', '', 'primary', 'Y', '0', 'admin', NOW()),
(2, 'PostgreSQL', 'postgresql', 'db_type', '', 'success', 'N', '0', 'admin', NOW()),
(3, 'MongoDB', 'mongodb', 'db_type', '', 'warning', 'N', '0', 'admin', NOW()),
(4, 'Redis', 'redis', 'db_type', '', 'danger', 'N', '0', 'admin', NOW()),
(5, 'Oracle', 'oracle', 'db_type', '', 'info', 'N', '0', 'admin', NOW()),
(6, 'SQL Server', 'sqlserver', 'db_type', '', 'info', 'N', '0', 'admin', NOW());

-- 备份方式
INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`) VALUES
(1, '全量备份', 'full', 'backup_mode', '', 'primary', 'Y', '0', 'admin', NOW()),
(2, '增量备份', 'incremental', 'backup_mode', '', 'success', 'N', '0', 'admin', NOW()),
(3, '差异备份', 'differential', 'backup_mode', '', 'warning', 'N', '0', 'admin', NOW());

-- 备份级别
INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`) VALUES
(1, '实例级', 'instance', 'backup_level', '', 'danger', 'N', '0', 'admin', NOW()),
(2, '数据库级', 'database', 'backup_level', '', 'primary', 'Y', '0', 'admin', NOW()),
(3, '表级', 'table', 'backup_level', '', 'success', 'N', '0', 'admin', NOW());

-- 存储类型
INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`) VALUES
(1, '本地存储', 'local', 'storage_type', '', 'primary', 'Y', '0', 'admin', NOW()),
(2, 'FTP服务器', 'ftp', 'storage_type', '', 'success', 'N', '0', 'admin', NOW()),
(3, 'SFTP服务器', 'sftp', 'storage_type', '', 'success', 'N', '0', 'admin', NOW()),
(4, '阿里云OSS', 'aliyun_oss', 'storage_type', '', 'warning', 'N', '0', 'admin', NOW()),
(5, '腾讯云COS', 'tencent_cos', 'storage_type', '', 'warning', 'N', '0', 'admin', NOW()),
(6, 'AWS S3', 's3', 'storage_type', '', 'warning', 'N', '0', 'admin', NOW());

-- 压缩类型
INSERT INTO `sys_dict_data` (`dict_sort`, `dict_label`, `dict_value`, `dict_type`, `css_class`, `list_class`, `is_default`, `status`, `create_by`, `create_time`) VALUES
(1, 'Gzip', 'gzip', 'compress_type', '', 'primary', 'Y', '0', 'admin', NOW()),
(2, 'Bzip2', 'bzip2', 'compress_type', '', 'success', 'N', '0', 'admin', NOW()),
(3, 'Zip', 'zip', 'compress_type', '', 'warning', 'N', '0', 'admin', NOW());

-- 5. 添加菜单
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`) VALUES
('备份策略', 0, 2, 'backup-strategy', 'system/db/backupStrategy/index', 1, 0, 'C', '0', '0', 'system:db:backupStrategy:list', 'el-icon-setting', 'admin', NOW(), '数据库备份策略配置');

SET @parentId = LAST_INSERT_ID();

INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`) VALUES
('查询', @parentId, 1, '#', '', 1, 0, 'F', '0', '0', 'system:db:backupStrategy:query', '#', 'admin', NOW(), ''),
('新增', @parentId, 2, '#', '', 1, 0, 'F', '0', '0', 'system:db:backupStrategy:add', '#', 'admin', NOW(), ''),
('修改', @parentId, 3, '#', '', 1, 0, 'F', '0', '0', 'system:db:backupStrategy:edit', '#', 'admin', NOW(), ''),
('删除', @parentId, 4, '#', '', 1, 0, 'F', '0', '0', 'system:db:backupStrategy:remove', '#', 'admin', NOW(), '');

-- 6. 修改target_name字段为TEXT类型以支持更多表名
ALTER TABLE `sys_db_backup` MODIFY COLUMN `target_name` TEXT COMMENT '备份目标(数据库名/表名，多个用逗号分隔)';
ALTER TABLE `sys_db_backup_strategy` MODIFY COLUMN `target_name` TEXT COMMENT '备份目标';
