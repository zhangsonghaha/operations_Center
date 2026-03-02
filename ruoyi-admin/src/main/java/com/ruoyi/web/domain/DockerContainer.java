package com.ruoyi.web.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Docker容器对象 t_docker_container
 * 
 * @author ruoyi
 */
public class DockerContainer extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 容器记录ID */
    private Long containerId;

    /** Docker容器ID */
    private String containerDockerId;

    /** 容器名称 */
    private String containerName;

    /** 镜像名称 */
    private String imageName;

    /** 镜像标签 */
    private String imageTag;

    /** 服务器ID */
    private Long serverId;

    /** 服务器名称 */
    private String serverName;

    /** 端口映射(JSON格式) */
    private String portMappings;

    /** 环境变量(JSON格式) */
    private String envVars;

    /** 卷挂载(JSON格式) */
    private String volumeMounts;

    /** CPU限制(核数) */
    private BigDecimal cpuLimit;

    /** 内存限制(如: 2g, 512m) */
    private String memoryLimit;

    /** 重启策略 */
    private String restartPolicy;

    /** 网络模式 */
    private String networkMode;

    /** 容器状态(running-运行中, stopped-已停止, exited-已退出, unknown-未知) */
    private String containerStatus;

    /** 关联的部署日志ID */
    private Long deployLogId;

    /** 健康检查URL */
    private String healthCheckUrl;

    /** 健康状态(healthy-健康, unhealthy-不健康, unknown-未知) */
    private String healthStatus;

    /** 最后健康检查时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date lastHealthCheck;

    public Long getContainerId() {
        return containerId;
    }

    public void setContainerId(Long containerId) {
        this.containerId = containerId;
    }

    public String getContainerDockerId() {
        return containerDockerId;
    }

    public void setContainerDockerId(String containerDockerId) {
        this.containerDockerId = containerDockerId;
    }

    public String getContainerName() {
        return containerName;
    }

    public void setContainerName(String containerName) {
        this.containerName = containerName;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getImageTag() {
        return imageTag;
    }

    public void setImageTag(String imageTag) {
        this.imageTag = imageTag;
    }

    public Long getServerId() {
        return serverId;
    }

    public void setServerId(Long serverId) {
        this.serverId = serverId;
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public String getPortMappings() {
        return portMappings;
    }

    public void setPortMappings(String portMappings) {
        this.portMappings = portMappings;
    }

    public String getEnvVars() {
        return envVars;
    }

    public void setEnvVars(String envVars) {
        this.envVars = envVars;
    }

    public String getVolumeMounts() {
        return volumeMounts;
    }

    public void setVolumeMounts(String volumeMounts) {
        this.volumeMounts = volumeMounts;
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

    public String getRestartPolicy() {
        return restartPolicy;
    }

    public void setRestartPolicy(String restartPolicy) {
        this.restartPolicy = restartPolicy;
    }

    public String getNetworkMode() {
        return networkMode;
    }

    public void setNetworkMode(String networkMode) {
        this.networkMode = networkMode;
    }

    public String getContainerStatus() {
        return containerStatus;
    }

    public void setContainerStatus(String containerStatus) {
        this.containerStatus = containerStatus;
    }

    public Long getDeployLogId() {
        return deployLogId;
    }

    public void setDeployLogId(Long deployLogId) {
        this.deployLogId = deployLogId;
    }

    public String getHealthCheckUrl() {
        return healthCheckUrl;
    }

    public void setHealthCheckUrl(String healthCheckUrl) {
        this.healthCheckUrl = healthCheckUrl;
    }

    public String getHealthStatus() {
        return healthStatus;
    }

    public void setHealthStatus(String healthStatus) {
        this.healthStatus = healthStatus;
    }

    public Date getLastHealthCheck() {
        return lastHealthCheck;
    }

    public void setLastHealthCheck(Date lastHealthCheck) {
        this.lastHealthCheck = lastHealthCheck;
    }
}
