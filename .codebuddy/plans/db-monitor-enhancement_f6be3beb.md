---
name: db-monitor-enhancement
overview: 优化数据监控页面，实现实时连接数监控、慢查询分析、表空间占用统计、SQL执行耗时统计功能，并支持仅管理员可配置监控规则
design:
  architecture:
    framework: vue
  styleKeywords:
    - Dashboard
    - Dark Theme
    - Glassmorphism
    - Data Visualization
    - Real-time Monitoring
  fontSystem:
    fontFamily: Inter
    heading:
      size: 18px
      weight: 600
    subheading:
      size: 14px
      weight: 500
    body:
      size: 13px
      weight: 400
  colorSystem:
    primary:
      - "#3b82f6"
      - "#60a5fa"
    background:
      - "#0f172a"
      - "#1e293b"
      - rgba(255,255,255,0.05)
    text:
      - "#ffffff"
      - "#94a3b8"
      - "#64748b"
    functional:
      - "#10b981"
      - "#f59e0b"
      - "#ef4444"
todos:
  - id: create-monitor-rule-table
    content: 创建监控规则表sys_db_monitor_rule及初始化SQL
    status: completed
  - id: backend-domain-mapper
    content: 创建监控规则实体类、Mapper接口和XML
    status: completed
    dependencies:
      - create-monitor-rule-table
  - id: backend-service-api
    content: 扩展ISysDbMonitorService接口，新增慢查询、SQL统计、规则管理方法
    status: completed
    dependencies:
      - backend-domain-mapper
  - id: backend-service-impl
    content: 实现监控服务，集成performance_schema查询
    status: completed
    dependencies:
      - backend-service-api
  - id: backend-controller
    content: 重构SysDbMonitorController，新增规则管理API和权限控制
    status: completed
    dependencies:
      - backend-service-impl
  - id: frontend-api
    content: 扩展db.js，新增监控相关API调用
    status: completed
    dependencies:
      - backend-controller
  - id: frontend-monitor-page
    content: 使用[skill:frontend-design]重构监控页面，实现仪表盘布局
    status: completed
    dependencies:
      - frontend-api
  - id: frontend-charts
    content: 集成ECharts，实现连接趋势、慢查询、表空间图表
    status: completed
    dependencies:
      - frontend-monitor-page
  - id: frontend-rule-config
    content: 实现监控规则配置面板（管理员权限）
    status: completed
    dependencies:
      - frontend-charts
  - id: test-integration
    content: 使用[mcp:mysql]验证慢查询和SQL统计功能
    status: completed
    dependencies:
      - frontend-rule-config
---

## 产品概述

重构数据监控页面，升级为专业级数据库性能监控平台，支持实时连接监控、慢查询分析、表空间统计、SQL执行耗时统计，并提供管理员专属的监控规则配置功能。

## 核心功能

### 1. 实时连接数监控

- 展示当前数据库连接列表（SHOW FULL PROCESSLIST）
- 显示连接ID、用户、主机、数据库、命令、时长、状态、SQL语句
- 支持按命令类型、时长筛选和排序
- 提供终止连接功能（KILL）

### 2. 慢查询分析

- 基于MySQL performance_schema或sys.statements_with_runtimes_in_95th_percentile
- 展示慢查询SQL、执行次数、平均耗时、最大耗时、总耗时
- 支持按耗时排序和TOP N展示
- 提供慢查询趋势图表

### 3. 表空间占用统计

- 展示所有表的存储占用情况
- 显示表名、注释、引擎、行数、数据大小、索引大小、创建时间
- 支持按大小排序和搜索
- 提供饼图展示TOP10表空间占用

### 4. SQL执行耗时统计

- 基于sys.statement_analysis或sys.statements_with_full_table_scans
- 展示SQL语句、执行次数、错误次数、警告次数、平均耗时
- 支持按执行次数或耗时排序

### 5. 监控规则配置（仅管理员）

- 慢查询阈值配置（执行时间超过X秒）
- 连接数阈值配置（最大连接数、空闲连接数）
- 表空间阈值配置（单表大小超过X MB）
- 规则启用/禁用开关
- 规则增删改查管理

## 技术栈选择

### 后端

- **框架**: Spring Boot + MyBatis
- **数据库**: MySQL（performance_schema/sys schema）
- **权限**: Spring Security + @PreAuthorize

### 前端

- **框架**: Vue 3 + Composition API
- **UI组件**: Element Plus
- **图表**: ECharts 5
- **布局**: 卡片式仪表盘设计

## 实现方案

### 数据库设计

```sql
-- 监控规则表
CREATE TABLE sys_db_monitor_rule (
  rule_id       bigint(20)      NOT NULL AUTO_INCREMENT,
  conn_id       bigint(20)      DEFAULT 0 COMMENT '连接ID(0代表全局)',
  rule_type     varchar(50)     NOT NULL COMMENT '规则类型(slow_query/connection/table_space)',
  metric_name   varchar(50)     NOT NULL COMMENT '指标名称',
  condition     varchar(10)     NOT NULL COMMENT '条件(GT, LT, EQ)',
  threshold     double          NOT NULL COMMENT '阈值',
  enabled       char(1)         DEFAULT '0' COMMENT '状态',
  create_by     varchar(64)     DEFAULT '',
  create_time   datetime        DEFAULT NULL,
  update_by     varchar(64)     DEFAULT '',
  update_time   datetime        DEFAULT NULL,
  remark        varchar(500)    DEFAULT NULL,
  PRIMARY KEY (rule_id)
);
```

### 后端API设计

- `GET /system/db/monitor/process` - 实时连接列表
- `GET /system/db/monitor/slow-queries` - 慢查询分析
- `GET /system/db/monitor/table-stats` - 表空间统计
- `GET /system/db/monitor/sql-stats` - SQL执行统计
- `GET /system/db/monitor/rules` - 查询监控规则（管理员）
- `POST /system/db/monitor/rules` - 新增规则（管理员）
- `PUT /system/db/monitor/rules` - 修改规则（管理员）
- `DELETE /system/db/monitor/rules/{id}` - 删除规则（管理员）
- `POST /system/db/monitor/kill` - 终止连接（管理员）

### 前端架构

- 仪表盘布局：顶部统计卡片 + 中部图表 + 底部详细表格
- 使用ECharts展示：连接数趋势、慢查询TOP10、表空间饼图
- 标签页切换：实时连接/慢查询/表空间/SQL统计/规则配置
- 规则配置页仅对管理员角色可见

## 关键技术点

### MySQL性能查询

```sql
-- 慢查询（需开启performance_schema）
SELECT * FROM sys.statements_with_runtimes_in_95th_percentile 
ORDER BY total_latency DESC LIMIT 20;

-- SQL统计
SELECT * FROM sys.statement_analysis 
ORDER BY total_latency DESC LIMIT 20;

-- 表空间
SELECT table_name, data_length, index_length 
FROM information_schema.tables 
WHERE table_schema = DATABASE();
```

### 权限控制

```java
@PreAuthorize("@ss.hasRole('admin')")  // 仅管理员
@PreAuthorize("@ss.hasPermi('system:db:monitor:rule')")  // 规则配置权限
```

### 前端性能优化

- 数据轮询：5秒刷新一次实时连接
- 图表懒加载：切换标签页时才渲染
- 虚拟滚动：大量连接数据时使用

采用现代化仪表盘设计风格，深色主题与卡片式布局结合，打造专业级数据库监控界面。

## 页面布局

### 顶部区域

- 连接选择器 + 刷新按钮
- 统计卡片（当前连接数/活跃连接数/慢查询数/总表空间）

### 中部图表区

- 连接数趋势折线图（近1小时）
- 慢查询TOP10横向柱状图
- 表空间占用饼图（TOP10表）

### 底部标签页

- **实时连接**：表格展示，支持KILL操作
- **慢查询分析**：表格+耗时分布图
- **表空间统计**：表格+大小排序
- **SQL统计**：执行次数和耗时分析
- **规则配置**：管理员专属，表单+表格

## 设计风格

### 色彩系统

- 背景：深蓝灰渐变 (#1e293b → #0f172a)
- 卡片：半透明毛玻璃效果 (rgba(255,255,255,0.05))
- 强调色：科技蓝 (#3b82f6)、警告橙 (#f59e0b)、危险红 (#ef4444)
- 文字：主文字白色、次级文字灰 (#94a3b8)

### 字体系统

- 标题：18px/600 白色
- 数据：24px/700 科技蓝
- 标签：12px/400 灰色

### 交互效果

- 卡片悬停：轻微上浮+阴影增强
- 数据刷新：脉冲动画指示器
- 图表：tooltip跟随、点击下钻

## Agent Extensions

### Skill

- **frontend-design**: 用于创建专业级数据监控仪表盘界面，实现卡片布局、ECharts图表集成、深色主题样式
- **vue-best-practices**: 确保Vue 3 Composition API规范使用，组件结构优化

### MCP

- **mysql**: 用于查询MySQL performance_schema和sys schema获取慢查询、SQL统计等监控数据