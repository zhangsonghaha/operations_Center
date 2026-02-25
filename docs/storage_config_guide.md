# 数据库存储备份存储配置指南

## 概述

本系统支持多种存储类型，需要在【系统管理-参数配置】中配置对应的存储参数。

## 存储类型配置

### 1. 本地存储 (local)

**无需额外配置**，备份文件存储在应用服务器的 `ruoyi.profile/backup` 目录下。

默认配置位置：`application.yml` 中的 `ruoyi.profile` 参数。

---

### 2. FTP服务器 (ftp)

需要在参数配置中添加以下参数：

| 参数键 | 参数值示例 | 说明 |
|--------|-----------|------|
| `storage.ftp.host` | `192.168.1.100` | FTP服务器地址 |
| `storage.ftp.port` | `21` | FTP端口，默认21 |
| `storage.ftp.username` | `ftpuser` | FTP用户名 |
| `storage.ftp.password` | `ftppass` | FTP密码 |
| `storage.ftp.basePath` | `/backups/db` | 远程基础路径 |
| `storage.ftp.passiveMode` | `true` | 是否使用被动模式 |

---

### 3. SFTP服务器 (sftp)

需要在参数配置中添加以下参数：

| 参数键 | 参数值示例 | 说明 |
|--------|-----------|------|
| `storage.sftp.host` | `192.168.1.100` | SFTP服务器地址 |
| `storage.sftp.port` | `22` | SSH端口，默认22 |
| `storage.sftp.username` | `sftpuser` | SFTP用户名 |
| `storage.sftp.password` | `sftppass` | SFTP密码（或私钥） |
| `storage.sftp.basePath` | `/backups/db` | 远程基础路径 |
| `storage.sftp.privateKey` | `/path/to/key` | 私钥路径（可选，使用密钥认证时） |

---

### 4. 阿里云OSS (aliyun_oss)

需要在参数配置中添加以下参数：

| 参数键 | 参数值示例 | 说明 |
|--------|-----------|------|
| `storage.oss.endpoint` | `oss-cn-beijing.aliyuncs.com` | OSS访问域名 |
| `storage.oss.accessKeyId` | `LTAIxxxxx` | AccessKey ID |
| `storage.oss.accessKeySecret` | `xxxxx` | AccessKey Secret |
| `storage.oss.bucketName` | `my-backup-bucket` | Bucket名称 |
| `storage.oss.basePath` | `db-backups/` | 对象存储前缀路径 |

**安全提示**：建议创建RAM子账号，仅授予OSS读写权限，不要使用主账号AccessKey。

---

### 5. 腾讯云COS (tencent_cos)

需要在参数配置中添加以下参数：

| 参数键 | 参数值示例 | 说明 |
|--------|-----------|------|
| `storage.cos.region` | `ap-beijing` | 存储桶地域 |
| `storage.cos.secretId` | `AKIDxxxxx` | SecretId |
| `storage.cos.secretKey` | `xxxxx` | SecretKey |
| `storage.cos.bucketName` | `my-backup-bucket-1250000000` | Bucket名称 |
| `storage.cos.basePath` | `db-backups/` | 对象存储前缀路径 |

**安全提示**：建议使用子账号密钥，仅授予COS读写权限。

---

## 配置步骤

1. 登录系统，进入【系统管理】-【参数配置】
2. 点击【新增参数】按钮
3. 填写参数键和参数值（参考上表）
4. 参数类型选择"系统内置"
5. 保存配置

## 备份流程中的存储处理

### 本地存储
- 备份文件直接写入本地磁盘
- 下载时通过 `/download/resource` 接口提供

### 远程存储 (FTP/SFTP)
- 先在本地生成备份文件
- 上传完成后删除本地临时文件
- 下载时从远程服务器获取

### 云存储 (OSS/COS)
- 先在本地生成备份文件
- 上传至云存储后删除本地文件
- 下载时生成临时预签名URL

## 安全性建议

1. **密钥管理**：不要将密钥硬编码在代码中，使用参数配置
2. **网络隔离**：生产环境建议使用内网地址访问存储服务
3. **加密传输**：确保使用HTTPS/SSL等加密传输方式
4. **访问控制**：限制备份文件的访问权限，避免未授权访问
5. **定期轮换**：定期更换存储服务的访问密钥

## 故障排查

### 备份失败

1. 检查参数配置是否正确
2. 检查网络连通性（ping/telnet）
3. 查看应用日志中的详细错误信息
4. 确认存储服务账号权限是否足够

### 下载失败

1. 检查备份文件是否存在于目标存储
2. 检查存储服务的访问权限
3. 对于云存储，检查预签名URL是否过期

## 扩展开发

如需添加新的存储类型，需要：

1. 实现 `IStorageProvider` 接口
2. 在 `StorageProviderFactory` 中注册
3. 添加对应的配置参数
4. 更新前端存储类型选项
