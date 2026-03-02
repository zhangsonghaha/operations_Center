package com.ruoyi.web.domain.dto;

import java.io.Serializable;

/**
 * Docker环境信息
 * 
 * @author ruoyi
 */
public class DockerEnvInfo implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** Docker是否已安装 */
    private Boolean dockerInstalled;

    /** Docker版本 */
    private String dockerVersion;

    /** Docker服务是否运行 */
    private Boolean dockerRunning;

    /** 存储驱动 */
    private String storageDriver;

    /** Docker根目录 */
    private String dockerRootDir;

    /** 总磁盘空间(字节) */
    private Long totalSpace;

    /** 已使用磁盘空间(字节) */
    private Long usedSpace;

    /** 错误信息 */
    private String errorMessage;

    public Boolean getDockerInstalled() {
        return dockerInstalled;
    }

    public void setDockerInstalled(Boolean dockerInstalled) {
        this.dockerInstalled = dockerInstalled;
    }

    // 为Boolean类型提供is前缀的getter方法
    public Boolean isDockerInstalled() {
        return dockerInstalled;
    }

    public String getDockerVersion() {
        return dockerVersion;
    }

    public void setDockerVersion(String dockerVersion) {
        this.dockerVersion = dockerVersion;
    }

    public Boolean getDockerRunning() {
        return dockerRunning;
    }

    public void setDockerRunning(Boolean dockerRunning) {
        this.dockerRunning = dockerRunning;
    }

    // 为Boolean类型提供is前缀的getter方法
    public Boolean isDockerRunning() {
        return dockerRunning;
    }

    public String getStorageDriver() {
        return storageDriver;
    }

    public void setStorageDriver(String storageDriver) {
        this.storageDriver = storageDriver;
    }

    public String getDockerRootDir() {
        return dockerRootDir;
    }

    public void setDockerRootDir(String dockerRootDir) {
        this.dockerRootDir = dockerRootDir;
    }

    public Long getTotalSpace() {
        return totalSpace;
    }

    public void setTotalSpace(Long totalSpace) {
        this.totalSpace = totalSpace;
    }

    public Long getUsedSpace() {
        return usedSpace;
    }

    public void setUsedSpace(Long usedSpace) {
        this.usedSpace = usedSpace;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    @Override
    public String toString() {
        return "DockerEnvInfo{" +
                "dockerInstalled=" + dockerInstalled +
                ", dockerVersion='" + dockerVersion + '\'' +
                ", dockerRunning=" + dockerRunning +
                ", storageDriver='" + storageDriver + '\'' +
                ", dockerRootDir='" + dockerRootDir + '\'' +
                ", totalSpace=" + totalSpace +
                ", usedSpace=" + usedSpace +
                ", errorMessage='" + errorMessage + '\'' +
                '}';
    }
}
