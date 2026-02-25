package com.ruoyi.system.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 数据库备份记录对象 sys_db_backup
 * 
 * @author ruoyi
 */
public class SysDbBackup extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 备份ID */
    private Long backupId;

    /** 连接ID */
    @Excel(name = "连接ID")
    private Long connId;

    /** 备份文件名 */
    @Excel(name = "备份文件名")
    private String fileName;

    /** 文件存储路径 */
    @Excel(name = "文件存储路径")
    private String filePath;

    /** 备份类型（0手动 1自动） */
    @Excel(name = "备份类型", readConverterExp = "0=手动,1=自动")
    private String backupType;

    /** 状态（0成功 1失败） */
    @Excel(name = "状态", readConverterExp = "0=成功,1=失败")
    private String status;

    /** 日志信息 */
    @Excel(name = "日志信息")
    private String logMsg;

    /** ============ 扩展字段 ============ */
    
    /** 数据库类型 */
    @Excel(name = "数据库类型")
    private String dbType;

    /** 备份方式 */
    @Excel(name = "备份方式", readConverterExp = "full=全量,incremental=增量,differential=差异")
    private String backupMode;

    /** 备份级别 */
    @Excel(name = "备份级别", readConverterExp = "instance=实例级,database=数据库级,table=表级")
    private String backupLevel;

    /** 备份目标 */
    private String targetName;

    /** 文件大小 */
    private Long fileSize;

    /** 文件MD5校验值 */
    private String fileMd5;

    /** 存储类型 */
    private String storageType;

    /** 存储配置 */
    private String storageConfig;

    /** 验证状态（0未验证 1验证成功 2验证失败） */
    private String verifyStatus;

    /** 验证信息 */
    private String verifyMsg;

    /** 是否压缩 */
    private String compressEnabled;

    /** 过期时间 */
    private java.util.Date expireTime;

    /** 是否删除（0正常 1已删除） */
    private String isDeleted;

    public void setBackupId(Long backupId) 
    {
        this.backupId = backupId;
    }

    public Long getBackupId() 
    {
        return backupId;
    }
    public void setConnId(Long connId) 
    {
        this.connId = connId;
    }

    public Long getConnId() 
    {
        return connId;
    }
    public void setFileName(String fileName) 
    {
        this.fileName = fileName;
    }

    public String getFileName() 
    {
        return fileName;
    }
    public void setFilePath(String filePath) 
    {
        this.filePath = filePath;
    }

    public String getFilePath() 
    {
        return filePath;
    }
    public void setBackupType(String backupType) 
    {
        this.backupType = backupType;
    }

    public String getBackupType() 
    {
        return backupType;
    }
    public void setStatus(String status) 
    {
        this.status = status;
    }

    public String getStatus() 
    {
        return status;
    }
    public void setLogMsg(String logMsg) 
    {
        this.logMsg = logMsg;
    }

    public String getLogMsg() 
    {
        return logMsg;
    }

    public String getDbType() {
        return dbType;
    }

    public void setDbType(String dbType) {
        this.dbType = dbType;
    }

    public String getBackupMode() {
        return backupMode;
    }

    public void setBackupMode(String backupMode) {
        this.backupMode = backupMode;
    }

    public String getBackupLevel() {
        return backupLevel;
    }

    public void setBackupLevel(String backupLevel) {
        this.backupLevel = backupLevel;
    }

    public String getTargetName() {
        return targetName;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileMd5() {
        return fileMd5;
    }

    public void setFileMd5(String fileMd5) {
        this.fileMd5 = fileMd5;
    }

    public String getStorageType() {
        return storageType;
    }

    public void setStorageType(String storageType) {
        this.storageType = storageType;
    }

    public String getStorageConfig() {
        return storageConfig;
    }

    public void setStorageConfig(String storageConfig) {
        this.storageConfig = storageConfig;
    }

    public String getVerifyStatus() {
        return verifyStatus;
    }

    public void setVerifyStatus(String verifyStatus) {
        this.verifyStatus = verifyStatus;
    }

    public String getVerifyMsg() {
        return verifyMsg;
    }

    public void setVerifyMsg(String verifyMsg) {
        this.verifyMsg = verifyMsg;
    }

    public String getCompressEnabled() {
        return compressEnabled;
    }

    public void setCompressEnabled(String compressEnabled) {
        this.compressEnabled = compressEnabled;
    }

    public java.util.Date getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(java.util.Date expireTime) {
        this.expireTime = expireTime;
    }

    public String getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(String isDeleted) {
        this.isDeleted = isDeleted;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("backupId", getBackupId())
            .append("connId", getConnId())
            .append("fileName", getFileName())
            .append("filePath", getFilePath())
            .append("backupType", getBackupType())
            .append("status", getStatus())
            .append("logMsg", getLogMsg())
            .append("dbType", getDbType())
            .append("backupMode", getBackupMode())
            .append("backupLevel", getBackupLevel())
            .append("targetName", getTargetName())
            .append("fileSize", getFileSize())
            .append("fileMd5", getFileMd5())
            .append("storageType", getStorageType())
            .append("verifyStatus", getVerifyStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .toString();
    }
}
