-- ----------------------------
-- JMX 监控目标表
-- ----------------------------
DROP TABLE IF EXISTS `sys_jvm_target`;
CREATE TABLE `sys_jvm_target` (
  `target_id`     bigint(20)      NOT NULL AUTO_INCREMENT COMMENT '目标ID',
  `name`          varchar(50)     NOT NULL COMMENT '应用名称',
  `host`          varchar(100)    NOT NULL COMMENT '主机地址',
  `port`          int(11)         NOT NULL COMMENT 'JMX端口',
  `username`      varchar(50)     DEFAULT NULL COMMENT 'JMX用户名',
  `password`      varchar(100)    DEFAULT NULL COMMENT 'JMX密码',
  `enabled`       char(1)         DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `create_by`     varchar(64)     DEFAULT '' COMMENT '创建者',
  `create_time`   datetime        DEFAULT NULL COMMENT '创建时间',
  `update_by`     varchar(64)     DEFAULT '' COMMENT '更新者',
  `update_time`   datetime        DEFAULT NULL COMMENT '更新时间',
  `remark`        varchar(500)    DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`target_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='JMX监控目标表';

-- ----------------------------
-- JVM 监控指标历史表
-- ----------------------------
DROP TABLE IF EXISTS `sys_jvm_metric`;
CREATE TABLE `sys_jvm_metric` (
  `metric_id`       bigint(20)      NOT NULL AUTO_INCREMENT COMMENT '指标ID',
  `target_id`       bigint(20)      NOT NULL COMMENT '目标ID',
  `heap_used`       bigint(20)      DEFAULT 0 COMMENT '堆内存使用量(bytes)',
  `heap_max`        bigint(20)      DEFAULT 0 COMMENT '堆内存最大值(bytes)',
  `non_heap_used`   bigint(20)      DEFAULT 0 COMMENT '非堆内存使用量(bytes)',
  `non_heap_max`    bigint(20)      DEFAULT 0 COMMENT '非堆内存最大值(bytes)',
  `thread_active`   int(11)         DEFAULT 0 COMMENT '当前活跃线程数',
  `thread_peak`     int(11)         DEFAULT 0 COMMENT '峰值线程数',
  `gc_count`        bigint(20)      DEFAULT 0 COMMENT 'GC总次数',
  `gc_time`         bigint(20)      DEFAULT 0 COMMENT 'GC总时间(ms)',
  `create_time`     datetime        DEFAULT NULL COMMENT '采集时间',
  PRIMARY KEY (`metric_id`),
  KEY `idx_sys_jvm_metric_target` (`target_id`),
  KEY `idx_sys_jvm_metric_time` (`create_time`)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='JVM监控指标历史表';

-- ----------------------------
-- JVM 告警规则表
-- ----------------------------
DROP TABLE IF EXISTS `sys_jvm_alert_rule`;
CREATE TABLE `sys_jvm_alert_rule` (
  `rule_id`       bigint(20)      NOT NULL AUTO_INCREMENT COMMENT '规则ID',
  `target_id`     bigint(20)      DEFAULT 0 COMMENT '目标ID(0代表全局)',
  `metric_name`   varchar(50)     NOT NULL COMMENT '指标名称(heap_usage, thread_count)',
  `condition`     varchar(10)     NOT NULL COMMENT '条件(GT, LT)',
  `threshold`     double          NOT NULL COMMENT '阈值',
  `enabled`       char(1)         DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `create_by`     varchar(64)     DEFAULT '' COMMENT '创建者',
  `create_time`   datetime        DEFAULT NULL COMMENT '创建时间',
  `update_by`     varchar(64)     DEFAULT '' COMMENT '更新者',
  `update_time`   datetime        DEFAULT NULL COMMENT '更新时间',
  `remark`        varchar(500)    DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`rule_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 COMMENT='JVM告警规则表';

-- ----------------------------
-- 插入本地JMX监控配置
-- ----------------------------
INSERT INTO `sys_jvm_target` VALUES (1, 'Local Application', '127.0.0.1', 1099, NULL, NULL, '0', 'admin', SYSDATE(), '', NULL, '本地应用监控');

-- ----------------------------
-- 插入菜单
-- ----------------------------
-- 菜单 SQL
insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values(118, 'JVM 监控', 2050, 7, 'jvm', 'monitor/jvm/index', 1, 0, 'C', '0', '0', 'monitor:jvm:list', 'server', 'admin', sysdate(), '', null, 'JVM监控菜单');

-- 按钮 SQL
insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values(1070, 'JVM监控查询', 118, 1, '#', '', 1, 0, 'F', '0', '0', 'monitor:jvm:query', '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values(1061, 'JVM监控新增', 118, 2, '#', '', 1, 0, 'F', '0', '0', 'monitor:jvm:add', '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values(1062, 'JVM监控修改', 118, 3, '#', '', 1, 0, 'F', '0', '0', 'monitor:jvm:edit', '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values(1063, 'JVM监控删除', 118, 4, '#', '', 1, 0, 'F', '0', '0', 'monitor:jvm:remove', '#', 'admin', sysdate(), '', null, '');
