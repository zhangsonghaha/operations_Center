package com.ruoyi.web.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 监控日志对象 t_ops_monitor_log
 */
public class OpsMonitorLog extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 日志ID */
    private Long logId;

    /** CPU负载 */
    private Double cpuLoad;

    /** 内存负载 */
    private Double memoryLoad;

    /** 记录时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date recordTime;

    public void setLogId(Long logId) 
    {
        this.logId = logId;
    }

    public Long getLogId() 
    {
        return logId;
    }
    public void setCpuLoad(Double cpuLoad) 
    {
        this.cpuLoad = cpuLoad;
    }

    public Double getCpuLoad() 
    {
        return cpuLoad;
    }
    public void setMemoryLoad(Double memoryLoad) 
    {
        this.memoryLoad = memoryLoad;
    }

    public Double getMemoryLoad() 
    {
        return memoryLoad;
    }
    public void setRecordTime(Date recordTime) 
    {
        this.recordTime = recordTime;
    }

    public Date getRecordTime() 
    {
        return recordTime;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("logId", getLogId())
            .append("cpuLoad", getCpuLoad())
            .append("memoryLoad", getMemoryLoad())
            .append("recordTime", getRecordTime())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .toString();
    }
}
