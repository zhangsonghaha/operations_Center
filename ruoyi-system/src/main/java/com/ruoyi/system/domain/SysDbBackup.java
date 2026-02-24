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
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .toString();
    }
}
