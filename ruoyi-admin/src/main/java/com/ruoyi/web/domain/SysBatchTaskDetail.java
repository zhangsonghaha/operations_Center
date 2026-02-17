package com.ruoyi.web.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 批量任务明细对象 sys_batch_task_detail
 */
public class SysBatchTaskDetail extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 明细ID */
    private Long detailId;

    /** 任务ID */
    private Long taskId;

    /** 服务器ID */
    private Long serverId;

    /** 服务器名称快照 */
    private String serverName;

    /** 服务器IP快照 */
    private String serverIp;

    /** 执行状态（0等待 1执行中 2成功 3失败） */
    private String status;

    /** 退出码 */
    private Integer exitCode;

    /** 执行日志 */
    private String executionLog;

    /** 开始时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    /** 结束时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;

    public void setDetailId(Long detailId) 
    {
        this.detailId = detailId;
    }

    public Long getDetailId() 
    {
        return detailId;
    }
    public void setTaskId(Long taskId) 
    {
        this.taskId = taskId;
    }

    public Long getTaskId() 
    {
        return taskId;
    }
    public void setServerId(Long serverId) 
    {
        this.serverId = serverId;
    }

    public Long getServerId() 
    {
        return serverId;
    }
    public void setServerName(String serverName) 
    {
        this.serverName = serverName;
    }

    public String getServerName() 
    {
        return serverName;
    }
    public void setServerIp(String serverIp) 
    {
        this.serverIp = serverIp;
    }

    public String getServerIp() 
    {
        return serverIp;
    }
    public void setStatus(String status) 
    {
        this.status = status;
    }

    public String getStatus() 
    {
        return status;
    }
    public void setExitCode(Integer exitCode) 
    {
        this.exitCode = exitCode;
    }

    public Integer getExitCode() 
    {
        return exitCode;
    }
    public void setExecutionLog(String executionLog) 
    {
        this.executionLog = executionLog;
    }

    public String getExecutionLog() 
    {
        return executionLog;
    }
    public void setStartTime(Date startTime) 
    {
        this.startTime = startTime;
    }

    public Date getStartTime() 
    {
        return startTime;
    }
    public void setEndTime(Date endTime) 
    {
        this.endTime = endTime;
    }

    public Date getEndTime() 
    {
        return endTime;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("detailId", getDetailId())
            .append("taskId", getTaskId())
            .append("serverId", getServerId())
            .append("serverName", getServerName())
            .append("serverIp", getServerIp())
            .append("status", getStatus())
            .append("exitCode", getExitCode())
            .append("executionLog", getExecutionLog())
            .append("startTime", getStartTime())
            .append("endTime", getEndTime())
            .toString();
    }
}
