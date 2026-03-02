-- ============================================
-- Docker 一键部署功能 - 数据库初始化脚本
-- ============================================

-- ----------------------------
-- 1. Docker 容器表
-- ----------------------------
DROP TABLE IF EXISTS `t_docker_container`;
CREATE TABLE `t_docker_container` (
  `container_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '容器记录ID',
  `container_docker_id` varchar(64) DEFAULT NULL COMMENT 'Docker容器ID',
  `container_name` varchar(100) NOT NULL COMMENT '容器名称',
  `image_name` varchar(200) NOT NULL COMMENT '镜像名称',
  `image_tag` varchar(50) DEFAULT 'latest' COMMENT '镜像标签',
  `server_id` bigint(20) NOT NULL COMMENT '服务器ID',
  `server_name` varchar(100) DEFAULT NULL COMMENT '服务器名称',
  `port_mappings` text COMMENT '端口映射(JSON格式)',
  `env_vars` text COMMENT '环境变量(JSON格式)',
  `volume_mounts` text COMMENT '卷挂载(JSON格式)',
  `cpu_limit` decimal(4,2) DEFAULT NULL COMMENT 'CPU限制(核数)',
  `memory_limit` varchar(20) DEFAULT NULL COMMENT '内存限制(如: 2g, 512m)',
  `restart_policy` varchar(50) DEFAULT 'unless-stopped' COMMENT '重启策略',
  `network_mode` varchar(50) DEFAULT 'bridge' COMMENT '网络模式',
  `container_status` varchar(20) DEFAULT 'unknown' COMMENT '容器状态(running-运行中, stopped-已停止, exited-已退出, unknown-未知)',
  `deploy_log_id` bigint(20) DEFAULT NULL COMMENT '关联的部署日志ID',
  `health_check_url` varchar(500) DEFAULT NULL COMMENT '健康检查URL',
  `health_status` varchar(20) DEFAULT 'unknown' COMMENT '健康状态(healthy-健康, unhealthy-不健康, unknown-未知)',
  `last_health_check` datetime DEFAULT NULL COMMENT '最后健康检查时间',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`container_id`),
  UNIQUE KEY `uk_container_name_server` (`container_name`, `server_id`),
  KEY `idx_server_id` (`server_id`),
  KEY `idx_container_status` (`container_status`),
  KEY `idx_deploy_log_id` (`deploy_log_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='Docker容器管理表';

-- ----------------------------
-- 2. Docker 部署模板表
-- ----------------------------
DROP TABLE IF EXISTS `t_docker_template`;
CREATE TABLE `t_docker_template` (
  `template_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '模板ID',
  `template_name` varchar(100) NOT NULL COMMENT '模板名称',
  `template_type` varchar(50) NOT NULL COMMENT '模板类型(mysql, redis, nginx, mongodb, custom)',
  `image_name` varchar(200) NOT NULL COMMENT '镜像名称',
  `image_tag` varchar(50) DEFAULT 'latest' COMMENT '镜像标签',
  `default_ports` text COMMENT '默认端口映射(JSON格式)',
  `default_env_vars` text COMMENT '默认环境变量(JSON格式)',
  `default_volumes` text COMMENT '默认卷挂载(JSON格式)',
  `default_cpu_limit` decimal(4,2) DEFAULT 2.00 COMMENT '默认CPU限制',
  `default_memory_limit` varchar(20) DEFAULT '2g' COMMENT '默认内存限制',
  `description` varchar(500) DEFAULT NULL COMMENT '模板描述',
  `is_system` char(1) DEFAULT '0' COMMENT '是否系统模板(0-否, 1-是)',
  `sort_order` int(11) DEFAULT 0 COMMENT '排序',
  `status` char(1) DEFAULT '0' COMMENT '状态(0-正常, 1-停用)',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`template_id`),
  UNIQUE KEY `uk_template_name` (`template_name`),
  KEY `idx_template_type` (`template_type`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='Docker部署模板表';

-- ----------------------------
-- 3. Docker 镜像缓存表
-- ----------------------------
DROP TABLE IF EXISTS `t_docker_image`;
CREATE TABLE `t_docker_image` (
  `image_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '镜像记录ID',
  `image_name` varchar(200) NOT NULL COMMENT '镜像名称',
  `image_tag` varchar(50) NOT NULL COMMENT '镜像标签',
  `server_id` bigint(20) NOT NULL COMMENT '服务器ID',
  `image_docker_id` varchar(64) DEFAULT NULL COMMENT 'Docker镜像ID',
  `image_size` varchar(50) DEFAULT NULL COMMENT '镜像大小',
  `pull_time` datetime DEFAULT NULL COMMENT '拉取时间',
  `last_used_time` datetime DEFAULT NULL COMMENT '最后使用时间',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`image_id`),
  UNIQUE KEY `uk_image_server` (`image_name`, `image_tag`, `server_id`),
  KEY `idx_server_id` (`server_id`),
  KEY `idx_pull_time` (`pull_time`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='Docker镜像缓存表';

-- ----------------------------
-- 4. 修改部署日志表 - 添加 deploy_type 字段支持 Docker 部署
-- ----------------------------
-- 检查字段是否存在，如果不存在则添加
SET @col_exists = 0;
SELECT COUNT(*) INTO @col_exists 
FROM information_schema.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() 
  AND TABLE_NAME = 't_ops_deploy_log' 
  AND COLUMN_NAME = 'deploy_type';

SET @sql = IF(@col_exists = 0,
  'ALTER TABLE t_ops_deploy_log MODIFY COLUMN deploy_type varchar(20) DEFAULT NULL COMMENT ''部署类型(deploy-部署, start-启动, stop-停止, restart-重启, docker-Docker部署)''',
  'SELECT ''Column deploy_type already exists'' AS message');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- ----------------------------
-- 5. 插入预定义 Docker 模板数据
-- ----------------------------

-- MySQL 模板
INSERT INTO `t_docker_template` (`template_name`, `template_type`, `image_name`, `image_tag`, `default_ports`, `default_env_vars`, `default_volumes`, `default_cpu_limit`, `default_memory_limit`, `description`, `is_system`, `sort_order`, `status`, `create_by`, `create_time`)
VALUES ('MySQL 8.0', 'mysql', 'mysql', '8.0', 
'[{"hostPort":3306,"containerPort":3306,"protocol":"tcp"}]',
'{"MYSQL_ROOT_PASSWORD":"root123456","MYSQL_DATABASE":"mydb","MYSQL_USER":"dbuser","MYSQL_PASSWORD":"dbpass123"}',
'[{"hostPath":"/data/mysql/data","containerPath":"/var/lib/mysql","mode":"rw"},{"hostPath":"/data/mysql/conf","containerPath":"/etc/mysql/conf.d","mode":"ro"}]',
2.00, '2g', 'MySQL 8.0 数据库服务', '1', 1, '0', 'admin', sysdate());

-- Redis 模板
INSERT INTO `t_docker_template` (`template_name`, `template_type`, `image_name`, `image_tag`, `default_ports`, `default_env_vars`, `default_volumes`, `default_cpu_limit`, `default_memory_limit`, `description`, `is_system`, `sort_order`, `status`, `create_by`, `create_time`)
VALUES ('Redis 7', 'redis', 'redis', '7-alpine', 
'[{"hostPort":6379,"containerPort":6379,"protocol":"tcp"}]',
'{}',
'[{"hostPath":"/data/redis/data","containerPath":"/data","mode":"rw"}]',
1.00, '1g', 'Redis 7 缓存服务', '1', 2, '0', 'admin', sysdate());

-- Nginx 模板
INSERT INTO `t_docker_template` (`template_name`, `template_type`, `image_name`, `image_tag`, `default_ports`, `default_env_vars`, `default_volumes`, `default_cpu_limit`, `default_memory_limit`, `description`, `is_system`, `sort_order`, `status`, `create_by`, `create_time`)
VALUES ('Nginx', 'nginx', 'nginx', 'alpine', 
'[{"hostPort":80,"containerPort":80,"protocol":"tcp"},{"hostPort":443,"containerPort":443,"protocol":"tcp"}]',
'{}',
'[{"hostPath":"/data/nginx/html","containerPath":"/usr/share/nginx/html","mode":"ro"},{"hostPath":"/data/nginx/conf","containerPath":"/etc/nginx/conf.d","mode":"ro"}]',
1.00, '512m', 'Nginx Web 服务器', '1', 3, '0', 'admin', sysdate());

-- MongoDB 模板
INSERT INTO `t_docker_template` (`template_name`, `template_type`, `image_name`, `image_tag`, `default_ports`, `default_env_vars`, `default_volumes`, `default_cpu_limit`, `default_memory_limit`, `description`, `is_system`, `sort_order`, `status`, `create_by`, `create_time`)
VALUES ('MongoDB 7', 'mongodb', 'mongo', '7', 
'[{"hostPort":27017,"containerPort":27017,"protocol":"tcp"}]',
'{"MONGO_INITDB_ROOT_USERNAME":"admin","MONGO_INITDB_ROOT_PASSWORD":"admin123456"}',
'[{"hostPath":"/data/mongodb/data","containerPath":"/data/db","mode":"rw"}]',
2.00, '2g', 'MongoDB 7 文档数据库', '1', 4, '0', 'admin', sysdate());

-- ----------------------------
-- 6. 插入 Docker 管理菜单和权限
-- ----------------------------

-- 查找运维管理菜单的ID (假设运维管理菜单ID为2000，如果不存在则需要先创建)
SET @ops_menu_id = (SELECT menu_id FROM sys_menu WHERE menu_name = '运维管理' LIMIT 1);

-- 如果运维管理菜单不存在，则创建
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
SELECT '运维管理', 0, 5, 'ops', NULL, 1, 0, 'M', '0', '0', '', 'tool', 'admin', sysdate(), '运维管理目录'
WHERE NOT EXISTS (SELECT 1 FROM sys_menu WHERE menu_name = '运维管理');

-- 重新获取运维管理菜单ID
SET @ops_menu_id = (SELECT menu_id FROM sys_menu WHERE menu_name = '运维管理' LIMIT 1);

-- Docker 容器管理菜单
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('Docker容器', @ops_menu_id, 4, 'dockerContainer', 'ops/dockerContainer/index', 1, 0, 'C', '0', '0', 'ops:docker:list', 'server', 'admin', sysdate(), 'Docker容器管理菜单');

SET @docker_menu_id = LAST_INSERT_ID();

-- Docker 容器管理按钮权限
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES 
('Docker容器查询', @docker_menu_id, 1, '#', '', 1, 0, 'F', '0', '0', 'ops:docker:query', '#', 'admin', sysdate(), ''),
('Docker容器新增', @docker_menu_id, 2, '#', '', 1, 0, 'F', '0', '0', 'ops:docker:add', '#', 'admin', sysdate(), ''),
('Docker容器修改', @docker_menu_id, 3, '#', '', 1, 0, 'F', '0', '0', 'ops:docker:edit', '#', 'admin', sysdate(), ''),
('Docker容器删除', @docker_menu_id, 4, '#', '', 1, 0, 'F', '0', '0', 'ops:docker:remove', '#', 'admin', sysdate(), ''),
('Docker容器部署', @docker_menu_id, 5, '#', '', 1, 0, 'F', '0', '0', 'ops:docker:deploy', '#', 'admin', sysdate(), ''),
('Docker容器启动', @docker_menu_id, 6, '#', '', 1, 0, 'F', '0', '0', 'ops:docker:start', '#', 'admin', sysdate(), ''),
('Docker容器停止', @docker_menu_id, 7, '#', '', 1, 0, 'F', '0', '0', 'ops:docker:stop', '#', 'admin', sysdate(), ''),
('Docker容器重启', @docker_menu_id, 8, '#', '', 1, 0, 'F', '0', '0', 'ops:docker:restart', '#', 'admin', sysdate(), ''),
('Docker环境检测', @docker_menu_id, 9, '#', '', 1, 0, 'F', '0', '0', 'ops:docker:checkEnv', '#', 'admin', sysdate(), '');

-- Docker 模板管理菜单
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES ('Docker模板', @ops_menu_id, 5, 'dockerTemplate', 'ops/dockerTemplate/index', 1, 0, 'C', '0', '0', 'ops:dockerTemplate:list', 'example', 'admin', sysdate(), 'Docker模板管理菜单');

SET @template_menu_id = LAST_INSERT_ID();

-- Docker 模板管理按钮权限
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
VALUES 
('Docker模板查询', @template_menu_id, 1, '#', '', 1, 0, 'F', '0', '0', 'ops:dockerTemplate:query', '#', 'admin', sysdate(), ''),
('Docker模板新增', @template_menu_id, 2, '#', '', 1, 0, 'F', '0', '0', 'ops:dockerTemplate:add', '#', 'admin', sysdate(), ''),
('Docker模板修改', @template_menu_id, 3, '#', '', 1, 0, 'F', '0', '0', 'ops:dockerTemplate:edit', '#', 'admin', sysdate(), ''),
('Docker模板删除', @template_menu_id, 4, '#', '', 1, 0, 'F', '0', '0', 'ops:dockerTemplate:remove', '#', 'admin', sysdate(), ''),
('Docker模板导出', @template_menu_id, 5, '#', '', 1, 0, 'F', '0', '0', 'ops:dockerTemplate:export', '#', 'admin', sysdate(), '');

-- ----------------------------
-- 7. 插入字典类型和字典数据
-- ----------------------------

-- Docker 容器状态字典
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
VALUES ('Docker容器状态', 'docker_container_status', '0', 'admin', sysdate(), 'Docker容器运行状态');

SET @dict_type_id = LAST_INSERT_ID();

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
VALUES 
(1, '运行中', 'running', 'docker_container_status', '', 'success', 'N', '0', 'admin', sysdate(), '容器正在运行'),
(2, '已停止', 'stopped', 'docker_container_status', '', 'info', 'N', '0', 'admin', sysdate(), '容器已停止'),
(3, '已退出', 'exited', 'docker_container_status', '', 'warning', 'N', '0', 'admin', sysdate(), '容器已退出'),
(4, '未知', 'unknown', 'docker_container_status', '', 'danger', 'N', '0', 'admin', sysdate(), '容器状态未知');

-- Docker 健康状态字典
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
VALUES ('Docker健康状态', 'docker_health_status', '0', 'admin', sysdate(), 'Docker容器健康检查状态');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
VALUES 
(1, '健康', 'healthy', 'docker_health_status', '', 'success', 'N', '0', 'admin', sysdate(), '容器健康'),
(2, '不健康', 'unhealthy', 'docker_health_status', '', 'danger', 'N', '0', 'admin', sysdate(), '容器不健康'),
(3, '未知', 'unknown', 'docker_health_status', '', 'info', 'N', '0', 'admin', sysdate(), '健康状态未知');

-- Docker 模板类型字典
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, remark)
VALUES ('Docker模板类型', 'docker_template_type', '0', 'admin', sysdate(), 'Docker部署模板类型');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, remark)
VALUES 
(1, 'MySQL', 'mysql', 'docker_template_type', '', 'primary', 'N', '0', 'admin', sysdate(), 'MySQL数据库'),
(2, 'Redis', 'redis', 'docker_template_type', '', 'success', 'N', '0', 'admin', sysdate(), 'Redis缓存'),
(3, 'Nginx', 'nginx', 'docker_template_type', '', 'info', 'N', '0', 'admin', sysdate(), 'Nginx服务器'),
(4, 'MongoDB', 'mongodb', 'docker_template_type', '', 'warning', 'N', '0', 'admin', sysdate(), 'MongoDB数据库'),
(5, '自定义', 'custom', 'docker_template_type', '', 'default', 'N', '0', 'admin', sysdate(), '自定义模板');

-- ----------------------------
-- 初始化完成
-- ----------------------------
SELECT 'Docker 一键部署功能数据库初始化完成！' AS message;
