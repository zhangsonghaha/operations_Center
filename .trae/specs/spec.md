# 流程追踪系统增强与站内信系统开发规格说明书

## 1. 数据库设计

### 1.1 站内信表 `sys_message`
| 字段名 | 类型 | 长度 | 说明 |
| :--- | :--- | :--- | :--- |
| message_id | bigint | 20 | 主键ID |
| sender_id | bigint | 20 | 发送者ID |
| receiver_id | bigint | 20 | 接收者ID |
| title | varchar | 255 | 标题 |
| content | longtext | - | 内容（支持富文本） |
| message_type | char | 1 | 消息类型（1=通知, 2=待办, 3=催办, 4=完结） |
| read_status | char | 1 | 阅读状态（0=未读, 1=已读） |
| delete_status | char | 1 | 删除状态（0=正常, 1=回收站, 2=彻底删除） |
| create_time | datetime | - | 创建时间 |
| read_time | datetime | - | 阅读时间 |
| attachment | varchar | 500 | 附件路径（JSON格式或逗号分隔） |
| business_id | varchar | 64 | 关联业务ID（如流程实例ID） |

## 2. 后端接口设计

### 2.1 站内信控制器 `SysMessageController`
- `GET /system/message/list`: 获取消息列表（分页，支持按箱类型：inbox/sent/draft/trash 过滤）
- `GET /system/message/unreadCount`: 获取未读消息数量
- `GET /system/message/{messageId}`: 获取消息详情
- `POST /system/message`: 发送消息（支持草稿）
- `PUT /system/message`: 修改消息（主要用于标记已读、恢复删除）
- `DELETE /system/message/{messageIds}`: 删除消息（逻辑删除/物理删除）

### 2.2 流程增强接口 `OpsWorkflowController` (现有或新增)
- `POST /system/ops/workflow/urge`: 发起催办
    - 参数: `processInstanceId`, `reason`
    - 逻辑: 查询当前任务办理人 -> 生成"催办"类型的站内信 -> 发送
- `GET /system/ops/workflow/node-detail/{procInstId}/{activityId}`: 获取节点详细信息
    - 返回: 审批人、审批时间、意见、耗时、日志

## 3. 前端功能设计

### 3.1 流程追踪组件升级 (`BpmnViewer`)
- **交互**:
    - 监听节点点击事件，打开 `NodeDetailDialog`。
    - 增加缩放工具栏（放大、缩小、还原、适应屏幕）。
    - 增加导出按钮（导出为 SVG/PNG/PDF）。
- **展示**:
    - 顶部显示流程总进度条（根据已完成节点/总节点估算）。
    - 节点详情弹窗：Tab页展示（基本信息、操作日志、耗时分析）。

### 3.2 站内信模块 (`views/system/message`)
- **布局**: 左侧菜单（收件箱、已发送...），右侧列表/详情。
- **列表**: 表格展示，支持按类型筛选，支持批量操作。
- **编辑器**: 集成富文本编辑器（Quill 或 RuoYi 自带 Editor），支持文件上传。

### 3.3 全局通知
- **Navbar**: 右上角增加铃铛图标，显示未读红点/数字。
- **通知机制**: 轮询（每30秒）检查未读数。
- **浏览器通知**: 使用 `Notification` API。

## 4. 待办任务集成
- 在站内信详情页，如果 `message_type` 为待办或催办，且 `business_id` 存在，显示"前往处理"按钮。
- 点击跳转到审批页面，自动加载对应的任务。

