# 网络环境部署指南

如果要在公网/互联网环境下实现扫码登录，需要满足以下架构要求：

## 1. 总体架构

*   **服务端 (Backend)**: 部署在公网服务器（如阿里云、AWS），拥有公网 IP 或域名，并开启 HTTPS。
*   **Web 端 (PC)**: 部署在 Web 服务器（Nginx），通过 HTTPS 访问后端 API。
*   **APP 端 (Mobile)**: 安装在用户手机，通过 4G/5G/Wi-Fi 访问后端的公网 HTTPS 接口。

## 2. APP 端配置 (已更新)

为了支持灵活切换环境，APP 代码已更新，允许在登录界面动态设置服务器地址。

1.  打开 APP，在登录界面顶部会看到 **"Server URL"** 输入框。
2.  输入你的公网地址，例如：`https://api.yourdomain.com` 或 `http://1.2.3.4:8080`。
3.  点击 Login，APP 会记住这个地址，后续扫码也会使用该地址。

## 3. 服务端部署要求

### 3.1 开放端口
确保服务器防火墙（安全组）开放了 API 端口（如 8080）。

### 3.2 域名与 SSL (强烈推荐)
在公网环境下，为了安全（特别是防止密码泄露）和兼容性（Android 高版本默认禁止 HTTP 明文传输），强烈建议配置 HTTPS。

**Nginx 配置示例**:
```nginx
server {
    listen 443 ssl;
    server_name api.yourdomain.com;

    ssl_certificate /path/to/cert.pem;
    ssl_certificate_key /path/to/key.pem;

    location / {
        proxy_pass http://localhost:8080; # 转发到后端
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

## 4. 常见网络问题排查

1.  **连不上服务器**:
    *   检查手机是否开启了网络。
    *   检查服务器安全组是否开放端口。
    *   尝试在手机浏览器直接访问 `https://api.yourdomain.com`，看是否能显示后端提示或 404。

2.  **Cleartext HTTP traffic not permitted**:
    *   这是 Android 的安全限制，禁止明文 HTTP。
    *   **解决方法 1 (推荐)**: 部署 HTTPS。
    *   **解决方法 2 (测试用)**: APP 已经配置了 `android:usesCleartextTraffic="true"`，所以支持 HTTP，但要注意运营商可能会劫持 HTTP 流量。
