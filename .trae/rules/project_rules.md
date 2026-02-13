---
alwaysApply: true
---
# 若依框架项目规则体系

## 1. 项目架构分析

### 1.1 整体架构
- **架构类型**：基于SpringBoot+Vue的前后端分离架构
- **版本信息**：RuoYi v3.9.0
- **模块划分**：
  - `ruoyi-admin`：系统启动模块
  - `ruoyi-framework`：核心框架模块
  - `ruoyi-system`：系统业务模块
  - `ruoyi-common`：通用工具模块
  - `ruoyi-quartz`：定时任务模块
  - `ruoyi-generator`：代码生成模块
  - `ruoyi-ui`：前端Vue项目

### 1.2 技术栈
- **后端**：
  - Spring Boot 2.5.15
  - Spring Security 5.7.14
  - MyBatis（通过PageHelper集成）
  - Redis
  - JWT认证
  - Druid连接池
  - Swagger3接口文档
- **前端**：
  - Vue 2.6.12
  - Element UI 2.15.14
  - Vuex 3.6.0
  - Vue Router 3.4.9
  - Axios 0.28.1

### 1.3 分层架构
- **表现层**：Controller处理HTTP请求
- **业务层**：Service实现业务逻辑
- **数据访问层**：Mapper操作数据库
- **实体层**：Domain定义数据模型
- **通用层**：Common提供工具类和通用组件

## 2. 代码规范

### 2.1 Java代码规范

#### 2.1.1 命名规范
- **包命名**：使用小写字母，采用反域名格式，如`com.ruoyi.system`
- **类命名**：使用驼峰命名法，首字母大写，如`UserController`
- **方法命名**：使用驼峰命名法，首字母小写，如`getUserList()`
- **变量命名**：使用驼峰命名法，首字母小写，如`userId`
- **常量命名**：使用全大写字母，单词间用下划线分隔，如`MAX_PAGE_SIZE`

#### 2.1.2 代码风格
- **缩进**：使用4个空格进行缩进
- **行宽**：单行代码不超过120个字符
- **大括号**：使用K&R风格，左大括号不换行
- **空格**：
  - 操作符前后各加一个空格
  - 逗号、冒号、分号后加一个空格
  - 左括号前加一个空格

#### 2.1.3 注释规范
- **类注释**：使用Javadoc格式，包含类的用途、作者信息
- **方法注释**：使用Javadoc格式，包含方法功能、参数说明、返回值说明
- **代码注释**：复杂逻辑必须添加注释，说明实现思路

#### 2.1.4 示例代码
```java
/**
 * 用户控制器
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/system/user")
public class UserController extends BaseController {
    
    @Autowired
    private IUserService userService;
    
    /**
     * 查询用户列表
     * 
     * @param user 用户信息
     * @return 用户列表
     */
    @PreAuthorize("@ss.hasPermi('system:user:list')")
    @GetMapping("/list")
    public TableDataInfo list(SysUser user) {
        startPage();
        List<SysUser> list = userService.selectUserList(user);
        return getDataTable(list);
    }
}
```

### 2.2 前端代码规范

#### 2.2.1 命名规范
- **组件命名**：使用PascalCase（大驼峰），如`UserProfile.vue`
- **变量命名**：使用camelCase（小驼峰），如`userName`
- **方法命名**：使用camelCase，如`getUserData()`
- **常量命名**：使用全大写，单词间用下划线分隔，如`MAX_RETRY_COUNT`

#### 2.2.2 Vue组件规范
- **组件结构**：遵循template-script-style顺序
- **Prop定义**：必须指定类型和默认值
- **数据管理**：组件内部状态使用data，全局状态使用Vuex
- **路由定义**：严格遵循现有路由配置规范，包含name、meta等属性

#### 2.2.3 示例代码
```vue
<template>
  <div class="user-profile">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>个人信息</span>
        </div>
      </template>
      <el-form :model="userInfo" label-width="80px">
        <el-form-item label="用户名">
          <el-input v-model="userInfo.userName" disabled></el-input>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'UserProfile',
  data() {
    return {
      userInfo: {}
    }
  },
  created() {
    this.getUserInfo()
  },
  methods: {
    getUserInfo() {
      this.$http.get('/system/user/profile').then(response => {
        this.userInfo = response.data
      })
    }
  }
}
</script>

<style scoped>
.user-profile {
  padding: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
```

## 3. 提交规范

### 3.1 提交信息格式
```
<type>[optional scope]: <description>

[optional body]

[optional footer(s)]
```

### 3.2 提交类型（Type）
- **feat**：新功能
- **fix**：修复bug
- **docs**：文档更新
- **style**：代码风格调整（不影响功能）
- **refactor**：代码重构（既不是修复bug也不是添加功能）
- **perf**：性能优化
- **test**：添加或修改测试
- **chore**：构建过程或辅助工具变动

### 3.3 提交示例
```
feat(用户管理): 添加用户批量导入功能

支持Excel格式的用户数据批量导入，包含数据校验和重复检测
```

## 4. 分支管理策略

### 4.1 分支类型
- **master**：主分支，生产环境代码
- **develop**：开发分支，集成所有功能开发
- **feature/\<功能名称\>**：功能开发分支
- **bugfix/\<bug编号\>**：bug修复分支
- **release/\<版本号\>**：版本发布分支

### 4.2 分支流程
1. 从develop分支创建feature分支进行功能开发
2. 功能开发完成后合并回develop分支
3. 准备发布时，从develop分支创建release分支
4. 测试通过后，将release分支同时合并到master和develop分支
5. 生产环境发现bug时，从master分支创建bugfix分支进行修复
6. 修复完成后，将bugfix分支同时合并到master和develop分支

### 4.3 分支命名示例
- `feature/user-management`
- `bugfix/login-error`
- `release/v1.2.0`

## 5. 开发流程

### 5.1 需求分析
- 详细理解业务需求
- 编写需求文档
- 进行技术可行性分析

### 5.2 设计阶段
- 数据库设计
- API接口设计
- 前端页面设计
- 关键类和方法设计

### 5.3 编码阶段
- 使用代码生成器生成基础代码
- 按照规范实现业务逻辑
- 编写单元测试

### 5.4 测试阶段
- 单元测试
- 集成测试
- 功能测试
- 性能测试

### 5.5 部署阶段
- 构建应用
- 部署到测试环境
- 部署到生产环境
- 监控应用运行状态

## 6. 质量标准

### 6.1 代码质量
- 代码覆盖率：核心业务代码覆盖率不低于80%
- 静态代码分析：使用SonarQube进行代码质量检查
- 代码审查：必须经过至少一次代码审查才能合并

### 6.2 性能要求
- 接口响应时间：90%的接口响应时间不超过500ms
- 系统并发：支持至少100用户同时在线操作
- 数据库查询：避免全表扫描，合理使用索引

### 6.3 安全要求
- 输入验证：所有用户输入必须进行验证
- SQL注入防护：使用参数化查询
- XSS防护：对输出进行适当编码
- CSRF防护：实现CSRF令牌验证
- 密码安全：使用安全的密码加密算法

## 7. 文档要求

### 7.1 技术文档
- 架构设计文档：系统整体架构、技术选型等
- API接口文档：使用Swagger自动生成并维护
- 数据库设计文档：表结构、关系图等

### 7.2 开发文档
- 开发环境配置文档
- 开发规范文档
- 常见问题解决方案

### 7.3 用户文档
- 用户手册：功能介绍、操作步骤等
- 管理员手册：系统配置、维护说明等

## 8. 数据库规范

### 8.1 命名规范
- **表名**：使用`t_`前缀，采用下划线分隔，如`t_sys_user`
- **字段名**：使用下划线分隔，如`user_name`
- **索引名**：主键索引使用`PRIMARY`，唯一索引使用`idx_表名_字段名`

### 8.2 设计规范
- 所有表必须包含`id`、`create_by`、`create_time`、`update_by`、`update_time`字段
- 逻辑删除字段统一使用`del_flag`，值为`0`表示正常，`1`表示删除
- 状态字段使用`status`，遵循统一的状态码定义

### 8.3 示例表结构
```sql
CREATE TABLE `t_sys_user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `user_name` varchar(30) NOT NULL COMMENT '用户名',
  `nick_name` varchar(30) NOT NULL COMMENT '昵称',
  `password` varchar(100) DEFAULT NULL COMMENT '密码',
  `status` char(1) DEFAULT '0' COMMENT '状态（0正常 1停用）',
  `create_by` varchar(64) DEFAULT '' COMMENT '创建者',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT '' COMMENT '更新者',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `del_flag` char(1) DEFAULT '0' COMMENT '删除标志（0代表存在 2代表删除）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx_user_name` (`user_name`)
) ENGINE=InnoDB AUTO_INCREMENT=100 COMMENT='用户信息表';
```

## 9. API接口规范

### 9.1 接口设计原则
- **RESTful风格**：使用HTTP方法表示操作类型
- **统一响应格式**：所有接口返回统一的JSON格式
- **版本控制**：API路径包含版本信息，如`/api/v1/...`

### 9.2 HTTP方法使用
- `GET`：获取资源
- `POST`：创建资源
- `PUT`：更新资源
- `DELETE`：删除资源

### 9.3 响应格式规范
```json
{
  "code": 200,
  "msg": "操作成功",
  "data": {}
}
```

### 9.4 接口示例
```java
/**
 * 新增用户
 */
@PreAuthorize("@ss.hasPermi('system:user:add')")
@Log(title = "用户管理", businessType = BusinessType.INSERT)
@PostMapping
public AjaxResult add(@Validated @RequestBody SysUser user) {
    if (UserConstants.NOT_UNIQUE.equals(userService.checkUserNameUnique(user.getUserName()))) {
        return AjaxResult.error("新增用户'" + user.getUserName() + "'失败，用户名已存在");
    }
    user.setCreateBy(getUsername());
    return toAjax(userService.insertUser(user));
}
```

## 10. 安全规范

### 10.1 认证与授权
- 使用JWT进行身份认证
- 基于RBAC模型进行权限控制
- 实现细粒度的权限校验（菜单权限、按钮权限）

### 10.2 数据安全
- 敏感数据加密存储
- 防止SQL注入、XSS攻击、CSRF攻击
- 实现请求频率限制，防止暴力攻击

### 10.3 日志记录
- 记录关键操作日志
- 记录异常日志
- 记录登录日志

## 11. 部署规范

### 11.1 环境配置
- 开发环境：JDK 1.8+、MySQL 5.7+、Node.js 10+
- 测试环境：与生产环境保持一致
- 生产环境：使用Tomcat 9.0+、Nginx作为前端代理

### 11.2 配置管理
- 环境变量分离：开发、测试、生产环境配置分离
- 敏感配置加密：数据库密码等敏感信息加密存储

### 11.3 构建与部署流程
- 使用Maven构建后端项目
- 使用npm/yarn构建前端项目
- 推荐使用Docker容器化部署

## 12. 其他规范

### 12.1 环境配置
- 开发环境：统一使用JDK 1.8
- 数据库：MySQL 5.7+
- 前端开发环境：Node.js 10+

### 12.2 日志规范
- 使用SLF4J作为日志接口
- 日志级别：DEBUG、INFO、WARN、ERROR
- 关键操作必须记录日志，包括操作人、操作时间、操作内容

### 12.3 异常处理
- 使用统一的异常处理机制
- 异常信息必须包含足够的上下文信息
- 用户友好的错误提示

### 12.4 代码生成规范
- 不强制要求使用系统提供的代码生成器
- 生成的代码必须符合项目规范
- 避免直接修改生成的代码，通过继承或组合方式扩展功能

## 13. 实施建议

1. **渐进式实施**：先从核心规范开始，逐步扩展到全部规范
2. **工具支持**：配置IDE插件辅助规范实施
3. **培训宣导**：对团队成员进行规范培训
4. **监督检查**：定期检查规范执行情况
5. **持续改进**：根据项目实际情况不断优化和完善规范

通过严格执行以上规则体系，可以确保项目代码质量、提高开发效率、降低维护成本，保障项目的长期健康发展。