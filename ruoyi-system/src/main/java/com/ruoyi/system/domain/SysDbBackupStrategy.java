package com.ruoyi.system.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 数据库备份策略对象 sys_db_backup_strategy
 * 
 * @author ruoyi
 */
public class SysDbBackupStrategy extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 策略ID */
    private Long strategyId;

    /** 策略名称 */
    @Excel(name = "策略名称")
    private String strategyName;

    /** 连接ID */
    @Excel(name = "连接ID")
    private Long connId;

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

    /** Cron表达式 */
    private String cronExpression;

    /** 状态（0启用 1停用） */
    @Excel(name = "状态", readConverterExp = "0=启用,1=停用")
    private String enabled;

    /** 保留天数 */
    private Integer retentionDays;

    /** 保留数量 */
    private Integer retentionCount;

    /** 是否压缩 */
    private String compressEnabled;

    /** 压缩类型 */
    private String compressType;

    /** 是否加密 */
    private String encryptEnabled;

    /** 存储类型 */
    private String storageType;

    /** 存储配置 */
    private String storageConfig;

    /** 是否告警 */
    private String alertEnabled;

    /** 告警配置 */
    private String alertConfig;

    /** 备注 */
    private String remark;

    public Long getStrategyId() {
        return strategyId;
    }

    public void setStrategyId(Long strategyId) {
        this.strategyId = strategyId;
    }

    public String getStrategyName() {
        return strategyName;
    }

    public void setStrategyName(String strategyName) {
        this.strategyName = strategyName;
    }

    public Long getConnId() {
        return connId;
    }

    public void setConnId(Long connId) {
        this.connId = connId;
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

    public String getCronExpression() {
        return cronExpression;
    }

    public void setCronExpression(String cronExpression) {
        this.cronExpression = cronExpression;
    }

    public String getEnabled() {
        return enabled;
    }

    public void setEnabled(String enabled) {
        this.enabled = enabled;
    }

    public Integer getRetentionDays() {
        return retentionDays;
    }

    public void setRetentionDays(Integer retentionDays) {
        this.retentionDays = retentionDays;
    }

    public Integer getRetentionCount() {
        return retentionCount;
    }

    public void setRetentionCount(Integer retentionCount) {
        this.retentionCount = retentionCount;
    }

    public String getCompressEnabled() {
        return compressEnabled;
    }

    public void setCompressEnabled(String compressEnabled) {
        this.compressEnabled = compressEnabled;
    }

    public String getCompressType() {
        return compressType;
    }

    public void setCompressType(String compressType) {
        this.compressType = compressType;
    }

    public String getEncryptEnabled() {
        return encryptEnabled;
    }

    public void setEncryptEnabled(String encryptEnabled) {
        this.encryptEnabled = encryptEnabled;
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

    public String getAlertEnabled() {
        return alertEnabled;
    }

    public void setAlertEnabled(String alertEnabled) {
        this.alertEnabled = alertEnabled;
    }

    public String getAlertConfig() {
        return alertConfig;
    }

    public void setAlertConfig(String alertConfig) {
        this.alertConfig = alertConfig;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("strategyId", getStrategyId())
            .append("strategyName", getStrategyName())
            .append("connId", getConnId())
            .append("dbType", getDbType())
            .append("backupMode", getBackupMode())
            .append("backupLevel", getBackupLevel())
            .append("cronExpression", getCronExpression())
            .append("enabled", getEnabled())
            .append("retentionDays", getRetentionDays())
            .append("retentionCount", getRetentionCount())
            .append("compressEnabled", getCompressEnabled())
            .append("storageType", getStorageType())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .toString();
    }
}
