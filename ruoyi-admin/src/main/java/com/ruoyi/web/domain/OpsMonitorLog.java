package com.ruoyi.web.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 服务器监控日志对象 sys_ops_monitor_log
 */
public class OpsMonitorLog extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** ID */
    private Long logId;

    /** 服务器ID */
    private Long serverId;

    /** CPU使用率(%) */
    private BigDecimal cpuUsage;

    /** 内存使用率(%) */
    private BigDecimal memoryUsage;

    /** 磁盘使用率(%) */
    private BigDecimal diskUsage;

    /** 网络发送速率(KB/s) */
    private BigDecimal netTxRate;

    /** 网络接收速率(KB/s) */
    private BigDecimal netRxRate;
    
    public void setLogId(Long logId) 
    {
        this.logId = logId;
    }

    public Long getLogId() 
    {
        return logId;
    }
    public void setServerId(Long serverId) 
    {
        this.serverId = serverId;
    }

    public Long getServerId() 
    {
        return serverId;
    }
    public void setCpuUsage(BigDecimal cpuUsage) 
    {
        this.cpuUsage = cpuUsage;
    }

    public BigDecimal getCpuUsage() 
    {
        return cpuUsage;
    }
    public void setMemoryUsage(BigDecimal memoryUsage) 
    {
        this.memoryUsage = memoryUsage;
    }

    public BigDecimal getMemoryUsage() 
    {
        return memoryUsage;
    }
    public void setDiskUsage(BigDecimal diskUsage) 
    {
        this.diskUsage = diskUsage;
    }

    public BigDecimal getDiskUsage() 
    {
        return diskUsage;
    }
    public void setNetTxRate(BigDecimal netTxRate) 
    {
        this.netTxRate = netTxRate;
    }

    public BigDecimal getNetTxRate() 
    {
        return netTxRate;
    }
    public void setNetRxRate(BigDecimal netRxRate) 
    {
        this.netRxRate = netRxRate;
    }

    public BigDecimal getNetRxRate() 
    {
        return netRxRate;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("logId", getLogId())
            .append("serverId", getServerId())
            .append("cpuUsage", getCpuUsage())
            .append("memoryUsage", getMemoryUsage())
            .append("diskUsage", getDiskUsage())
            .append("netTxRate", getNetTxRate())
            .append("netRxRate", getNetRxRate())
            .append("createTime", getCreateTime())
            .toString();
    }
}
