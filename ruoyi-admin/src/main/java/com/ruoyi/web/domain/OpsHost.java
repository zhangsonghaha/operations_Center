package com.ruoyi.web.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 主机设备对象 t_ops_host
 */
public class OpsHost extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主机ID */
    private Long hostId;

    /** 主机名称 */
    private String hostName;

    /** IP地址 */
    private String ipAddress;

    /** 状态（0正常 1停用） */
    private String status;

    /** CPU使用率 */
    private Double cpuUsage;

    /** 内存使用率 */
    private Double memoryUsage;

    public void setHostId(Long hostId) 
    {
        this.hostId = hostId;
    }

    public Long getHostId() 
    {
        return hostId;
    }
    public void setHostName(String hostName) 
    {
        this.hostName = hostName;
    }

    public String getHostName() 
    {
        return hostName;
    }
    public void setIpAddress(String ipAddress) 
    {
        this.ipAddress = ipAddress;
    }

    public String getIpAddress() 
    {
        return ipAddress;
    }
    public void setStatus(String status) 
    {
        this.status = status;
    }

    public String getStatus() 
    {
        return status;
    }
    public void setCpuUsage(Double cpuUsage) 
    {
        this.cpuUsage = cpuUsage;
    }

    public Double getCpuUsage() 
    {
        return cpuUsage;
    }
    public void setMemoryUsage(Double memoryUsage) 
    {
        this.memoryUsage = memoryUsage;
    }

    public Double getMemoryUsage() 
    {
        return memoryUsage;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("hostId", getHostId())
            .append("hostName", getHostName())
            .append("ipAddress", getIpAddress())
            .append("status", getStatus())
            .append("cpuUsage", getCpuUsage())
            .append("memoryUsage", getMemoryUsage())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
