package com.ruoyi.web.domain.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Docker部署配置
 * 
 * @author ruoyi
 */
public class DockerDeployConfig implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 容器名称 */
    private String containerName;

    /** 镜像名称 */
    private String imageName;

    /** 镜像标签 */
    private String imageTag;

    /** 服务器ID */
    private Long serverId;

    /** 端口映射列表 */
    private List<PortMapping> ports;

    /** 环境变量映射 */
    private Map<String, String> envVars;

    /** 卷挂载列表 */
    private List<VolumeMount> volumes;

    /** CPU限制(核数) */
    private BigDecimal cpuLimit;

    /** 内存限制(如: 2g, 512m) */
    private String memoryLimit;

    /** 重启策略 */
    private String restartPolicy;

    /** 网络模式 */
    private String networkMode;

    /** 健康检查URL */
    private String healthCheckUrl;

    /** 是否执行健康检查 */
    private Boolean enableHealthCheck;

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

    public List<PortMapping> getPorts() {
        return ports;
    }

    public void setPorts(List<PortMapping> ports) {
        this.ports = ports;
    }

    public Map<String, String> getEnvVars() {
        return envVars;
    }

    public void setEnvVars(Map<String, String> envVars) {
        this.envVars = envVars;
    }

    public List<VolumeMount> getVolumes() {
        return volumes;
    }

    public void setVolumes(List<VolumeMount> volumes) {
        this.volumes = volumes;
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

    public String getHealthCheckUrl() {
        return healthCheckUrl;
    }

    public void setHealthCheckUrl(String healthCheckUrl) {
        this.healthCheckUrl = healthCheckUrl;
    }

    public Boolean getEnableHealthCheck() {
        return enableHealthCheck;
    }

    public void setEnableHealthCheck(Boolean enableHealthCheck) {
        this.enableHealthCheck = enableHealthCheck;
    }

    @Override
    public String toString() {
        return "DockerDeployConfig{" +
                "containerName='" + containerName + '\'' +
                ", imageName='" + imageName + '\'' +
                ", imageTag='" + imageTag + '\'' +
                ", serverId=" + serverId +
                ", ports=" + ports +
                ", envVars=" + envVars +
                ", volumes=" + volumes +
                ", cpuLimit=" + cpuLimit +
                ", memoryLimit='" + memoryLimit + '\'' +
                ", restartPolicy='" + restartPolicy + '\'' +
                ", networkMode='" + networkMode + '\'' +
                ", healthCheckUrl='" + healthCheckUrl + '\'' +
                ", enableHealthCheck=" + enableHealthCheck +
                '}';
    }
}
