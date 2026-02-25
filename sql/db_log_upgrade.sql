-- ============================================
-- 数据库操作日志功能升级脚本
-- 支持备份、恢复操作的日志记录
-- ============================================

-- 1. 扩展日志表，添加操作类型和影响行数字段
ALTER TABLE `sys_db_log` 
ADD COLUMN `operation_type` VARCHAR(50) DEFAULT 'EXECUTE' COMMENT '操作类型(BACKUP备份/RESTORE恢复/EXECUTE执行)',
ADD COLUMN `affected_rows` INT DEFAULT 0 COMMENT '影响行数',
ADD INDEX `idx_operation_type` (`operation_type`),
ADD INDEX `idx_create_time` (`create_time`);

-- 2. 更新现有数据的操作类型
UPDATE `sys_db_log` SET `operation_type` = 'EXECUTE' WHERE `operation_type` IS NULL OR `operation_type` = '';

-- 3. 添加菜单（如果不存在）
-- 检查菜单是否已存在
SELECT @menuCount := COUNT(*) FROM `sys_menu` WHERE `menu_name` = '操作日志' AND `parent_id` = (SELECT `menu_id` FROM `sys_menu` WHERE `component` = 'system/db/index');

-- 获取数据库管理父菜单ID
SELECT @dbParentId := `menu_id` FROM `sys_menu` WHERE `component` = 'system/db/index' LIMIT 1;

-- 插入操作日志菜单（如果父菜单存在）
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT '操作日志', @dbParentId, 3, 'db-log', 'system/db/log/index', 1, 0, 'C', '0', '0', 'system:db:log:list', 'el-icon-document', 'admin', NOW(), '数据库操作日志'
WHERE @dbParentId IS NOT NULL AND NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `component` = 'system/db/log/index');

SET @logParentId = (SELECT `menu_id` FROM `sys_menu` WHERE `component` = 'system/db/log/index' LIMIT 1);

-- 添加操作日志按钮权限
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT '查询', @logParentId, 1, '#', '', 1, 0, 'F', '0', '0', 'system:db:log:query', '#', 'admin', NOW(), ''
WHERE @logParentId IS NOT NULL AND NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `perms` = 'system:db:log:query');

INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT '删除', @logParentId, 2, '#', '', 1, 0, 'F', '0', '0', 'system:db:log:remove', '#', 'admin', NOW(), ''
WHERE @logParentId IS NOT NULL AND NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `perms` = 'system:db:log:remove');

INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT '导出', @logParentId, 3, '#', '', 1, 0, 'F', '0', '0', 'system:db:log:export', '#', 'admin', NOW(), ''
WHERE @logParentId IS NOT NULL AND NOT EXISTS (SELECT 1 FROM `sys_menu` WHERE `perms` = 'system:db:log:export');
