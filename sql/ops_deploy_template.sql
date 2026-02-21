-- ----------------------------
-- 1. 部署模板表
-- ----------------------------
DROP TABLE IF EXISTS `ops_deploy_template`;
CREATE TABLE `ops_deploy_template` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `template_name` varchar(100) NOT NULL COMMENT '模板名称',
  `app_type` varchar(50) NOT NULL COMMENT '适用应用类型(Java/Node/Python/Static)',
  `script_content` text NOT NULL COMMENT '脚本内容(YAML)',
  `version` varchar(20) NOT NULL DEFAULT 'v1.0.0' COMMENT '当前版本',
  `built_in` char(1) DEFAULT '0' COMMENT '内置标志(1是 0否)',
  `status` char(1) DEFAULT '0' COMMENT '状态(0正常 1停用)',
  `sha256` varchar(64) DEFAULT '' COMMENT '脚本SHA256',
  `remark` varchar(500) DEFAULT '' COMMENT '备注',
  `creator` varchar(64) DEFAULT '' COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新人',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_name_version` (`template_name`, `version`),
  KEY `idx_app_type_status` (`app_type`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部署模板表';

-- ----------------------------
-- 2. 部署模板版本表
-- ----------------------------
DROP TABLE IF EXISTS `ops_deploy_template_version`;
CREATE TABLE `ops_deploy_template_version` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `template_id` bigint(20) NOT NULL COMMENT '模板ID',
  `version` varchar(20) NOT NULL COMMENT '版本号',
  `script_content` text NOT NULL COMMENT '脚本内容',
  `sha256` varchar(64) DEFAULT '' COMMENT '脚本SHA256',
  `change_log` varchar(500) DEFAULT '' COMMENT '变更日志',
  `creator` varchar(64) DEFAULT '' COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_template_version` (`template_id`, `version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='部署模板版本历史表';

-- ----------------------------
-- 3. 部署记录表 (新增)
-- ----------------------------
DROP TABLE IF EXISTS `ops_deploy_record`;
CREATE TABLE `ops_deploy_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `app_id` bigint(20) NOT NULL COMMENT '应用ID',
  `template_version_id` bigint(20) DEFAULT NULL COMMENT '使用的模板版本ID',
  `oper_log_id` bigint(20) DEFAULT NULL COMMENT '关联的操作日志ID',
  `status` char(1) DEFAULT '0' COMMENT '状态(0成功 1失败)',
  `error_msg` text COMMENT '异常信息',
  `start_time` datetime DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime DEFAULT NULL COMMENT '结束时间',
  `duration` bigint(20) DEFAULT 0 COMMENT '耗时(ms)',
  `json_result` text COMMENT '执行结果JSON',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建人',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_app_id` (`app_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='应用部署记录表';

-- ----------------------------
-- 4. 初始化默认模板数据
-- ----------------------------
INSERT INTO `ops_deploy_template` (`template_name`, `app_type`, `script_content`, `version`, `built_in`, `status`, `sha256`, `creator`, `create_time`, `remark`) VALUES 
('Java标准部署模板', 'Java', 'steps:\n  - name: pull_code\n    command: git pull\n  - name: stop_app\n    command: ./stop.sh\n  - name: backup\n    command: cp -r app app_backup_${timestamp}\n  - name: replace_jar\n    command: cp target/*.jar app/\n  - name: start_app\n    command: ./start.sh\n  - name: health_check\n    url: ${health_check_url}\n    timeout: 30\n    retry: 3', 'v1.0.0', '1', '0', 'e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855', 'admin', NOW(), '系统内置Java应用标准部署流程');

INSERT INTO `ops_deploy_template_version` (`template_id`, `version`, `script_content`, `sha256`, `change_log`, `creator`, `create_time`) 
SELECT id, version, script_content, sha256, '初始化创建', creator, create_time FROM `ops_deploy_template` WHERE `template_name` = 'Java标准部署模板';
