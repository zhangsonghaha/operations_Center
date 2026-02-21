-- 菜单 SQL
insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values(2065, '发布管理', 2100, 5, 'release', 'ops/release/index', 1, 0, 'C', '0', '0', 'ops:release:list', 'guide', 'admin', sysdate(), '', null, '发布申请管理菜单');

insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values(2073, '待审批', 2100, 6, 'releasePending', 'ops/release/pending', 1, 0, 'C', '0', '0', 'ops:release:pending', 'guide', 'admin', sysdate(), '', null, '发布待审批菜单');

-- 按钮 SQL
insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values(2066, '发布查询', 2065, 1, '#', '', 1, 0, 'F', '0', '0', 'ops:release:query', '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values(2067, '发布新增', 2065, 2, '#', '', 1, 0, 'F', '0', '0', 'ops:release:add', '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values(2068, '发布修改', 2065, 3, '#', '', 1, 0, 'F', '0', '0', 'ops:release:edit', '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values(2069, '发布删除', 2065, 4, '#', '', 1, 0, 'F', '0', '0', 'ops:release:remove', '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values(2070, '发布审批', 2065, 5, '#', '', 1, 0, 'F', '0', '0', 'ops:release:audit', '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values(2071, '发布执行', 2065, 6, '#', '', 1, 0, 'F', '0', '0', 'ops:release:deploy', '#', 'admin', sysdate(), '', null, '');

insert into sys_menu (menu_id, menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
values(2072, '发布导出', 2065, 7, '#', '', 1, 0, 'F', '0', '0', 'ops:release:export', '#', 'admin', sysdate(), '', null, '');
