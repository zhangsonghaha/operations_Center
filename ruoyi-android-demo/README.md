# RuoYi-Android-Demo 使用文档

本项目是基于 Android (Kotlin) 实现的扫码登录示例应用，配合 RuoYi-Vue 后端实现 PC 端扫码登录功能。

## 1. 环境准备

*   **Android Studio**: 推荐使用 Hedgehog 或更高版本。
*   **JDK**: JDK 11 或 JDK 17 (Android Studio 内置)。
*   **Android SDK**: 推荐 API Level 33 (Android 13) 或更高。

## 2. 项目导入

1.  打开 Android Studio。
2.  选择 **File -> Open**。
3.  导航到 `E:\vue_package\RuoYi-Vue\ruoyi-android-demo` 目录并选择。
4.  等待 Gradle Sync 完成（初次加载可能需要下载依赖，请保持网络通畅）。

## 3. 配置服务器地址

由于 Android 模拟器和真机的网络环境不同，你需要根据运行环境修改 API 地址。

打开文件：`app/src/main/java/com/ruoyi/mobile/LoginActivity.kt` 和 `QrScanActivity.kt`

### 情况 A：使用 Android 模拟器
如果你的后端服务运行在开发机本机 (localhost:8080)，请使用 Android 模拟器专用地址 `10.0.2.2`：

```kotlin
.baseUrl("http://10.0.2.2:8080")
```

### 情况 B：使用真机调试
如果使用真实手机，请确保手机和电脑连接在同一局域网（Wi-Fi）。
1.  查找电脑的局域网 IP (例如 `192.168.1.100`)。
2.  修改代码中的 URL：

```kotlin
.baseUrl("http://192.168.1.100:8080")
```

## 4. 运行与使用

### 第一步：启动应用
点击 Android Studio 顶部的 **Run 'app'** 按钮 (绿色三角形)。

### 第二步：APP 登录
1.  应用启动后进入登录页。
2.  输入用户名和密码 (例如 `admin` / `admin123`)。
3.  点击 **Login**。
4.  登录成功后进入主页 (Home)，显示 "Welcome, admin"。

### 第三步：扫码登录 PC 端
1.  在 PC 浏览器打开若依管理系统登录页，切换到 **“扫码登录”** -> **“APP 扫码”**。
2.  在手机 APP 主页点击 **“Scan QR Code to Login”**。
3.  授予相机权限（如果是第一次使用）。
4.  将手机摄像头对准 PC 屏幕上的二维码。
5.  **扫码成功**：PC 端二维码状态会变为“扫码成功，请在手机上确认”。
6.  **确认登录**：手机端弹出确认对话框，点击 **“Confirm”**。
7.  **完成**：PC 端自动跳转进入系统。

## 5. 常见问题

**Q: 扫码后提示 "Scan Failed" 或 "Network Error"?**
A: 请检查以下几点：
1.  **网络连接**: 手机能否访问电脑 IP？（尝试在手机浏览器输入 `http://电脑IP:8080` 看能否打开）。
2.  **防火墙**: 电脑防火墙是否允许 8080 端口入站连接。
3.  **Base URL**: 确认代码中的 IP 地址是否正确。

**Q: 点击 Confirm 后 PC 端没有反应？**
A: 检查后端 Redis 服务是否正常，PC 端是否在轮询状态（二维码是否过期）。

**Q: Gradle Sync 失败？**
A: 请检查网络连接，确保能访问 Google Maven 仓库，或者配置阿里云镜像源。
