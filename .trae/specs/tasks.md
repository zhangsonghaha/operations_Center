# 部署模板文档系统任务清单

## 后端任务
- [ ] 1. 创建数据库表 `ops_deploy_doc`。
- [ ] 2. 创建 Java 实体类 `OpsDeployDoc.java`。
- [ ] 3. 创建 MyBatis Mapper `OpsDeployDocMapper.java` 和 XML `OpsDeployDocMapper.xml`。
- [ ] 4. 创建 Service 接口 `IOpsDeployDocService.java` 和实现类 `OpsDeployDocServiceImpl.java`。
- [ ] 5. 在 `OpsDeployTemplateServiceImpl` 中实现创建新模板版本时复制文档的逻辑。
- [ ] 6. 创建 Controller `OpsDeployDocController.java`。
- [ ] 7. 在 `OpsDeployDocController` 中实现文件上传/下载接口。
- [ ] 8. 添加访问日志和权限检查。

## 前端任务
- [ ] 9. 创建 `DocManager.vue` 组件，用于列表展示和管理文档。
- [ ] 10. 在 `DocManager.vue` 中实现文件上传功能。
- [ ] 11. 创建 `DocPreview.vue` 组件，支持 PDF、Word 和 Markdown 预览。
- [ ] 12. 将 `DocManager` 集成到 `deployTemplate/index.vue`（添加按钮/链接）。
- [ ] 13. 实现文档版本选择（如果适用，或者仅显示当前版本文档）。
- [ ] 14. 在 `DocManager` 中添加搜索功能。

## 验证任务
- [ ] 15. 验证数据库表创建成功。
- [ ] 16. 验证后端 API 接口（上传、列表、删除、下载）。
- [ ] 17. 验证前端 UI 集成。
- [ ] 18. 验证 PDF、Word 和 Markdown 的文档预览。
- [ ] 19. 验证版本同步逻辑（上传文档到 v1，更新模板到 v2，检查 v2 是否存在该文档）。
