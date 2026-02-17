# 服务器文件管理模块开发文档

## 1. 简介
本模块提供基于 Web 的远程服务器文件管理功能，支持文件浏览、上传、下载、删除、压缩和解压操作。前端采用类似 Windows 资源管理器的界面，后端通过 SSH (SFTP) 协议与服务器交互。

## 2. 数据库设计
本模块主要依赖 `t_ops_server` 表（服务器资产表）来获取连接信息，无独立的业务数据表。

### 2.1 菜单配置
*   一级菜单：运维管理
*   二级菜单：文件管理 (路由: `/ops/file`)
*   详细 SQL 见 `sql/ops_module_menu.sql`

## 3. 后端实现 (Java)

### 3.1 核心类
*   **Service**: `com.ruoyi.web.service.OpsFileService`
*   **Controller**: `com.ruoyi.web.controller.system.OpsFileController`
*   **Utils**: `com.ruoyi.common.utils.file.FileUploadUtils` (辅助)

### 3.2 核心功能实现
基于 `JSch` 的 `ChannelSftp` 实现：
*   **浏览 (`listFiles`)**: 获取指定路径下的文件列表，包含名称、大小、权限、修改时间、是否目录等属性。
*   **上传 (`uploadFile`)**: 将前端 `MultipartFile`流写入远程目标路径。
*   **下载 (`downloadFile`)**: 读取远程文件流并输出到 `HttpServletResponse`。注意处理了认证 Token 传递问题（支持 URL 参数 `Authorization`）。
*   **删除 (`deleteFile`)**: 递归删除文件或目录（`rm -rf`）。
*   **压缩 (`compressFile`)**: 执行 `tar -czvf` 命令。
*   **解压 (`extractFile`)**: 执行 `tar -xzvf` 或 `unzip` 命令。

## 4. 前端实现 (Vue)

### 4.1 页面结构
*   路径: `src/views/ops/file/index.vue` (主入口)
*   组件: `src/views/ops/server/fileManager.vue` (文件列表核心组件)

### 4.2 关键特性
*   **多服务器切换**: 左侧列表选择服务器，右侧动态加载文件。
*   **连接状态管理**:
    *   使用 Element Plus Tag (`<el-tag>`) 展示连接状态（连接中、已连接、失败）。
    *   实现了独立的连接超时处理（10s）和组件卸载时的状态清理，防止快速切换导致的状态错乱。
*   **面包屑导航**: 支持点击路径跳转。
*   **文件操作**:
    *   右键菜单或操作列按钮触发。
    *   上传支持拖拽。
    *   下载自动处理认证头。

## 5. 部署说明
1.  执行 `sql/ops_module_menu.sql` 初始化菜单。
2.  确保 `folder.svg` 图标已存在于 `src/assets/icons/svg` 并正确注册。
