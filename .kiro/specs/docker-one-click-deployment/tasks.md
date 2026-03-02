# Implementation Plan: Docker 一键部署功能

## 概述

本实施计划将 Docker 一键部署功能集成到若依运维管理平台中。该功能通过可视化界面和自动化脚本，简化 Docker 容器的部署流程，包括环境检测、镜像拉取、容器配置、启动和监控。实现将使用 Java 1.8 + Spring Boot 2.5.15，与现有的 SSH 部署基础设施深度集成。

## 任务列表

- [ ] 1. 创建数据库表结构和初始化数据
  - 创建 t_docker_container 表存储容器信息
  - 创建 t_docker_template 表存储部署模板
  - 创建 t_docker_image 表缓存镜像信息
  - 为 t_ops_deploy_log 表添加 deploy_type 字段
  - 插入 Docker 管理菜单和权限数据
  - 插入预定义模板数据（MySQL、Redis、Nginx、MongoDB）
  - _需求: 13.1, 13.6, 15.1, 15.10_

- [ ] 2. 实现后端领域模型和数据传输对象
  - [ ] 2.1 创建 Docker 领域模型类
    - 在 ruoyi-admin 模块创建 com.ruoyi.web.domain 包
    - 实现 DockerContainer 实体类（继承 BaseEntity）
    - 实现 DockerTemplate 实体类
    - 实现 DockerImage 实体类
    - 添加必要的字段、getter/setter 和注解
    - _需求: 15.1, 15.2, 15.3, 15.8, 15.9, 15.10_

  - [ ] 2.2 创建配置和请求 DTO 类
    - 实现 DockerDeployConfig 类（部署配置）
    - 实现 PortMapping 类（端口映射）
    - 实现 VolumeMount 类（卷挂载）
    - 实现 ResourceLimit 类（资源限制）
    - 实现 DockerEnvInfo 类（环境信息）
    - 实现 DockerContainerStatus 类（容器状态）
    - 实现 DockerContainerStats 类（容器统计）
    - 实现 DockerDeployResult 类（部署结果）
    - _需求: 1.8, 4.1, 5.1, 8.3, 8.6_

- [ ] 3. 实现 MyBatis Mapper 层
  - [ ] 3.1 创建 Mapper 接口
    - 在 com.ruoyi.web.mapper 包创建 DockerContainerMapper 接口
    - 在 com.ruoyi.web.mapper 包创建 DockerTemplateMapper 接口
    - 在 com.ruoyi.web.mapper 包创建 DockerImageMapper 接口
    - 定义 CRUD 方法和自定义查询方法
    - _需求: 15.1, 15.8, 15.10_

  - [ ] 3.2 创建 Mapper XML 文件
    - 在 resources/mapper/web/ 创建 DockerContainerMapper.xml
    - 在 resources/mapper/web/ 创建 DockerTemplateMapper.xml
    - 在 resources/mapper/web/ 创建 DockerImageMapper.xml
    - 实现 SQL 映射（insert、update、delete、select）
    - 添加必要的索引查询优化
    - _需求: 12.10, 15.1, 15.8, 15.10_

- [ ] 4. 实现 Docker 环境检测服务
  - [ ] 4.1 创建 DockerEnvService 接口和实现类
    - 在 com.ruoyi.web.service 创建 IDockerEnvService 接口
    - 在 com.ruoyi.web.service.impl 创建 DockerEnvServiceImpl
    - 注入现有的 DeployServiceImpl 以复用 SSH 连接
    - _需求: 13.3, 13.4_

  - [ ] 4.2 实现 Docker 安装检测方法
    - 实现 checkDockerEnvironment(Long serverId) 方法
    - 执行 "docker --version" 命令检查 Docker 是否安装
    - 解析版本信息并设置 dockerInstalled 和 dockerVersion
    - 如果未安装，返回 dockerInstalled=false
    - _需求: 1.1, 1.2, 1.3_

  - [ ] 4.3 实现 Docker 服务运行检测
    - 执行 "docker info" 命令检查服务是否运行
    - 捕获异常并设置 dockerRunning=false
    - 解析 JSON 格式输出获取存储驱动和根目录
    - _需求: 1.4, 1.5, 1.6_

  - [ ] 4.4 实现磁盘空间检测
    - 执行 "df -B1 <docker_root_dir>" 命令
    - 解析输出获取总空间和已用空间
    - 设置 totalSpace 和 usedSpace 字段
    - _需求: 1.7, 1.8_

  - [ ]* 4.5 编写环境检测单元测试
    - 测试 Docker 已安装场景
    - 测试 Docker 未安装场景
    - 测试 Docker 未运行场景
    - Mock SSH 会话和命令执行

- [ ] 5. 实现部署前验证服务
  - [ ] 5.1 创建 DockerValidationService 接口和实现类
    - 在 com.ruoyi.web.service 创建 IDockerValidationService 接口
    - 在 com.ruoyi.web.service.impl 创建 DockerValidationServiceImpl
    - _需求: 2.1, 10.1, 10.2_

  - [ ] 5.2 实现容器名称验证
    - 实现 validateContainerName(String name) 方法
    - 使用正则表达式验证仅包含字母、数字、下划线和连字符
    - 抛出异常如果格式不正确
    - _需求: 2.1, 10.2_

  - [ ] 5.3 实现容器名称唯一性检查
    - 实现 checkContainerNameExists(Session, String) 方法
    - 执行 "docker ps -a --filter name=<name> --format '{{.Names}}'" 命令
    - 检查输出是否包含完全匹配的容器名
    - _需求: 2.2, 2.3_

  - [ ] 5.4 实现端口冲突检查
    - 实现 checkPortInUse(Session, int port) 方法
    - 优先使用 "ss -tuln | grep ':<port> '" 命令
    - 回退到 "netstat -tuln" 或 "lsof -i :<port>"
    - 返回端口是否被占用
    - _需求: 2.4, 2.5_

  - [ ] 5.5 实现卷挂载路径安全验证
    - 实现 validateVolumeMounts(List<VolumeMount>) 方法
    - 检查是否包含敏感路径（/etc, /root, /var/run/docker.sock, /sys, /proc）
    - 抛出 SecurityException 如果包含敏感路径
    - _需求: 2.6, 2.7, 10.3_

  - [ ]* 5.6 编写验证服务单元测试
    - 测试容器名称格式验证
    - 测试端口冲突检查
    - 测试卷挂载安全验证

- [ ] 6. 实现 Docker 命令构建工具类
  - [ ] 6.1 创建 DockerCommandBuilder 工具类
    - 在 com.ruoyi.web.utils 包创建 DockerCommandBuilder 类
    - 实现 buildDockerRunCommand(DockerDeployConfig) 方法
    - _需求: 4.1, 4.2, 10.5_

  - [ ] 6.2 实现基础命令构建
    - 添加 "docker run -d" 基础命令
    - 添加 "--name" 参数和转义的容器名
    - 添加镜像名称和标签
    - _需求: 4.2_

  - [ ] 6.3 实现端口映射参数构建
    - 遍历 ports 列表添加 "-p" 参数
    - 格式: hostPort:containerPort/protocol
    - _需求: 4.3_

  - [ ] 6.4 实现环境变量参数构建
    - 遍历 envVars 映射添加 "-e" 参数
    - 使用 escapeShellArg() 转义特殊字符
    - 格式: -e KEY='value'
    - _需求: 4.4, 10.1, 10.5_

  - [ ] 6.5 实现卷挂载参数构建
    - 遍历 volumes 列表添加 "-v" 参数
    - 转义主机路径
    - 格式: -v 'hostPath':containerPath:mode
    - _需求: 4.5_

  - [ ] 6.6 实现资源限制参数构建
    - 添加 "--cpus" 参数（如果配置）
    - 添加 "--memory" 参数（如果配置）
    - 强制设置默认限制（CPU: 2核, 内存: 2GB）
    - 验证不超过最大限制（CPU: 4核, 内存: 4GB）
    - _需求: 4.6, 10.4_

  - [ ] 6.7 实现其他参数构建
    - 添加 "--restart" 参数（默认: unless-stopped）
    - 添加 "--network" 参数（如果配置）
    - _需求: 4.7_

  - [ ] 6.8 实现 Shell 参数转义方法
    - 实现 escapeShellArg(String arg) 静态方法
    - 移除危险字符（;&|`$(){}[]<>）
    - 转义单引号为 '\''
    - 用单引号包裹参数值
    - _需求: 10.1, 10.5, 10.6_

  - [ ]* 6.9 编写命令构建单元测试
    - 测试基本配置生成正确命令
    - 测试端口映射格式
    - 测试环境变量转义
    - 测试特殊字符转义
    - 测试资源限制

- [ ] 7. 实现镜像拉取服务
  - [ ] 7.1 创建 DockerImageService 接口和实现类
    - 在 com.ruoyi.web.service 创建 IDockerImageService 接口
    - 在 com.ruoyi.web.service.impl 创建 DockerImageServiceImpl
    - 注入 DockerImageMapper 和 DeployServiceImpl
    - _需求: 3.1, 12.2, 12.3_

  - [ ] 7.2 实现镜像存在性检查
    - 实现 checkImageExists(Session, String, String) 方法
    - 先查询数据库缓存表
    - 执行 "docker images -q <image>:<tag>" 命令
    - 返回镜像是否存在
    - _需求: 3.1, 3.2, 12.2_

  - [ ] 7.3 实现镜像拉取方法
    - 实现 pullImageWithProgress(Session, String, String, Long) 方法
    - 如果镜像已存在，跳过拉取并推送日志
    - 执行 "docker pull <image>:<tag>" 命令
    - _需求: 3.2, 3.3_

  - [ ] 7.4 实现拉取进度实时监控
    - 打开 SSH exec channel 执行拉取命令
    - 实时读取 InputStream 输出
    - 解析进度信息（Pulling, Downloading, Extracting, Pull complete）
    - 通过 WebSocket 推送进度日志
    - 避免推送重复的进度行
    - _需求: 3.4, 3.5, 12.4_

  - [ ] 7.5 实现拉取失败处理和重试
    - 检查 channel 退出状态码
    - 如果失败，记录错误并自动重试（最多3次）
    - 推送失败日志
    - _需求: 3.6, 11.5, 11.6_

  - [ ] 7.6 实现镜像缓存更新
    - 拉取成功后更新 t_docker_image 表
    - 记录拉取时间和最后使用时间
    - _需求: 3.8, 15.8, 15.9_

  - [ ]* 7.7 编写镜像拉取单元测试
    - Mock SSH channel 和输出流
    - 测试镜像已存在场景
    - 测试拉取成功场景
    - 测试拉取失败和重试

- [ ] 8. 实现容器部署核心服务
  - [ ] 8.1 创建 DockerDeployService 接口和实现类
    - 在 com.ruoyi.web.service 创建 IDockerDeployService 接口
    - 在 com.ruoyi.web.service.impl 创建 DockerDeployServiceImpl
    - 注入所有依赖服务（Env, Validation, Image, Monitor）
    - _需求: 13.3, 13.4_

  - [ ] 8.2 实现部署主流程方法
    - 实现 deployContainer(DockerDeployConfig) 方法
    - 创建部署日志记录并获取 logId
    - 获取 SSH 会话
    - 在 try-catch-finally 块中执行部署流程
    - _需求: 6.1, 11.1, 11.8_

  - [ ] 8.3 实现环境检测步骤
    - 调用 DockerEnvService.checkDockerEnvironment()
    - 推送日志: "开始检测Docker环境..."
    - 验证 dockerInstalled 和 dockerRunning
    - 如果不可用，抛出异常
    - 推送日志: "Docker环境检测通过"
    - _需求: 1.1-1.8, 6.2, 6.3_

  - [ ] 8.4 实现部署前验证步骤
    - 调用 validateContainerName() 验证容器名
    - 调用 checkContainerNameExists() 检查名称冲突
    - 遍历端口调用 checkPortInUse() 检查端口冲突
    - 调用 validateVolumeMounts() 验证卷挂载安全
    - 推送相应的日志消息
    - _需求: 2.1-2.8, 6.3_

  - [ ] 8.5 实现镜像拉取步骤
    - 推送日志: "开始拉取镜像: <image>:<tag>"
    - 调用 pullImageWithProgress() 拉取镜像
    - 如果失败，抛出异常
    - 推送日志: "镜像拉取完成"
    - _需求: 3.1-3.8, 6.3_

  - [ ] 8.6 实现容器创建和启动步骤
    - 推送日志: "构建容器启动命令..."
    - 调用 DockerCommandBuilder.buildDockerRunCommand()
    - 推送日志: "执行命令: <command>"
    - 执行命令并获取容器 ID
    - 验证容器 ID 有效性（64位十六进制）
    - 推送日志: "容器创建成功: <containerId前12位>"
    - _需求: 4.1-4.10, 6.3_

  - [ ] 8.7 实现健康检查步骤
    - 推送日志: "执行健康检查..."
    - 调用 DockerMonitorService.performHealthCheck()
    - 如果未通过，推送警告但不回滚
    - 推送日志: "健康检查通过" 或警告消息
    - _需求: 5.1-5.10, 6.3_

  - [ ] 8.8 实现容器记录保存
    - 创建 DockerContainer 实体对象
    - 设置所有字段（容器ID、配置、日志ID等）
    - 将端口、环境变量、卷挂载序列化为 JSON
    - 调用 DockerContainerMapper.insert() 保存
    - _需求: 15.1-15.5_

  - [ ] 8.9 实现部署成功处理
    - 设置 result.success = true
    - 推送日志: "部署完成"
    - 更新部署日志状态为 SUCCESS
    - 返回部署结果
    - _需求: 6.4, 6.8_

  - [ ] 8.10 实现部署失败回滚
    - 在 catch 块中捕获所有异常
    - 推送日志: "部署失败: <错误消息>"
    - 如果容器已创建，尝试删除容器
    - 推送清理日志
    - 更新部署日志状态为 FAILED
    - 设置 result.success = false
    - _需求: 11.1-11.4, 11.9_

  - [ ] 8.11 实现 SSH 连接清理
    - 在 finally 块中关闭 SSH 会话
    - 确保无论成功或失败都关闭连接
    - _需求: 11.8, 13.9_

  - [ ]* 8.12 编写部署服务集成测试
    - Mock 完整部署流程
    - 测试成功部署场景
    - 测试各种失败场景
    - 验证回滚逻辑

- [ ] 9. Checkpoint - 核心部署功能验证
  - 确保所有测试通过，手动测试基本部署流程，询问用户是否有问题

