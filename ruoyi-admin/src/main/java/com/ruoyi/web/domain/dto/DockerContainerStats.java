package com.ruoyi.web.domain.dto;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Docker容器统计信息
 * 
 * @author ruoyi
 */
public class DockerContainerStats implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 容器ID */
    private String containerId;

    /** 容器名称 */
    private String containerName;

    /** CPU使用率(百分比) */
    private BigDecimal cpuPercent;

    /** 内存使用量(字节) */
    private Long memoryUsage;

    /** 内存限制(字节) */
    private Long memoryLimit;

    /** 内存使用率(百分比) */
    private BigDecimal memoryPercent;

    /** 网络接收字节数 */
    private Long networkRxBytes;

    /** 网络发送字节数 */
    private Long networkTxBytes;

    /** 磁盘读取字节数 */
    private Long blockReadBytes;

    /** 磁盘写入字节数 */
    private Long blockWriteBytes;

    /** PIDs数量 */
    private Integer pids;

    public String getContainerId() {
        return containerId;
    }

    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }

    public String getContainerName() {
        return containerName;
    }

    public void setContainerName(String containerName) {
        this.containerName = containerName;
    }

    public BigDecimal getCpuPercent() {
        return cpuPercent;
    }

    public void setCpuPercent(BigDecimal cpuPercent) {
        this.cpuPercent = cpuPercent;
    }

    public Long getMemoryUsage() {
        return memoryUsage;
    }

    public void setMemoryUsage(Long memoryUsage) {
        this.memoryUsage = memoryUsage;
    }

    public Long getMemoryLimit() {
        return memoryLimit;
    }

    public void setMemoryLimit(Long memoryLimit) {
        this.memoryLimit = memoryLimit;
    }

    public BigDecimal getMemoryPercent() {
        return memoryPercent;
    }

    public void setMemoryPercent(BigDecimal memoryPercent) {
        this.memoryPercent = memoryPercent;
    }

    public Long getNetworkRxBytes() {
        return networkRxBytes;
    }

    public void setNetworkRxBytes(Long networkRxBytes) {
        this.networkRxBytes = networkRxBytes;
    }

    public Long getNetworkTxBytes() {
        return networkTxBytes;
    }

    public void setNetworkTxBytes(Long networkTxBytes) {
        this.networkTxBytes = networkTxBytes;
    }

    public Long getBlockReadBytes() {
        return blockReadBytes;
    }

    public void setBlockReadBytes(Long blockReadBytes) {
        this.blockReadBytes = blockReadBytes;
    }

    public Long getBlockWriteBytes() {
        return blockWriteBytes;
    }

    public void setBlockWriteBytes(Long blockWriteBytes) {
        this.blockWriteBytes = blockWriteBytes;
    }

    public Integer getPids() {
        return pids;
    }

    public void setPids(Integer pids) {
        this.pids = pids;
    }

    @Override
    public String toString() {
        return "DockerContainerStats{" +
                "containerId='" + containerId + '\'' +
                ", containerName='" + containerName + '\'' +
                ", cpuPercent=" + cpuPercent +
                ", memoryUsage=" + memoryUsage +
                ", memoryLimit=" + memoryLimit +
                ", memoryPercent=" + memoryPercent +
                ", networkRxBytes=" + networkRxBytes +
                ", networkTxBytes=" + networkTxBytes +
                ", blockReadBytes=" + blockReadBytes +
                ", blockWriteBytes=" + blockWriteBytes +
                ", pids=" + pids +
                '}';
    }
}
