package com.ruoyi.web.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 进程信息对象
 */
public class OpsProcess extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 进程PID */
    private String pid;

    /** 进程名称 */
    private String name;

    /** CPU使用率 */
    private String cpuUsage;

    /** 内存使用率 */
    private String memoryUsage;

    /** 启动时间 */
    private String startTime;

    /** 运行状态 */
    private String status;

    /** 所属服务器ID */
    private Long serverId;

    public void setPid(String pid) 
    {
        this.pid = pid;
    }

    public String getPid() 
    {
        return pid;
    }
    public void setName(String name) 
    {
        this.name = name;
    }

    public String getName() 
    {
        return name;
    }
    public void setCpuUsage(String cpuUsage) 
    {
        this.cpuUsage = cpuUsage;
    }

    public String getCpuUsage() 
    {
        return cpuUsage;
    }
    public void setMemoryUsage(String memoryUsage) 
    {
        this.memoryUsage = memoryUsage;
    }

    public String getMemoryUsage() 
    {
        return memoryUsage;
    }
    public void setStartTime(String startTime) 
    {
        this.startTime = startTime;
    }

    public String getStartTime() 
    {
        return startTime;
    }
    public void setStatus(String status) 
    {
        this.status = status;
    }

    public String getStatus() 
    {
        return status;
    }
    public void setServerId(Long serverId) 
    {
        this.serverId = serverId;
    }

    public Long getServerId() 
    {
        return serverId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("pid", getPid())
            .append("name", getName())
            .append("cpuUsage", getCpuUsage())
            .append("memoryUsage", getMemoryUsage())
            .append("startTime", getStartTime())
            .append("status", getStatus())
            .append("serverId", getServerId())
            .toString();
    }
}
