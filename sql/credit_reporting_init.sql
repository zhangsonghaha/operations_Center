-- ----------------------------
-- 1. Create Tables
-- ----------------------------

-- Enterprise Credit Main Table
DROP TABLE IF EXISTS `enterprise_credit`;
CREATE TABLE `enterprise_credit` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `company_name` varchar(200) NOT NULL COMMENT '企业名称',
  `credit_code` varchar(50) NOT NULL COMMENT '统一社会信用代码',
  `register_code` varchar(50) DEFAULT NULL COMMENT '注册号',
  `register_capital` decimal(15,2) DEFAULT NULL COMMENT '注册资本',
  `establish_date` date DEFAULT NULL COMMENT '成立日期',
  `company_type` varchar(100) DEFAULT NULL COMMENT '企业类型',
  `industry` varchar(100) DEFAULT NULL COMMENT '行业分类',
  `business_scope` text COMMENT '经营范围',
  `employee_count` int(11) DEFAULT NULL COMMENT '员工人数',
  `total_assets` decimal(15,2) DEFAULT NULL COMMENT '资产总额',
  `legal_representative` varchar(100) DEFAULT NULL COMMENT '法定代表人',
  `controller_name` varchar(100) DEFAULT NULL COMMENT '实际控制人姓名',
  `parent_company` varchar(200) DEFAULT NULL COMMENT '上级机构名称',
  `register_address` varchar(500) DEFAULT NULL COMMENT '注册地址',
  `office_address` varchar(500) DEFAULT NULL COMMENT '办公地址',
  `contact_phone` varchar(50) DEFAULT NULL COMMENT '联系电话',
  `contact_email` varchar(100) DEFAULT NULL COMMENT '联系邮箱',
  `status` varchar(20) DEFAULT '0' COMMENT '报送状态(0待录入 1待审核 2已审核 3已报送 4退回修改)',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_credit_code` (`credit_code`),
  KEY `idx_company_name` (`company_name`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='企业征信信息主表';

-- Other ID Table
DROP TABLE IF EXISTS `credit_other_id`;
CREATE TABLE `credit_other_id` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `credit_id` bigint(20) NOT NULL COMMENT '征信ID',
  `id_type` varchar(50) NOT NULL COMMENT '标识类型',
  `id_code` varchar(100) NOT NULL COMMENT '标识代码',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_credit_id` (`credit_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='企业其他标识信息表';

-- Personnel Table
DROP TABLE IF EXISTS `credit_personnel`;
CREATE TABLE `credit_personnel` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `credit_id` bigint(20) NOT NULL COMMENT '征信ID',
  `personnel_type` varchar(50) NOT NULL COMMENT '人员类型',
  `name` varchar(100) NOT NULL COMMENT '姓名',
  `id_type` varchar(50) DEFAULT NULL COMMENT '证件类型',
  `id_number` varchar(50) DEFAULT NULL COMMENT '证件号码',
  `position` varchar(100) DEFAULT NULL COMMENT '职位',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_credit_id` (`credit_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='企业人员信息表';

-- Shareholder Table
DROP TABLE IF EXISTS `credit_shareholder`;
CREATE TABLE `credit_shareholder` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `credit_id` bigint(20) NOT NULL COMMENT '征信ID',
  `shareholder_name` varchar(200) NOT NULL COMMENT '股东名称',
  `contribution_ratio` decimal(5,2) DEFAULT NULL COMMENT '出资比例',
  `contribution_method` varchar(50) DEFAULT NULL COMMENT '出资方式',
  `contribution_date` date DEFAULT NULL COMMENT '出资时间',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_credit_id` (`credit_id`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8mb4 COMMENT='企业股东信息表';

-- ----------------------------
-- 2. Initialize Dict Data
-- ----------------------------
INSERT INTO sys_dict_type (dict_name, dict_type, status, create_by, create_time, update_by, update_time, remark) 
VALUES ('征信报送状态', 'credit_reporting_status', '0', 'admin', NOW(), 'admin', NOW(), '征信报送状态字典');

INSERT INTO sys_dict_data (dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, status, create_by, create_time, update_by, update_time, remark) VALUES
(1, '待录入', '0', 'credit_reporting_status', '', 'default', 'Y', '0', 'admin', NOW(), 'admin', NOW(), '待录入状态'),
(2, '待审核', '1', 'credit_reporting_status', '', 'warning', 'N', '0', 'admin', NOW(), 'admin', NOW(), '待审核状态'),
(3, '已审核', '2', 'credit_reporting_status', '', 'success', 'N', '0', 'admin', NOW(), 'admin', NOW(), '已审核状态'),
(4, '已报送', '3', 'credit_reporting_status', '', 'primary', 'N', '0', 'admin', NOW(), 'admin', NOW(), '已报送状态'),
(5, '退回修改', '4', 'credit_reporting_status', '', 'danger', 'N', '0', 'admin', NOW(), 'admin', NOW(), '退回修改状态');

-- ----------------------------
-- 3. Initialize Menu Data
-- ----------------------------
-- Top Menu
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES('征信报送', 0, 10, 'credit', NULL, 1, 0, 'M', '0', '0', '', 'documentation', 'admin', sysdate(), '', NULL, '征信报送根目录');

-- Sub Menu
SELECT @parentId := menu_id FROM sys_menu WHERE menu_name = '征信报送' AND parent_id = 0;

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES('企业征信管理', @parentId, 1, 'reporting', 'credit/reporting/index', 1, 0, 'C', '0', '0', 'credit:reporting:list', 'form', 'admin', sysdate(), '', NULL, '企业征信列表菜单');

-- Buttons
SELECT @menuId := menu_id FROM sys_menu WHERE menu_name = '企业征信管理' AND parent_id = @parentId;

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark) VALUES
('查询', @menuId, 1, '', '', 1, 0, 'F', '0', '0', 'credit:reporting:query', '#', 'admin', sysdate(), '', NULL, ''),
('新增', @menuId, 2, '', '', 1, 0, 'F', '0', '0', 'credit:reporting:add', '#', 'admin', sysdate(), '', NULL, ''),
('修改', @menuId, 3, '', '', 1, 0, 'F', '0', '0', 'credit:reporting:edit', '#', 'admin', sysdate(), '', NULL, ''),
('删除', @menuId, 4, '', '', 1, 0, 'F', '0', '0', 'credit:reporting:remove', '#', 'admin', sysdate(), '', NULL, ''),
('导出', @menuId, 5, '', '', 1, 0, 'F', '0', '0', 'credit:reporting:export', '#', 'admin', sysdate(), '', NULL, '');
