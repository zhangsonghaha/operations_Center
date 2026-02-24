-- 站内信表
CREATE TABLE `sys_message` (
  `message_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `sender_id` bigint(20) DEFAULT NULL COMMENT '发送者ID',
  `receiver_id` bigint(20) DEFAULT NULL COMMENT '接收者ID',
  `title` varchar(255) DEFAULT '' COMMENT '标题',
  `content` longtext COMMENT '内容',
  `message_type` char(1) DEFAULT '1' COMMENT '消息类型（1通知 2待办 3催办 4完结）',
  `read_status` char(1) DEFAULT '0' COMMENT '阅读状态（0未读 1已读）',
  `delete_status` char(1) DEFAULT '0' COMMENT '删除状态（0正常 1回收站 2彻底删除）',
  `attachment` varchar(500) DEFAULT '' COMMENT '附件路径',
  `business_id` varchar(64) DEFAULT '' COMMENT '关联业务ID',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`message_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='站内信表';

-- 菜单SQL（可选，需要手动插入sys_menu表）
-- INSERT INTO sys_menu ...
