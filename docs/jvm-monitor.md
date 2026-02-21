# JVM 监控配置说明

## 1. 常见报错及原因

### Connection refused to host: 127.0.0.1
**现象**：
后台报错 `Caused by: java.rmi.ConnectException: Connection refused to host: 127.0.0.1`

**原因**：
监控服务尝试通过 JMX 连接目标 JVM，但目标 JVM 未开启 JMX 服务或端口未监听。

## 2. 配置步骤

要启用 JVM 监控，需要在被监控的 Java 应用启动参数中添加 JMX 配置。

### 2.1 添加启动参数 (VM Options)

无论是在本地 IDEA 开发环境，还是服务器部署环境，都需要添加以下参数：

```bash
-Dcom.sun.management.jmxremote
-Dcom.sun.management.jmxremote.port=1099
-Dcom.sun.management.jmxremote.authenticate=false
-Dcom.sun.management.jmxremote.ssl=false
```

*   `port=1099`: JMX 监听端口，需与监控后台配置一致。
*   `authenticate=false`: 关闭身份验证（开发环境推荐）。如需开启，请参考 JDK 文档配置 `jmxremote.password` 文件。
*   `ssl=false`: 关闭 SSL 加密。

### 2.2 IDEA 配置方法

1.  点击右上角启动配置 -> **Edit Configurations**。
2.  选择你的 Spring Boot 应用。
3.  点击 **Modify options** -> **Add VM options**。
4.  在文本框中粘贴上述参数。
5.  重启应用。

## 3. 验证

1.  启动应用后，检查端口 `1099` 是否开启：`netstat -ano | findstr 1099` (Windows) 或 `lsof -i:1099` (Linux)。
2.  进入系统 -> JVM 监控。
3.  确保配置的目标主机为 `127.0.0.1`，端口为 `1099`。
4.  点击“监控”按钮，应能正常显示数据。
