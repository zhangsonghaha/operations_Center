package com.ruoyi.system.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 数据库操作日志对象 sys_db_log
 * 
 * @author ruoyi
 */
public class SysDbLog extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 日志ID */
    private Long logId;

    /** 连接ID */
    @Excel(name = "连接ID")
    private Long connId;

    /** 执行语句 */
    @Excel(name = "执行语句")
    private String sqlContent;

    /** 耗时(ms) */
    @Excel(name = "耗时(ms)")
    private Long costTime;

    /** 状态（0成功 1失败） */
    @Excel(name = "状态", readConverterExp = "0=成功,1=失败")
    private String status;

    /** 错误信息 */
    @Excel(name = "错误信息")
    private String errorMsg;

    public void setLogId(Long logId) 
    {
        this.logId = logId;
    }

    public Long getLogId() 
    {
        return logId;
    }
    public void setConnId(Long connId) 
    {
        this.connId = connId;
    }

    public Long getConnId() 
    {
        return connId;
    }
    public void setSqlContent(String sqlContent) 
    {
        this.sqlContent = sqlContent;
    }

    public String getSqlContent() 
    {
        return sqlContent;
    }
    public void setCostTime(Long costTime) 
    {
        this.costTime = costTime;
    }

    public Long getCostTime() 
    {
        return costTime;
    }
    public void setStatus(String status) 
    {
        this.status = status;
    }

    public String getStatus() 
    {
        return status;
    }
    public void setErrorMsg(String errorMsg) 
    {
        this.errorMsg = errorMsg;
    }

    public String getErrorMsg() 
    {
        return errorMsg;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("logId", getLogId())
            .append("connId", getConnId())
            .append("sqlContent", getSqlContent())
            .append("costTime", getCostTime())
            .append("status", getStatus())
            .append("errorMsg", getErrorMsg())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .toString();
    }
}
