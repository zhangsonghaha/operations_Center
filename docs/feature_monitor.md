# 服务器信息监控模块开发文档

## 1. 简介
本模块提供对已添加的服务器进行实时状态监控（CPU、内存、磁盘、网络）以及历史趋势分析的功能。通过 SSH 连接远程采集数据，无需在目标服务器安装 Agent。

## 2. 数据库设计

### 2.1 监控日志表 `sys_ops_monitor_log`
用于存储定时采集的服务器性能快照数据。

```sql
CREATE TABLE `sys_ops_monitor_log` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `server_id` bigint(20) NOT NULL COMMENT '服务器ID',
  `cpu_usage` decimal(5,2) DEFAULT '0.00' COMMENT 'CPU使用率(%)',
  `memory_usage` decimal(5,2) DEFAULT '0.00' COMMENT '内存使用率(%)',
  `disk_usage` decimal(5,2) DEFAULT '0.00' COMMENT '磁盘使用率(%)',
  `net_tx_rate` decimal(10,2) DEFAULT '0.00' COMMENT '网络发送速率(KB/s)',
  `net_rx_rate` decimal(10,2) DEFAULT '0.00' COMMENT '网络接收速率(KB/s)',
  `create_time` datetime DEFAULT NULL COMMENT '采集时间',
  PRIMARY KEY (`id`),
  KEY `idx_server_time` (`server_id`,`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='服务器监控日志表';
```

## 3. 后端实现 (Java)

### 3.1 核心类
*   **Entity**: `com.ruoyi.web.domain.OpsMonitorLog`
*   **Mapper**: `com.ruoyi.web.mapper.OpsMonitorLogMapper`
*   **Service**: `com.ruoyi.web.service.OpsMonitorService`
*   **Controller**: `com.ruoyi.web.controller.system.OpsMonitorController`
*   **Task**: `com.ruoyi.web.task.OpsMonitorTask`

### 3.2 数据采集逻辑 (`OpsMonitorService`)
使用 JSch 库通过 SSH 连接服务器执行 Linux 命令：
*   **CPU**: `top -bn1 | grep "Cpu(s)"` -> 解析 idle 值计算 usage。
*   **Memory**: `free -m | grep Mem` -> 解析 used/total。
*   **Disk**: `df -h / | tail -1` -> 解析根分区使用率。
*   **Network**: 暂未完全实现速率计算（预留字段）。

### 3.3 定时任务 (`OpsMonitorTask`)
*   配置为每 **5分钟** 执行一次 (`0 0/5 * * * ?`)。
*   遍历所有状态为“正常”的服务器，采集数据并写入数据库。
*   自动清理 7 天前的历史数据。

### 3.4 API 接口
*   `GET /ops/monitor/realtime/{serverId}`: 获取当前瞬时状态（不查库，直接 SSH）。
*   `GET /ops/monitor/trend/{serverId}?range=24h`: 获取历史趋势数据（查库）。

## 4. 前端实现 (Vue)

### 4.1 页面
*   路径: `src/views/ops/monitor/index.vue`
*   布局: 左侧服务器列表 + 右侧监控面板。
*   组件:
    *   **ECharts Gauge**: 用于展示 CPU、内存、磁盘的实时仪表盘。
    *   **ECharts Line**: 用于展示历史趋势折线图。

### 4.2 路由与菜单
*   路由路径: `/ops/monitor`
*   菜单 SQL: 详见 `sql/monitor_module_menu.sql`

## 5. 部署说明
1.  执行 `sql/monitor_module_menu.sql` 初始化菜单。
2.  确保目标服务器 SSH 端口开放且账号密码/密钥正确。
3.  后端需引入 `jsch` 依赖（RuoYi-Vue 默认已包含或需确认）。
