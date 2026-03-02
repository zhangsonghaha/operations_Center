# 项目结构

## 根目录布局

```
ruoyi/
├── ruoyi-admin/          # 主应用模块（入口点）
├── ruoyi-framework/      # 框架核心（安全、配置、拦截器）
├── ruoyi-system/         # 系统管理模块（用户、角色、菜单）
├── ruoyi-common/         # 通用工具和基础类
├── ruoyi-generator/      # 代码生成器模块
├── ruoyi-quartz/         # 定时任务模块
├── ruoyi-ui/             # Vue.js 前端应用
├── ruoyi-android-demo/   # Android 演示应用
├── sql/                  # 数据库脚本
├── doc/                  # 文档
├── docs/                 # 功能文档
└── bin/                  # 工具脚本
```

## 后端模块组织

### ruoyi-admin（主入口）
- `src/main/java/com/ruoyi/` - 应用入口和控制器
- `src/main/resources/` - 配置文件
  - `application.yml` - 主配置文件
  - `application-druid.yml` - 数据库配置
  - `mapper/` - MyBatis XML 映射器
  - `mybatis/mybatis-config.xml` - MyBatis 配置
- `uploadPath/` - 文件上传存储目录

### ruoyi-framework（核心框架）
- 安全配置（Spring Security + JWT）
- Web 配置（CORS、拦截器、异常处理器）
- Redis 配置和工具
- Swagger 配置
- 通用注解和切面

### ruoyi-system（系统模块）
- 领域模型（User、Role、Menu、Dept 等）
- 服务层实现
- Mapper 接口
- 系统管理业务逻辑

### ruoyi-common（共享工具）
- 基础类（BaseEntity、AjaxResult 等）
- 通用工具（StringUtils、DateUtils 等）
- 常量和枚举
- 异常定义
- 注解

### ruoyi-generator（代码生成器）
- 模板引擎（Velocity）
- 代码生成逻辑
- Controller、Service、Mapper、Vue 页面模板

### ruoyi-quartz（定时任务）
- Quartz 任务管理
- 任务执行和监控

## 前端结构（ruoyi-ui/）

```
ruoyi-ui/
├── src/
│   ├── api/              # API 请求模块
│   ├── assets/           # 静态资源（图片、样式）
│   ├── components/       # 可复用 Vue 组件
│   ├── directive/        # 自定义 Vue 指令
│   ├── layout/           # 布局组件
│   ├── plugins/          # 插件配置
│   ├── router/           # Vue Router 配置
│   ├── store/            # Pinia 状态管理模块
│   ├── utils/            # 工具函数
│   ├── views/            # 页面组件
│   ├── App.vue           # 根组件
│   └── main.js           # 应用入口
├── public/               # 公共静态文件
├── vite/                 # Vite 插件配置
├── vite.config.js        # Vite 配置
└── package.json          # 依赖
```

## 关键目录约定

### 后端 Java 包结构
```
com.ruoyi.{module}/
├── controller/           # REST 控制器（@RestController）
├── service/              # 服务接口
│   └── impl/             # 服务实现
├── mapper/               # MyBatis Mapper 接口
├── domain/               # 实体/领域模型
└── dto/                  # 数据传输对象（如果使用）
```

### MyBatis Mapper 位置
- Mapper 接口：`src/main/java/com/ruoyi/{module}/mapper/`
- Mapper XML 文件：`src/main/resources/mapper/{module}/`

### 前端视图组织
```
src/views/
├── system/               # 系统管理页面
├── monitor/              # 监控页面
├── tool/                 # 工具页面
├── login.vue             # 登录页面
└── index.vue             # 仪表板
```

## 配置文件

### 后端
- `application.yml` - Spring Boot 主配置
- `application-druid.yml` - Druid 数据源配置
- `logback.xml` - 日志配置
- `mybatis-config.xml` - MyBatis 全局配置

### 前端
- `vite.config.js` - 构建和开发服务器配置
- `.env.development` - 开发环境变量
- `.env.production` - 生产环境变量
- `.env.staging` - 预发布环境变量

## 构建产物

- 后端 JAR：`{module}/target/{module}-{version}.jar`
- 前端 dist：`ruoyi-ui/dist/`
- 上传文件：`ruoyi-admin/uploadPath/`

## 命名约定

### 后端
- 控制器：`{Entity}Controller.java`（例如：`SysUserController.java`）
- 服务：`I{Entity}Service.java` 和 `{Entity}ServiceImpl.java`
- Mapper：`{Entity}Mapper.java` 和 `{Entity}Mapper.xml`
- 领域模型：`{Entity}.java` 或系统实体使用 `Sys{Entity}.java`

### 前端
- 组件：大驼峰命名（例如：`UserDialog.vue`）
- 视图：目录使用短横线命名，文件使用大驼峰（例如：`system/user/index.vue`）
- API 模块：短横线命名（例如：`system/user.js`）
- Store 模块：小驼峰命名（例如：`user.js`）

## 模块依赖关系

```
ruoyi-admin（主模块）
  ├── 依赖：ruoyi-framework
  ├── 依赖：ruoyi-system
  ├── 依赖：ruoyi-generator
  └── 依赖：ruoyi-quartz

ruoyi-framework
  └── 依赖：ruoyi-common

ruoyi-system
  └── 依赖：ruoyi-common

ruoyi-generator
  └── 依赖：ruoyi-common

ruoyi-quartz
  └── 依赖：ruoyi-common
```

所有模块都依赖 `ruoyi-common` 获取共享工具和基础类。
