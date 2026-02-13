import pymysql
import datetime

def init_ops_db():
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

        # 1. Host Table
        cursor.execute("""
            CREATE TABLE IF NOT EXISTS `t_ops_host` (
              `host_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主机ID',
              `host_name` varchar(64) DEFAULT '' COMMENT '主机名称',
              `ip_address` varchar(20) DEFAULT '' COMMENT 'IP地址',
              `status` char(1) DEFAULT '0' COMMENT '状态（0正常 1停用）',
              `cpu_usage` double DEFAULT 0 COMMENT 'CPU使用率',
              `memory_usage` double DEFAULT 0 COMMENT '内存使用率',
              `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
              `create_time` datetime DEFAULT NULL COMMENT '创建时间',
              `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
              `update_time` datetime DEFAULT NULL COMMENT '更新时间',
              `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
              PRIMARY KEY (`host_id`)
            ) ENGINE=InnoDB AUTO_INCREMENT=100 COMMENT='主机设备表';
        """)
        print("Created t_ops_host table.")

        # 2. Deployment Table
        cursor.execute("""
            CREATE TABLE IF NOT EXISTS `t_ops_deployment` (
              `deployment_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '部署ID',
              `app_name` varchar(64) DEFAULT '' COMMENT '应用名称',
              `version` varchar(32) DEFAULT '' COMMENT '版本号',
              `deploy_status` varchar(20) DEFAULT '' COMMENT '部署状态(success/failed)',
              `deploy_time` datetime DEFAULT NULL COMMENT '发布时间',
              `operator` varchar(64) DEFAULT '' COMMENT '操作人',
              `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
              `create_time` datetime DEFAULT NULL COMMENT '创建时间',
              `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
              `update_time` datetime DEFAULT NULL COMMENT '更新时间',
              `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
              PRIMARY KEY (`deployment_id`)
            ) ENGINE=InnoDB AUTO_INCREMENT=100 COMMENT='部署记录表';
        """)
        print("Created t_ops_deployment table.")

        # 3. Alert Table
        cursor.execute("""
            CREATE TABLE IF NOT EXISTS `t_ops_alert` (
              `alert_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '告警ID',
              `alert_content` varchar(500) DEFAULT '' COMMENT '告警内容',
              `alert_type` varchar(20) DEFAULT 'info' COMMENT '类型(danger/warning/info)',
              `status` char(1) DEFAULT '0' COMMENT '状态（0待处理 1已处理）',
              `alert_time` datetime DEFAULT NULL COMMENT '告警时间',
              `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
              `create_time` datetime DEFAULT NULL COMMENT '创建时间',
              `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
              `update_time` datetime DEFAULT NULL COMMENT '更新时间',
              `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
              PRIMARY KEY (`alert_id`)
            ) ENGINE=InnoDB AUTO_INCREMENT=100 COMMENT='运维告警表';
        """)
        print("Created t_ops_alert table.")

        # 4. Monitor Log Table
        cursor.execute("""
            CREATE TABLE IF NOT EXISTS `t_ops_monitor_log` (
              `log_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '日志ID',
              `cpu_load` double DEFAULT 0 COMMENT 'CPU负载',
              `memory_load` double DEFAULT 0 COMMENT '内存负载',
              `record_time` datetime DEFAULT NULL COMMENT '记录时间',
              `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
              `create_time` datetime DEFAULT NULL COMMENT '创建时间',
              `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
              PRIMARY KEY (`log_id`)
            ) ENGINE=InnoDB AUTO_INCREMENT=100 COMMENT='监控日志表';
        """)
        print("Created t_ops_monitor_log table.")

        # Insert Mock Data
        cursor.execute("TRUNCATE TABLE t_ops_host")
        cursor.execute("INSERT INTO t_ops_host (host_name, ip_address, status, cpu_usage, memory_usage, create_time) VALUES ('Server-01', '192.168.1.101', '0', 45.2, 60.5, NOW()), ('Server-02', '192.168.1.102', '0', 20.1, 30.2, NOW()), ('Server-03', '192.168.1.103', '1', 0, 0, NOW())")

        cursor.execute("TRUNCATE TABLE t_ops_deployment")
        cursor.execute("INSERT INTO t_ops_deployment (app_name, version, deploy_status, deploy_time, operator, create_time) VALUES ('payment-service', 'v2.1.0', 'success', NOW(), 'admin', NOW()), ('user-center', 'v1.5.2', 'failed', DATE_SUB(NOW(), INTERVAL 1 HOUR), 'devops', NOW()), ('gateway', 'v1.0.0', 'success', DATE_SUB(NOW(), INTERVAL 2 HOUR), 'admin', NOW())")
        
        cursor.execute("TRUNCATE TABLE t_ops_alert")
        cursor.execute("INSERT INTO t_ops_alert (alert_content, alert_type, status, alert_time, create_time) VALUES ('CPU load high on Server-01', 'danger', '0', NOW(), NOW()), ('Disk space low on Server-02', 'warning', '0', DATE_SUB(NOW(), INTERVAL 30 MINUTE), NOW()), ('System backup completed', 'info', '1', DATE_SUB(NOW(), INTERVAL 1 HOUR), NOW())")

        # Mock monitor logs for chart (last 7 data points)
        cursor.execute("TRUNCATE TABLE t_ops_monitor_log")
        for i in range(7):
            cursor.execute(f"INSERT INTO t_ops_monitor_log (cpu_load, memory_load, record_time, create_time) VALUES ({30 + i*5}, {40 + i*2}, DATE_SUB(NOW(), INTERVAL {6-i} HOUR), NOW())")

        print("Inserted mock data.")

        conn.commit()
        cursor.close()
        conn.close()
        print("Database initialization completed.")

    except Exception as e:
        print(f"Database error: {e}")

if __name__ == "__main__":
    init_ops_db()
