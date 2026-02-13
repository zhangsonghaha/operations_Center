import pymysql

def init_server_db():
    try:
        conn = pymysql.connect(
            host='localhost',
            user='root',
            password='zs123',
            database='ry-vue',
            charset='utf8mb4'
        )
        print("Connected to database.")
        cursor = conn.cursor()

        # Server Asset Table
        cursor.execute("""
            CREATE TABLE IF NOT EXISTS `t_ops_server` (
              `server_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '服务器ID',
              `server_name` varchar(64) DEFAULT '' COMMENT '服务器名称',
              `public_ip` varchar(20) DEFAULT '' COMMENT '公网IP',
              `private_ip` varchar(20) DEFAULT '' COMMENT '内网IP',
              `server_port` int(5) DEFAULT 22 COMMENT 'SSH端口',
              `username` varchar(64) DEFAULT 'root' COMMENT '账号',
              `password` varchar(128) DEFAULT '' COMMENT '密码',
              `auth_type` char(1) DEFAULT '0' COMMENT '认证方式（0密码 1密钥）',
              `private_key` text COMMENT 'SSH密钥',
              `data_center` varchar(64) DEFAULT '' COMMENT '所属机房',
              `group_id` bigint(20) DEFAULT NULL COMMENT '所属分组',
              `status` char(1) DEFAULT '0' COMMENT '状态（0正常 1停用）',
              `remark` varchar(500) DEFAULT '' COMMENT '备注',
              `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
              `create_time` datetime DEFAULT NULL COMMENT '创建时间',
              `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
              `update_time` datetime DEFAULT NULL COMMENT '更新时间',
              `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
              PRIMARY KEY (`server_id`)
            ) ENGINE=InnoDB AUTO_INCREMENT=100 COMMENT='服务器资产表';
        """)
        print("Created t_ops_server table.")

        # Server Group Table (Optional for better grouping)
        cursor.execute("""
            CREATE TABLE IF NOT EXISTS `t_ops_server_group` (
              `group_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '分组ID',
              `group_name` varchar(64) DEFAULT '' COMMENT '分组名称',
              `order_num` int(4) DEFAULT 0 COMMENT '显示顺序',
              `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
              `create_time` datetime DEFAULT NULL COMMENT '创建时间',
              `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
              `update_time` datetime DEFAULT NULL COMMENT '更新时间',
              `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
              PRIMARY KEY (`group_id`)
            ) ENGINE=InnoDB AUTO_INCREMENT=100 COMMENT='服务器分组表';
        """)
        print("Created t_ops_server_group table.")

        # Insert Mock Data
        cursor.execute("TRUNCATE TABLE t_ops_server")
        cursor.execute("""
            INSERT INTO t_ops_server (server_name, public_ip, private_ip, data_center, status, create_time, remark) 
            VALUES 
            ('Web-Server-01', '120.55.12.33', '192.168.1.101', '阿里云-杭州', '0', NOW(), '核心Web服务'), 
            ('DB-Master', '120.55.12.34', '192.168.1.102', '阿里云-杭州', '0', NOW(), 'MySQL主库'), 
            ('Redis-Cache', '120.55.12.35', '192.168.1.103', '阿里云-上海', '0', NOW(), '缓存集群')
        """)
        
        cursor.execute("TRUNCATE TABLE t_ops_server_group")
        cursor.execute("INSERT INTO t_ops_server_group (group_name, order_num, create_time) VALUES ('Web Servers', 1, NOW()), ('Database Servers', 2, NOW())")

        print("Inserted mock data.")

        # Add Menu
        cursor.execute("DELETE FROM sys_menu WHERE menu_name = '服务器管理'")
        cursor.execute("""
            INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, update_by, update_time, remark)
            VALUES('服务器管理', 0, 1, 'server', 'ops/server/index', 1, 0, 'C', '0', '0', 'ops:server:list', 'server', 'admin', sysdate(), '', null, '服务器管理菜单');
        """)
        print("Added menu entry.")

        conn.commit()
        cursor.close()
        conn.close()
        print("Database initialization completed.")

    except Exception as e:
        print(f"Database error: {e}")

if __name__ == "__main__":
    init_server_db()
