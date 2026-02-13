import pymysql

def fix_menu_structure():
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

        # 1. Clean up old menus
        cursor.execute("DELETE FROM sys_menu WHERE menu_name IN ('服务器管理', '运维管理')")
        print("Cleaned up old menus.")

        # 2. Create Parent Directory (Layout)
        # Parent ID 0, Type M, Component Layout, Path ops
        cursor.execute("""
            INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
            VALUES('运维管理', 0, 1, 'ops', 'Layout', 1, 0, 'M', '0', '0', '', 'monitor', 'admin', sysdate(), '运维管理目录');
        """)
        
        # Get the new Parent ID
        cursor.execute("SELECT menu_id FROM sys_menu WHERE menu_name = '运维管理' AND parent_id = 0")
        parent_id = cursor.fetchone()[0]
        print(f"Created Parent Menu '运维管理' with ID: {parent_id}")

        # 3. Create Child Menu (Server Page)
        # Parent ID {parent_id}, Type C, Component ops/server/index, Path server
        cursor.execute(f"""
            INSERT INTO sys_menu (menu_name, parent_id, order_num, path, component, is_frame, is_cache, menu_type, visible, status, perms, icon, create_by, create_time, remark)
            VALUES('服务器管理', {parent_id}, 1, 'server', 'ops/server/index', 1, 0, 'C', '0', '0', 'ops:server:list', 'server', 'admin', sysdate(), '服务器资产管理菜单');
        """)
        print("Created Child Menu '服务器管理'.")

        conn.commit()
        cursor.close()
        conn.close()
        print("Menu structure fixed successfully.")

    except Exception as e:
        print(f"Database error: {e}")

if __name__ == "__main__":
    fix_menu_structure()
