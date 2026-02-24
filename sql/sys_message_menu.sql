
-- 站内信菜单
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES('站内信', 1, 10, 'message', 'system/message/index', 1, 0, 'C', '0', '0', 'system:message:list', 'message', 'admin', sysdate(), '', NULL, '站内信菜单');

-- 按钮权限
INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES('站内信查询', (SELECT menu_id FROM sys_menu WHERE menu_name = '站内信' AND parent_id = 1), 1, '', '', 1, 0, 'F', '0', '0', 'system:message:query', '#', 'admin', sysdate(), '', NULL, '');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES('站内信新增', (SELECT menu_id FROM sys_menu WHERE menu_name = '站内信' AND parent_id = 1), 2, '', '', 1, 0, 'F', '0', '0', 'system:message:add', '#', 'admin', sysdate(), '', NULL, '');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES('站内信修改', (SELECT menu_id FROM sys_menu WHERE menu_name = '站内信' AND parent_id = 1), 3, '', '', 1, 0, 'F', '0', '0', 'system:message:edit', '#', 'admin', sysdate(), '', NULL, '');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES('站内信删除', (SELECT menu_id FROM sys_menu WHERE menu_name = '站内信' AND parent_id = 1), 4, '', '', 1, 0, 'F', '0', '0', 'system:message:remove', '#', 'admin', sysdate(), '', NULL, '');

INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
VALUES('站内信导出', (SELECT menu_id FROM sys_menu WHERE menu_name = '站内信' AND parent_id = 1), 5, '', '', 1, 0, 'F', '0', '0', 'system:message:export', '#', 'admin', sysdate(), '', NULL, '');
