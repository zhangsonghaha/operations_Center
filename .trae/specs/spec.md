# 部署模板文档系统规格说明书

## 1. 概述
本功能为部署模板模块（`deployTemplate`）增加文档管理系统。允许用户上传、管理和查看与部署模板关联的文档（PDF、Word、Markdown）。

## 2. 需求

### 2.1 功能需求
- **文档上传**：支持上传 PDF、Word (.docx) 和 Markdown (.md) 文件。
- **文档关联**：将文档链接到特定的部署模板 (`OpsDeployTemplate`)。
- **版本控制**：文档版本与模板版本同步。当模板更新（创建新版本）时，文档引用将自动带入新版本。
- **预览**：支持 PDF、Word 和 Markdown 文件的在线预览。
- **下载**：允许下载文档。
- **访问控制**：继承部署模板模块的权限。
- **日志记录**：记录文档的访问（查看/下载）行为。

### 2.2 用户界面
- **模板管理**：在模板列表或详情视图中添加“文档管理”按钮。
- **文档管理器**：弹窗或侧边栏，用于列出文档、上传新文档和执行操作（预览、下载、删除）。
- **预览器**：用于预览文件内容的专用视图或模态框。

## 3. 技术设计

### 3.1 数据库架构

**表名：`ops_deploy_doc`**

| 列名          | 类型          | 描述                                      |
|---------------|---------------|-------------------------------------------|
| `doc_id`      | BIGINT (PK)   | 主键                                      |
| `template_id` | BIGINT        | 外键，关联 `ops_deploy_template.id`       |
| `version`     | VARCHAR(20)   | 模板版本 (例如 'v1.0.0')                  |
| `doc_name`    | VARCHAR(255)  | 原始文件名                                |
| `doc_path`    | VARCHAR(500)  | 文件存储路径 (相对于上传目录)             |
| `doc_type`    | VARCHAR(20)   | 文件扩展名 (pdf, docx, md)                |
| `file_size`   | BIGINT        | 文件大小（字节）                          |
| `create_by`   | VARCHAR(64)   | 创建者                                    |
| `create_time` | DATETIME      | 创建时间                                  |
| `update_by`   | VARCHAR(64)   | 更新者                                    |
| `update_time` | DATETIME      | 更新时间                                  |
| `remark`      | VARCHAR(500)  | 备注                                      |

### 3.2 后端实现 (Java)

- **实体类**: `com.ruoyi.web.domain.OpsDeployDoc`
- **Mapper**: `com.ruoyi.web.mapper.OpsDeployDocMapper`
- **Service**: `com.ruoyi.web.service.IOpsDeployDocService` & `OpsDeployDocServiceImpl`
- **Controller**: `com.ruoyi.web.controller.system.OpsDeployDocController`
  - `GET /list`: 列出某模板版本的文档。
  - `POST /upload`: 上传文档。
  - `DELETE /{docIds}`: 删除文档。
  - `GET /download/{docId}`: 下载文档。

- **集成**:
  - 修改 `OpsDeployTemplateServiceImpl.updateOpsDeployTemplate`：创建新版本时，将现有文档记录复制到新版本（链接到相同的文件路径）。

### 3.3 前端实现 (Vue)

- **组件**:
  - `src/views/ops/deployTemplate/components/DocManager.vue`: 文档管理弹窗。
  - `src/views/ops/deployTemplate/components/DocPreview.vue`: 文件预览组件。

- **类库**:
  - Word 预览: `mammoth.js` 或 `docx-preview`。
  - PDF 预览: `pdfjs-dist` 或 `vue-pdf-embed`。
  - Markdown 预览: `marked` (现有)。

## 4. 安全与性能
- **权限**: 使用 `@PreAuthorize` 限制访问。
- **文件校验**: 上传时校验文件类型和大小。
- **懒加载**: 仅在需要时加载预览库。

## 5. 测试计划
- **单元测试**: 测试 CRUD 和版本同步的 Service 逻辑。
- **集成测试**: 验证上传、数据库持久化和文件检索。
- **UI 测试**: 验证所有支持格式的预览渲染。
