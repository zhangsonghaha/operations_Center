-- ----------------------------
-- 部署日志表
-- ----------------------------
DROP TABLE IF EXISTS `t_ops_deploy_log`;
CREATE TABLE `t_ops_deploy_log` (
  `log_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `app_id` bigint(20) DEFAULT NULL COMMENT '应用ID',
  `app_name` varchar(100) DEFAULT NULL COMMENT '应用名称',
  `server_id` bigint(20) DEFAULT NULL COMMENT '服务器ID',
  `server_name` varchar(100) DEFAULT NULL COMMENT '服务器名称',
  `deploy_type` varchar(20) DEFAULT NULL COMMENT '部署类型(deploy-部署, start-启动, stop-停止, restart-重启)',
  `deploy_status` varchar(20) DEFAULT NULL COMMENT '部署状态(running-进行中, success-成功, failed-失败)',
  `log_content` longtext COMMENT '日志内容',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `executor` varchar(50) DEFAULT NULL COMMENT '执行人',
  `error_msg` text COMMENT '错误信息',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`log_id`),
  KEY `idx_app_id` (`app_id`),
  KEY `idx_server_id` (`server_id`),
  KEY `idx_deploy_status` (`deploy_status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='部署日志表';

-- ----------------------------
-- 菜单权限 SQL
-- ----------------------------
-- 部署日志菜单
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('部署日志', 2000, 3, 'deployLog', 'ops/deployLog/index', 1, 0, 'C', '0', '0', 'ops:deployLog:list', 'log', 'admin', sysdate(), '', null, '部署日志菜单');

-- 部署日志按钮权限
SET @parentId = LAST_INSERT_ID();

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('部署日志查询', @parentId, 1, '#', '', 1, 0, 'F', '0', '0', 'ops:deployLog:query', '#', 'admin', sysdate(), '', null, '');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('部署日志删除', @parentId, 2, '#', '', 1, 0, 'F', '0', '0', 'ops:deployLog:remove', '#', 'admin', sysdate(), '', null, '');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES ('应用部署', @parentId, 3, '#', '', 1, 0, 'F', '0', '0', 'ops:app:deploy', '#', 'admin', sysdate(), '', null, '');
