-- ----------------------------
-- 1. 批量命令模板表
-- ----------------------------
DROP TABLE IF EXISTS `sys_command_template`;
CREATE TABLE `sys_command_template` (
  `template_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '模板ID',
  `template_name` varchar(64) NOT NULL COMMENT '模板名称',
  `command_content` text NOT NULL COMMENT '脚本/命令内容',
  `timeout` int(11) DEFAULT 60 COMMENT '超时时间(秒)',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`template_id`)
) ENGINE=InnoDB COMMENT='批量命令模板表';

-- ----------------------------
-- 2. 批量任务主表
-- ----------------------------
DROP TABLE IF EXISTS `sys_batch_task`;
CREATE TABLE `sys_batch_task` (
  `task_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '任务ID',
  `task_name` varchar(100) NOT NULL COMMENT '任务名称',
  `task_type` char(1) NOT NULL DEFAULT '1' COMMENT '任务类型（1命令执行 2文件分发）',
  `command_content` text COMMENT '执行命令内容',
  `source_file` varchar(500) COMMENT '源文件路径',
  `dest_path` varchar(500) COMMENT '目标路径',
  `status` char(1) DEFAULT '0' COMMENT '状态（0等待 1执行中 2完成 3失败 4部分成功）',
  `total_host` int(11) DEFAULT 0 COMMENT '总主机数',
  `success_host` int(11) DEFAULT 0 COMMENT '成功主机数',
  `fail_host` int(11) DEFAULT 0 COMMENT '失败主机数',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `start_time` datetime DEFAULT NULL COMMENT '开始执行时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束执行时间',
  PRIMARY KEY (`task_id`)
) ENGINE=InnoDB COMMENT='批量任务主表';

-- ----------------------------
-- 3. 批量任务明细表
-- ----------------------------
DROP TABLE IF EXISTS `sys_batch_task_detail`;
CREATE TABLE `sys_batch_task_detail` (
  `detail_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '明细ID',
  `task_id` bigint(20) NOT NULL COMMENT '任务ID',
  `server_id` bigint(20) NOT NULL COMMENT '服务器ID',
  `server_name` varchar(64) DEFAULT NULL COMMENT '服务器名称快照',
  `server_ip` varchar(64) DEFAULT NULL COMMENT '服务器IP快照',
  `status` char(1) DEFAULT '0' COMMENT '执行状态（0等待 1执行中 2成功 3失败）',
  `exit_code` int(11) DEFAULT NULL COMMENT '退出码',
  `execution_log` longtext COMMENT '执行日志',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  PRIMARY KEY (`detail_id`),
  KEY `idx_task_id` (`task_id`)
) ENGINE=InnoDB COMMENT='批量任务明细表';

-- ----------------------------
-- 4. 插入菜单 (批量操作管理)
-- ----------------------------
-- 假设父菜单 '运维管理' ID 为 2015
INSERT INTO `sys_menu` VALUES (2040, '批量操作', 2015, 5, 'batch', 'ops/batch/index', NULL, '1', 0, 'C', '0', '0', 'ops:batch:list', 'code', 'admin', sysdate(), '', NULL, '批量操作管理菜单');
INSERT INTO `sys_menu` VALUES (2041, '命令模板', 2040, 1, '#', '', NULL, '1', 0, 'F', '0', '0', 'ops:batch:template', '#', 'admin', sysdate(), '', NULL, '');
INSERT INTO `sys_menu` VALUES (2042, '任务执行', 2040, 2, '#', '', NULL, '1', 0, 'F', '0', '0', 'ops:batch:execute', '#', 'admin', sysdate(), '', NULL, '');
