# Java 应用监控模块实施计划

## 1. 数据库设计
创建用于存储 JMX 目标配置、指标历史记录和告警规则的表。
- `sys_jvm_target`: 存储 JMX 连接详情（名称、主机、端口、认证信息）。
- `sys_jvm_metric`: 存储历史指标数据（堆内存、非堆内存、线程、GC）。
- `sys_jvm_alert_rule`: 存储告警阈值和配置。

## 2. 后端实现 (ruoyi-system)
### 实体层 (`com.ruoyi.system.domain.monitor`)
- `JvmTarget`: 目标应用配置实体。
- `JvmMetric`: 采集到的指标实体。
- `JvmAlertRule`: 告警规则实体。
- `JvmInfo`: 实时仪表盘数据的 VO 对象。

### Mapper 层 (`com.ruoyi.system.mapper.monitor`)
- `JvmTargetMapper`: 目标的 CRUD 操作。
- `JvmMetricMapper`: 历史数据的插入和查询。
- `JvmAlertRuleMapper`: 规则的 CRUD 操作。

### Service 层 (`com.ruoyi.system.service.monitor`)
- `IJvmMonitorService` / `JvmMonitorServiceImpl`:
  - `connect(JvmTarget target)`: 建立 JMX 连接。
  - `collectMetrics(JvmTarget target)`: 获取堆、非堆、线程、GC 信息。
  - `triggerGC(JvmTarget target)`: 调用 `System.gc()`。
  - `checkAlerts(JvmMetric metric)`: 根据规则评估指标。

### 定时任务 (`ruoyi-quartz`)
- `JvmMonitorTask`: Quartz 任务，遍历所有启用的目标并采集指标。

## 3. Controller 层 (ruoyi-admin)
- `JvmMonitorController` (`com.ruoyi.web.controller.monitor`):
  - `listTargets()`: 管理监控目标。
  - `getRealtimeInfo(targetId)`: 获取当前 JMX 信息。
  - `getHistory(targetId, timeRange)`: 获取图表数据。
  - `gc(targetId)`: 触发 GC。
  - `listAlerts()`: 管理告警规则。

## 4. 前端实现 (ruoyi-ui)
- 在 `src/views/monitor/jvm` 中添加视图：
  - `index.vue`: 目标列表，显示状态。
  - `detail.vue`: 带有 ECharts 的仪表盘（堆/线程趋势），GC 按钮。
  - `history.vue`: 查询历史数据。

## 5. 测试与文档
- JMX 连接和解析的单元测试。
- Service 层的集成测试。
- 更新 `README.md` 或创建 `doc/jvm-monitor.md` 提供配置指南。

## 6. 性能
- 确保 JMX 采集轻量级。
- 如果频率较高，使用批量插入存储指标。
