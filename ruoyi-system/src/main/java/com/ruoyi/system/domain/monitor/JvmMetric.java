package com.ruoyi.system.domain.monitor;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * JVM 监控指标对象 sys_jvm_metric
 * 
 * @author ruoyi
 */
public class JvmMetric extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 指标ID */
    private Long metricId;

    /** 目标ID */
    private Long targetId;

    /** 堆内存使用量(bytes) */
    private Long heapUsed;

    /** 堆内存最大值(bytes) */
    private Long heapMax;

    /** 非堆内存使用量(bytes) */
    private Long nonHeapUsed;

    /** 非堆内存最大值(bytes) */
    private Long nonHeapMax;

    /** 当前活跃线程数 */
    private Integer threadActive;

    /** 峰值线程数 */
    private Integer threadPeak;

    /** GC总次数 */
    private Long gcCount;

    /** GC总时间(ms) */
    private Long gcTime;

    /** 采集时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    public void setMetricId(Long metricId) 
    {
        this.metricId = metricId;
    }

    public Long getMetricId() 
    {
        return metricId;
    }
    public void setTargetId(Long targetId) 
    {
        this.targetId = targetId;
    }

    public Long getTargetId() 
    {
        return targetId;
    }
    public void setHeapUsed(Long heapUsed) 
    {
        this.heapUsed = heapUsed;
    }

    public Long getHeapUsed() 
    {
        return heapUsed;
    }
    public void setHeapMax(Long heapMax) 
    {
        this.heapMax = heapMax;
    }

    public Long getHeapMax() 
    {
        return heapMax;
    }
    public void setNonHeapUsed(Long nonHeapUsed) 
    {
        this.nonHeapUsed = nonHeapUsed;
    }

    public Long getNonHeapUsed() 
    {
        return nonHeapUsed;
    }
    public void setNonHeapMax(Long nonHeapMax) 
    {
        this.nonHeapMax = nonHeapMax;
    }

    public Long getNonHeapMax() 
    {
        return nonHeapMax;
    }
    public void setThreadActive(Integer threadActive) 
    {
        this.threadActive = threadActive;
    }

    public Integer getThreadActive() 
    {
        return threadActive;
    }
    public void setThreadPeak(Integer threadPeak) 
    {
        this.threadPeak = threadPeak;
    }

    public Integer getThreadPeak() 
    {
        return threadPeak;
    }
    public void setGcCount(Long gcCount) 
    {
        this.gcCount = gcCount;
    }

    public Long getGcCount() 
    {
        return gcCount;
    }
    public void setGcTime(Long gcTime) 
    {
        this.gcTime = gcTime;
    }

    public Long getGcTime() 
    {
        return gcTime;
    }
    public void setCreateTime(Date createTime) 
    {
        this.createTime = createTime;
    }

    public Date getCreateTime() 
    {
        return createTime;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("metricId", getMetricId())
            .append("targetId", getTargetId())
            .append("heapUsed", getHeapUsed())
            .append("heapMax", getHeapMax())
            .append("nonHeapUsed", getNonHeapUsed())
            .append("nonHeapMax", getNonHeapMax())
            .append("threadActive", getThreadActive())
            .append("threadPeak", getThreadPeak())
            .append("gcCount", getGcCount())
            .append("gcTime", getGcTime())
            .append("createTime", getCreateTime())
            .toString();
    }
}
