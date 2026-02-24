-- ----------------------------
-- 数据库监控规则表
-- ----------------------------
DROP TABLE IF EXISTS `sys_db_monitor_rule`;
CREATE TABLE `sys_db_monitor_rule` (
  `rule_id`       bigint(20)      NOT NULL AUTO_INCREMENT COMMENT '规则ID',
  `conn_id`       bigint(20)      DEFAULT 0 COMMENT '连接ID(0代表全局)',
  `rule_type`     varchar(50)     NOT NULL COMMENT '规则类型(slow_query/connection/table_space)',
  `metric_name`   varchar(50)     NOT NULL COMMENT '指标名称',
  `condition`     varchar(10)     NOT NULL COMMENT '条件(GT, LT, EQ)',
  `threshold`     double          NOT NULL COMMENT '阈值',
  `enabled`       char(1)         DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `create_by`     varchar(64)     DEFAULT '' COMMENT '创建者',
  `create_time`   datetime        DEFAULT NULL COMMENT '创建时间',
  `update_by`     varchar(64)     DEFAULT '' COMMENT '更新者',
  `update_time`   datetime        DEFAULT NULL COMMENT '更新时间',
  `remark`        varchar(500)    DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`rule_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='数据库监控规则表';

-- ----------------------------
-- 插入默认监控规则
-- ----------------------------
-- 慢查询阈值：执行时间超过2秒
INSERT INTO `sys_db_monitor_rule` VALUES 
(1, 0, 'slow_query', 'query_time', 'GT', 2, '0', 'admin', NOW(), '', NULL, '慢查询阈值：执行时间超过2秒');

-- 连接数阈值：最大连接数超过100
INSERT INTO `sys_db_monitor_rule` VALUES 
(2, 0, 'connection', 'max_connections', 'GT', 100, '0', 'admin', NOW(), '', NULL, '连接数阈值：最大连接数超过100');

-- 表空间阈值：单表大小超过100MB
INSERT INTO `sys_db_monitor_rule` VALUES 
(3, 0, 'table_space', 'table_size_mb', 'GT', 100, '0', 'admin', NOW(), '', NULL, '表空间阈值：单表大小超过100MB');

-- ----------------------------
-- 插入菜单
-- ----------------------------
-- 数据监控规则配置菜单（仅管理员可见）
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT '监控规则', menu_id, 5, 'rules', 'system/db/monitor/rules', 1, 0, 'C', '0', '0', 'system:db:monitor:rule', 'setting', 'admin', NOW(), '数据库监控规则配置'
FROM `sys_menu` WHERE `menu_name` = '数据监控' AND `parent_id` = 2;

-- 按钮权限
INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT '规则查询', menu_id, 1, '#', '', 1, 0, 'F', '0', '0', 'system:db:monitor:rule:query', '#', 'admin', NOW(), ''
FROM `sys_menu` WHERE `menu_name` = '监控规则';

INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT '规则新增', menu_id, 2, '#', '', 1, 0, 'F', '0', '0', 'system:db:monitor:rule:add', '#', 'admin', NOW(), ''
FROM `sys_menu` WHERE `menu_name` = '监控规则';

INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT '规则修改', menu_id, 3, '#', '', 1, 0, 'F', '0', '0', 'system:db:monitor:rule:edit', '#', 'admin', NOW(), ''
FROM `sys_menu` WHERE `menu_name` = '监控规则';

INSERT INTO `sys_menu` (`menu_name`, `parent_id`, `order_num`, `path`, `component`, `is_frame`, `is_cache`, `menu_type`, `visible`, `status`, `perms`, `icon`, `create_by`, `create_time`, `remark`)
SELECT '规则删除', menu_id, 4, '#', '', 1, 0, 'F', '0', '0', 'system:db:monitor:rule:remove', '#', 'admin', NOW(), ''
FROM `sys_menu` WHERE `menu_name` = '监控规则';
