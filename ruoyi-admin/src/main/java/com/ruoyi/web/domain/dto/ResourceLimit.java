package com.ruoyi.web.domain.dto;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Docker资源限制配置
 * 
 * @author ruoyi
 */
public class ResourceLimit implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** CPU限制(核数) */
    private BigDecimal cpuLimit;

    /** 内存限制(如: 2g, 512m) */
    private String memoryLimit;

    public ResourceLimit() {
    }

    public ResourceLimit(BigDecimal cpuLimit, String memoryLimit) {
        this.cpuLimit = cpuLimit;
        this.memoryLimit = memoryLimit;
    }

    public BigDecimal getCpuLimit() {
        return cpuLimit;
    }

    public void setCpuLimit(BigDecimal cpuLimit) {
        this.cpuLimit = cpuLimit;
    }

    public String getMemoryLimit() {
        return memoryLimit;
    }

    public void setMemoryLimit(String memoryLimit) {
        this.memoryLimit = memoryLimit;
    }

    @Override
    public String toString() {
        return "ResourceLimit{" +
                "cpuLimit=" + cpuLimit +
                ", memoryLimit='" + memoryLimit + '\'' +
                '}';
    }
}
