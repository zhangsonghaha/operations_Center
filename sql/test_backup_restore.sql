-- ============================================
-- 备份恢复功能测试数据
-- ============================================

-- 创建测试用户表
DROP TABLE IF EXISTS `test_user`;
CREATE TABLE `test_user` (
  `user_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `nickname` VARCHAR(50) DEFAULT NULL COMMENT '昵称',
  `email` VARCHAR(100) DEFAULT NULL COMMENT '邮箱',
  `phone` VARCHAR(20) DEFAULT NULL COMMENT '手机号',
  `gender` CHAR(1) DEFAULT '0' COMMENT '性别(0男/1女/2未知)',
  `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像',
  `status` CHAR(1) DEFAULT '0' COMMENT '状态(0正常/1禁用)',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` DATETIME DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='测试用户表';

-- 插入测试用户数据
INSERT INTO `test_user` (`username`, `nickname`, `email`, `phone`, `gender`, `status`) VALUES
('admin', '系统管理员', 'admin@example.com', '13800138000', '0', '0'),
('test001', '测试用户1', 'test001@example.com', '13800138001', '0', '0'),
('test002', '测试用户2', 'test002@example.com', '13800138002', '1', '0'),
('test003', '测试用户3', 'test003@example.com', '13800138003', '0', '1'),
('test004', '测试用户4', 'test004@example.com', '13800138004', '1', '0'),
('test005', '测试用户5', 'test005@example.com', '13800138005', '0', '0');

-- 创建测试订单表
DROP TABLE IF EXISTS `test_order`;
CREATE TABLE `test_order` (
  `order_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `order_no` VARCHAR(50) NOT NULL COMMENT '订单编号',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `product_name` VARCHAR(100) NOT NULL COMMENT '商品名称',
  `product_count` INT NOT NULL DEFAULT 1 COMMENT '商品数量',
  `order_amount` DECIMAL(10,2) NOT NULL COMMENT '订单金额',
  `order_status` CHAR(1) DEFAULT '0' COMMENT '订单状态(0待支付/1已支付/2已完成/3已取消)',
  `pay_time` DATETIME DEFAULT NULL COMMENT '支付时间',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`order_id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`order_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='测试订单表';

-- 插入测试订单数据
INSERT INTO `test_order` (`order_no`, `user_id`, `product_name`, `product_count`, `order_amount`, `order_status`, `pay_time`) VALUES
('ORD20260225001', 1, 'iPhone 15 Pro', 1, 8999.00, '1', NOW()),
('ORD20260225002', 2, 'MacBook Pro', 1, 12999.00, '1', NOW()),
('ORD20260225003', 3, 'iPad Air', 2, 5998.00, '0', NULL),
('ORD20260225004', 4, 'AirPods Pro', 1, 1999.00, '1', NOW()),
('ORD20260225005', 5, 'Apple Watch', 1, 3299.00, '2', NOW()),
('ORD20260225006', 1, '数据线', 3, 147.00, '1', NOW()),
('ORD20260225007', 2, '手机壳', 2, 98.00, '3', NULL);

-- 创建测试日志表
DROP TABLE IF EXISTS `test_oper_log`;
CREATE TABLE `test_oper_log` (
  `log_id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `title` VARCHAR(100) DEFAULT NULL COMMENT '操作模块',
  `business_type` INT DEFAULT 0 COMMENT '业务类型',
  `method` VARCHAR(100) DEFAULT NULL COMMENT '方法名',
  `request_method` VARCHAR(10) DEFAULT NULL COMMENT '请求方式',
  `operator_type` INT DEFAULT 0 COMMENT '操作类别',
  `oper_name` VARCHAR(50) DEFAULT NULL COMMENT '操作人',
  `oper_url` VARCHAR(255) DEFAULT NULL COMMENT '请求URL',
  `oper_ip` VARCHAR(50) DEFAULT NULL COMMENT '主机地址',
  `oper_location` VARCHAR(255) DEFAULT NULL COMMENT '操作地点',
  `oper_param` VARCHAR(2000) DEFAULT NULL COMMENT '请求参数',
  `json_result` VARCHAR(2000) DEFAULT NULL COMMENT '返回结果',
  `status` CHAR(1) DEFAULT '0' COMMENT '操作状态(0正常/1异常)',
  `error_msg` VARCHAR(2000) DEFAULT NULL COMMENT '错误消息',
  `oper_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`log_id`),
  KEY `idx_oper_time` (`oper_time`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='测试操作日志表';

-- 插入测试日志数据
INSERT INTO `test_oper_log` (`title`, `business_type`, `method`, `request_method`, `operator_type`, `oper_name`, `oper_url`, `oper_ip`, `oper_location`, `status`) VALUES
('用户登录', 1, 'login', 'POST', 0, 'admin', '/login', '127.0.0.1', '内网IP', '0'),
('用户登出', 2, 'logout', 'POST', 0, 'admin', '/logout', '127.0.0.1', '内网IP', '0'),
('查询用户', 3, 'list', 'GET', 0, 'admin', '/system/user/list', '127.0.0.1', '内网IP', '0'),
('新增用户', 1, 'add', 'POST', 0, 'admin', '/system/user', '127.0.0.1', '内网IP', '0'),
('修改用户', 2, 'update', 'PUT', 0, 'admin', '/system/user', '127.0.0.1', '内网IP', '0'),
('删除用户', 3, 'delete', 'DELETE', 0, 'admin', '/system/user/1', '127.0.0.1', '内网IP', '0'),
('导出数据', 6, 'export', 'POST', 0, 'admin', '/system/user/export', '127.0.0.1', '内网IP', '0'),
('导入数据', 6, 'import', 'POST', 0, 'admin', '/system/user/import', '127.0.0.1', '内网IP', '0');

-- 查看测试数据统计
SELECT 'test_user' AS table_name, COUNT(*) AS row_count FROM test_user
UNION ALL
SELECT 'test_order', COUNT(*) FROM test_order
UNION ALL
SELECT 'test_oper_log', COUNT(*) FROM test_oper_log;
