# 数据库管理功能开发计划

本计划旨在为 RuoYi-Vue 项目添加一个全新的“数据库管理”模块，支持多数据源连接管理、SQL 在线执行、监控及备份恢复等功能。

## 1. 数据库设计 (Database Design)

### 1.1 新增表结构
我们需要新增以下三张表来存储相关数据：

1.  **`sys_db_conn` (数据库连接配置表)**
    *   `conn_id` (Long, PK): 连接ID
    *   `conn_name` (String): 连接名称
    *   `db_type` (String): 数据库类型 (MySQL, Oracle, etc. 目前主要支持 MySQL)
    *   `host` (String): 主机地址
    *   `port` (String): 端口
    *   `username` (String): 账号
    *   `password` (String): 密码 (加密存储)
    *   `db_name` (String): 数据库名
    *   `status` (String): 状态 (0=正常, 1=停用)
    *   `create_by`, `create_time`, `update_by`, `update_time`, `remark`: 通用字段

2.  **`sys_db_backup` (数据库备份记录表)**
    *   `backup_id` (Long, PK): 备份ID
    *   `conn_id` (Long): 关联的连接ID
    *   `file_name` (String): 备份文件名
    *   `file_path` (String): 文件存储路径
    *   `backup_type` (String): 备份类型 (0=手动, 1=自动)
    *   `status` (String): 状态 (0=成功, 1=失败)
    *   `log_msg` (String): 日志信息
    *   `create_by`, `create_time`: 通用字段

3.  **`sys_db_log` (数据库操作日志表)**
    *   `log_id` (Long, PK): 日志ID
    *   `conn_id` (Long): 关联的连接ID
    *   `sql_content` (String): 执行的SQL语句
    *   `cost_time` (Long): 耗时(ms)
    *   `status` (String): 执行状态
    *   `error_msg` (String): 错误信息
    *   `create_by`, `create_time`: 通用字段

### 1.2 菜单与权限 SQL
需要编写 SQL 脚本插入 `sys_menu` 表，创建以下菜单结构：
*   **数据库管理** (一级菜单, 目录)
    *   **连接管理** (二级菜单, 菜单) -> `system/db/conn/index`
    *   **数据操作** (二级菜单, 菜单) -> `system/db/execute/index`
    *   **数据监控** (二级菜单, 菜单) -> `system/db/monitor/index`
    *   **备份恢复** (二级菜单, 菜单) -> `system/db/backup/index`
    *   **操作日志** (二级菜单, 菜单) -> `system/db/log/index`

## 2. 后端开发 (Backend Development)

代码将放置在 `ruoyi-system` 模块中（业务逻辑），Controller 放在 `ruoyi-admin` 模块中。

### 2.1 实体类 (Domain)
*   `com.ruoyi.system.domain.SysDbConn`
*   `com.ruoyi.system.domain.SysDbBackup`
*   `com.ruoyi.system.domain.SysDbLog`

### 2.2 数据访问层 (Mapper)
*   `SysDbConnMapper`: CRUD
*   `SysDbBackupMapper`: CRUD
*   `SysDbLogMapper`: CRUD

### 2.3 业务逻辑层 (Service)
*   **`ISysDbConnService`**:
    *   CRUD 连接配置。
    *   `testConnection(SysDbConn conn)`: 测试数据库连接有效性。
    *   密码加密/解密处理。
*   **`IDbExecuteService`**:
    *   **核心功能**: 动态获取数据库连接。
    *   `List<String> getTableList(Long connId)`: 获取表列表。
    *   `List<Map<String, Object>> executeSql(Long connId, String sql)`: 执行查询 SQL。
    *   `int executeUpdate(Long connId, String sql)`: 执行增删改 SQL。
    *   维护一个 `Map<Long, DataSource>` 缓存，避免频繁创建连接，或者使用 `JdbcTemplate` 动态创建。
*   **`ISysDbBackupService`**:
    *   `backup(Long connId)`: 执行 `mysqldump` 命令或 SQL 导出。
    *   `restore(Long backupId)`: 执行 SQL 恢复。
*   **`ISysDbMonitorService`**:
    *   获取实时连接数 (`SHOW PROCESSLIST`)。
    *   获取慢查询日志。

### 2.4 控制层 (Controller)
*   `SysDbConnController`: `/system/db/conn`
*   `SysDbExecuteController`: `/system/db/execute`
*   `SysDbMonitorController`: `/system/db/monitor`
*   `SysDbBackupController`: `/system/db/backup`
*   `SysDbLogController`: `/system/db/log`

## 3. 前端开发 (Frontend Development)

### 3.1 API 封装 (`ruoyi-ui/src/api/system/db.js`)
封装所有后端接口请求。

### 3.2 页面开发 (`ruoyi-ui/src/views/system/db/`)
1.  **连接管理 (`conn/index.vue`)**:
    *   表格展示连接列表。
    *   新增/编辑/删除/测试连接 弹窗。
2.  **数据操作 (`execute/index.vue`)**:
    *   左侧：数据库表树形列表。
    *   右侧上部：SQL 编辑器 (使用 `vue-codemirror` 或简单 `textarea`)。
    *   右侧下部：执行结果表格 / 消息提示。
3.  **数据监控 (`monitor/index.vue`)**:
    *   ECharts 图表展示连接数、慢查询等。
4.  **备份恢复 (`backup/index.vue`)**:
    *   备份记录列表。
    *   手动备份按钮。
    *   恢复/下载 操作。
5.  **操作日志 (`log/index.vue`)**:
    *   标准查询表格。

## 4. 安全与权限 (Security)

*   使用 Spring Security 注解控制权限。
*   **角色分配建议**:
    *   `admin`: 拥有所有权限。
    *   `db_admin`: 拥有所有数据库管理权限。
    *   `db_ops`: 仅拥有连接查看、监控查看、只读查询权限。
*   **敏感操作保护**:
    *   连接密码不回显给前端（显示为 `******`）。
    *   执行 `DROP`, `TRUNCATE` 等高危 SQL 时增加二次确认或仅允许特定角色执行。

## 5. 待确认事项

1.  **SQL 解析与安全**: 是否需要集成 SQL 解析器 (如 JSqlParser) 来拦截高危 SQL？
    *   *建议*: 暂时先做基础的关键词匹配拦截，后期可优化。
2.  **多数据库支持**: 目前主要支持 MySQL，是否需要考虑 Oracle/PostgreSQL？
    *   *建议*: 第一版仅支持 MySQL。
3.  **在线编辑器组件**: 是否引入 `vue-codemirror` 以获得更好的 SQL 编辑体验？
    *   *建议*: 引入，提升体验。

请确认以上计划，确认后将开始实施。
