-- MySQL Database Backup
-- Generated: Wed Feb 25 11:28:01 CST 2026
-- Database: test
-- Charset: UTF-8

SET FOREIGN_KEY_CHECKS=0;

SET NAMES utf8mb4;

-- ----------------------------
-- Table structure for `test_oper_log`
-- ----------------------------
DROP TABLE IF EXISTS `test_oper_log`;
CREATE TABLE `test_oper_log` (
  `log_id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `title` varchar(100) DEFAULT NULL COMMENT '操作模块',
  `business_type` int DEFAULT '0' COMMENT '业务类型',
  `method` varchar(100) DEFAULT NULL COMMENT '方法名',
  `request_method` varchar(10) DEFAULT NULL COMMENT '请求方式',
  `operator_type` int DEFAULT '0' COMMENT '操作类别',
  `oper_name` varchar(50) DEFAULT NULL COMMENT '操作人',
  `oper_url` varchar(255) DEFAULT NULL COMMENT '请求URL',
  `oper_ip` varchar(50) DEFAULT NULL COMMENT '主机地址',
  `oper_location` varchar(255) DEFAULT NULL COMMENT '操作地点',
  `oper_param` varchar(2000) DEFAULT NULL COMMENT '请求参数',
  `json_result` varchar(2000) DEFAULT NULL COMMENT '返回结果',
  `status` char(1) DEFAULT '0' COMMENT '操作状态(0正常/1异常)',
  `error_msg` varchar(2000) DEFAULT NULL COMMENT '错误消息',
  `oper_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`log_id`),
  KEY `idx_oper_time` (`oper_time`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='测试操作日志表';

-- ----------------------------
-- Dumping data for table `test_oper_log`
-- ----------------------------
INSERT INTO `test_oper_log` VALUES (1, '用户登录', 1, 'login', 'POST', 0, 'admin', '/login', '127.0.0.1', '内网IP', NULL, NULL, '0', NULL, '2026-02-25T03:26:57');
INSERT INTO `test_oper_log` VALUES (2, '用户登出', 2, 'logout', 'POST', 0, 'admin', '/logout', '127.0.0.1', '内网IP', NULL, NULL, '0', NULL, '2026-02-25T03:26:57');
INSERT INTO `test_oper_log` VALUES (3, '查询用户', 3, 'list', 'GET', 0, 'admin', '/system/user/list', '127.0.0.1', '内网IP', NULL, NULL, '0', NULL, '2026-02-25T03:26:57');
INSERT INTO `test_oper_log` VALUES (4, '新增用户', 1, 'add', 'POST', 0, 'admin', '/system/user', '127.0.0.1', '内网IP', NULL, NULL, '0', NULL, '2026-02-25T03:26:57');
INSERT INTO `test_oper_log` VALUES (5, '修改用户', 2, 'update', 'PUT', 0, 'admin', '/system/user', '127.0.0.1', '内网IP', NULL, NULL, '0', NULL, '2026-02-25T03:26:57');
INSERT INTO `test_oper_log` VALUES (6, '删除用户', 3, 'delete', 'DELETE', 0, 'admin', '/system/user/1', '127.0.0.1', '内网IP', NULL, NULL, '0', NULL, '2026-02-25T03:26:57');
INSERT INTO `test_oper_log` VALUES (7, '导出数据', 6, 'export', 'POST', 0, 'admin', '/system/user/export', '127.0.0.1', '内网IP', NULL, NULL, '0', NULL, '2026-02-25T03:26:57');
INSERT INTO `test_oper_log` VALUES (8, '导入数据', 6, 'import', 'POST', 0, 'admin', '/system/user/import', '127.0.0.1', '内网IP', NULL, NULL, '0', NULL, '2026-02-25T03:26:57');

-- ----------------------------
-- Table structure for `test_order`
-- ----------------------------
DROP TABLE IF EXISTS `test_order`;
CREATE TABLE `test_order` (
  `order_id` bigint NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `order_no` varchar(50) NOT NULL COMMENT '订单编号',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `product_name` varchar(100) NOT NULL COMMENT '商品名称',
  `product_count` int NOT NULL DEFAULT '1' COMMENT '商品数量',
  `order_amount` decimal(10,2) NOT NULL COMMENT '订单金额',
  `order_status` char(1) DEFAULT '0' COMMENT '订单状态(0待支付/1已支付/2已完成/3已取消)',
  `pay_time` datetime DEFAULT NULL COMMENT '支付时间',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`order_id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`order_status`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='测试订单表';

-- ----------------------------
-- Dumping data for table `test_order`
-- ----------------------------
INSERT INTO `test_order` VALUES (1, 'ORD20260225001', 1, 'iPhone 15 Pro', 1, 8999.00, '1', '2026-02-25T03:26:57', '2026-02-25T03:26:57');
INSERT INTO `test_order` VALUES (2, 'ORD20260225002', 2, 'MacBook Pro', 1, 12999.00, '1', '2026-02-25T03:26:57', '2026-02-25T03:26:57');
INSERT INTO `test_order` VALUES (3, 'ORD20260225003', 3, 'iPad Air', 2, 5998.00, '0', NULL, '2026-02-25T03:26:57');
INSERT INTO `test_order` VALUES (4, 'ORD20260225004', 4, 'AirPods Pro', 1, 1999.00, '1', '2026-02-25T03:26:57', '2026-02-25T03:26:57');
INSERT INTO `test_order` VALUES (5, 'ORD20260225005', 5, 'Apple Watch', 1, 3299.00, '2', '2026-02-25T03:26:57', '2026-02-25T03:26:57');
INSERT INTO `test_order` VALUES (6, 'ORD20260225006', 1, '数据线', 3, 147.00, '1', '2026-02-25T03:26:57', '2026-02-25T03:26:57');
INSERT INTO `test_order` VALUES (7, 'ORD20260225007', 2, '手机壳', 2, 98.00, '3', NULL, '2026-02-25T03:26:57');

-- ----------------------------
-- Table structure for `test_user`
-- ----------------------------
DROP TABLE IF EXISTS `test_user`;
CREATE TABLE `test_user` (
  `user_id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `nickname` varchar(50) DEFAULT NULL COMMENT '昵称',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `gender` char(1) DEFAULT '0' COMMENT '性别(0男/1女/2未知)',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像',
  `status` char(1) DEFAULT '0' COMMENT '状态(0正常/1禁用)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='测试用户表';

-- ----------------------------
-- Dumping data for table `test_user`
-- ----------------------------
INSERT INTO `test_user` VALUES (1, 'admin', '系统管理员', 'admin@example.com', '13800138000', '0', NULL, '0', '2026-02-25T03:26:57', NULL);
INSERT INTO `test_user` VALUES (2, 'test001', '测试用户1', 'test001@example.com', '13800138001', '0', NULL, '0', '2026-02-25T03:26:57', NULL);
INSERT INTO `test_user` VALUES (3, 'test002', '测试用户2', 'test002@example.com', '13800138002', '1', NULL, '0', '2026-02-25T03:26:57', NULL);
INSERT INTO `test_user` VALUES (4, 'test003', '测试用户3', 'test003@example.com', '13800138003', '0', NULL, '1', '2026-02-25T03:26:57', NULL);
INSERT INTO `test_user` VALUES (5, 'test004', '测试用户4', 'test004@example.com', '13800138004', '1', NULL, '0', '2026-02-25T03:26:57', NULL);
INSERT INTO `test_user` VALUES (6, 'test005', '测试用户5', 'test005@example.com', '13800138005', '0', NULL, '0', '2026-02-25T03:26:57', NULL);

SET FOREIGN_KEY_CHECKS=1;
