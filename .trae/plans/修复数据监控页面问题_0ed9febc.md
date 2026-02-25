---
name: 修复数据监控页面问题
overview: 修复数据库监控页面的三个问题：1) 统计卡片无数据 2) 右侧图表显示undefined 3) 监控规则表缺失
todos:
  - id: create-sys-db-conn-sql
    content: 创建 sys_db_conn 表的 SQL 文件
    status: completed
  - id: fix-backend-field-case
    content: 修复后端 SysDbMonitorServiceImpl 字段大小写问题
    status: completed
    dependencies:
      - create-sys-db-conn-sql
  - id: fix-frontend-field-case
    content: 修复前端 index.vue 字段名兼容处理
    status: completed
    dependencies:
      - fix-backend-field-case
  - id: verify-database-tables
    content: 使用 [MCP:MCP Server for MySQL based on NodeJS] 验证数据库表结构
    status: completed
    dependencies:
      - fix-frontend-field-case
---

## 问题概述

数据监控页面存在三个主要问题：

1. **统计卡片显示0**：总连接数、活跃连接、慢查询、总表空间都显示为0
2. **表空间TOP10图表显示"undefined"**：右侧柱状图Y轴标签全部显示undefined
3. **监控规则表格无数据**：显示"暂无数据"

## 问题根因

1. **缺少 `sys_db_conn` 表**：数据库连接配置表只有Java代码，没有对应的SQL创建语句
2. **字段名大小写不匹配**：后端查询 `information_schema.tables` 返回的字段名是大写的（如 `TABLE_NAME`），但前端使用小写的 `table_name` 访问
3. **`sys_db_monitor_rule` 表未创建**：虽然SQL文件存在，但可能未执行

## 核心功能

- 修复数据库监控页面的数据展示问题
- 确保表空间统计图表正确显示表名
- 确保监控规则能够正常加载和展示

## 技术栈

- **后端**：Java + Spring Boot + MyBatis
- **前端**：Vue 3 + Element Plus + ECharts
- **数据库**：MySQL 8.0

## 问题分析

### 问题1：字段名大小写不一致

在 `SysDbMonitorServiceImpl.getTableStats()` 中，SQL查询使用小写字段别名：

```sql
SELECT table_name, table_comment, ... FROM information_schema.tables
```

但 JDBC 驱动返回的 `ResultSet` 中，列名可能被转换为大写（`TABLE_NAME`），导致前端访问 `item.table_name` 得到 `undefined`。

### 问题2：缺少数据库表

- `sys_db_conn`：存储数据库连接配置，Java代码已存在但缺少建表SQL
- `sys_db_monitor_rule`：存储监控规则，SQL文件已存在但需确保执行

## 实现方案

1. **创建缺失的SQL文件**：`sql/sys_db_conn.sql` 创建连接配置表
2. **修复后端代码**：在 `SysDbMonitorServiceImpl` 中统一转换字段名为小写
3. **修复前端代码**：在 `index.vue` 中兼容处理大小写不一致的字段名

## 目录结构

```
e:/package/operations_Center/
├── sql/
│   ├── sys_db_conn.sql              # [NEW] 创建sys_db_conn表
│   └── db_monitor.sql               # [已有] 创建sys_db_monitor_rule表
├── ruoyi-system/
│   └── src/main/java/com/ruoyi/system/service/impl/
│       └── SysDbMonitorServiceImpl.java  # [MODIFY] 修复字段大小写问题
└── ruoyi-ui/src/views/system/db/monitor/
    └── index.vue                    # [MODIFY] 兼容字段名大小写
```

## 关键修改点

### 后端修复 (SysDbMonitorServiceImpl.java)

在 `getTableStats()` 方法返回前，将Map中的键名统一转换为小写：

```java
// 转换键名为小写，避免information_schema返回大写键名
List<Map<String, Object>> result = dbExecuteService.executeSelect(connId, sql);
List<Map<String, Object>> normalizedResult = new ArrayList<>();
for (Map<String, Object> row : result) {
    Map<String, Object> normalizedRow = new HashMap<>();
    for (Map.Entry<String, Object> entry : row.entrySet()) {
        normalizedRow.put(entry.getKey().toLowerCase(), entry.getValue());
    }
    normalizedResult.add(normalizedRow);
}
return normalizedResult;
```

### 前端修复 (index.vue)

在 `updateTableSpaceChart()` 中兼容处理字段名：

```javascript
// 兼容大小写字段名
const tableName = item.table_name || item.TABLE_NAME || '';
const totalLength = item.total_length || item.TOTAL_LENGTH || 0;
```

## Agent Extensions

### MCP

- **MCP Server for MySQL based on NodeJS**
- **用途**：验证数据库表是否存在，查询表结构信息
- **预期结果**：确认 `sys_db_conn` 和 `sys_db_monitor_rule` 表的状态，验证修复后的数据查询结果