# 技术栈

## 后端

- Java 1.8
- Spring Boot 2.5.15
- Spring Security 5.7.14 + JWT (jsonwebtoken 0.9.1)
- Spring Framework 5.3.39
- MyBatis（配合 PageHelper 1.4.7 实现分页）
- Maven（多模块项目）
- Druid 1.2.27（数据库连接池）
- Redis（Lettuce 客户端）
- Quartz（定时任务）
- Apache Velocity 2.3（代码生成模板）
- Apache POI 4.1.2（Excel 操作）
- Swagger 3.0.0（API 文档）
- Fastjson2 2.0.60（JSON 处理）
- Logback 1.2.13
- Tomcat 9.0.112（内嵌）

## 前端

- Vue 3.5.26
- Element Plus 2.13.1（UI 组件库）
- Vite 6.4.1（构建工具）
- Pinia 3.0.4（状态管理）
- Vue Router 4.6.4
- Axios 1.13.2（HTTP 客户端）
- ECharts 5.6.0（图表）
- Monaco Editor 0.55.1（代码编辑器）
- BPMN.js 18.12.0（工作流图）

## 数据库

- MySQL（主数据库）
- Redis（缓存和会话存储）

## 常用命令

### 后端（Maven）

```bash
# 构建项目
mvn clean install

# 运行应用（在 ruoyi-admin 目录下）
mvn spring-boot:run

# 打包生产环境
mvn clean package

# 跳过测试打包
mvn clean package -DskipTests
```

### 前端（Yarn/NPM）

```bash
# 安装依赖
yarn install
# 或
npm install

# 启动开发服务器（运行在 80 端口）
yarn dev
# 或
npm run dev

# 构建生产环境
yarn build:prod
# 或
npm run build:prod

# 构建预发布环境
yarn build:stage
# 或
npm run build:stage

# 预览生产构建
yarn preview
# 或
npm run preview
```

### 快速启动脚本

```bash
# Windows
ry.bat

# Linux/Mac
./ry.sh
```

## 开发配置

- 后端运行在 8080 端口
- 前端开发服务器运行在 80 端口
- 前端将 `/dev-api` 请求代理到后端 `http://localhost:8080`
- 前端（Vite HMR）和后端（Spring DevTools）均启用热重载

## 构建输出

- 后端：`target/` 目录下的 JAR 文件
- 前端：`ruoyi-ui/dist/` 目录下的静态文件

## 关键库使用说明

- 使用 MyBatis XML 映射器（位于 `resources/mapper/`）
- 使用 PageHelper 实现分页（已配置为 MySQL）
- JWT 令牌存储在 Authorization 请求头中
- Redis 用于缓存和会话管理
- Druid 用于数据库连接池和 SQL 监控
