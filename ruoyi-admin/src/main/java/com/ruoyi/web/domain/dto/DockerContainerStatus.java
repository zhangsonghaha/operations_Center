package com.ruoyi.web.domain.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.io.Serializable;
import java.util.Date;

/**
 * Docker容器状态信息
 * 
 * @author ruoyi
 */
public class DockerContainerStatus implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 容器ID */
    private String containerId;

    /** 容器名称 */
    private String containerName;

    /** 容器状态(running, stopped, exited, paused, restarting) */
    private String status;

    /** 运行状态(Up, Exited, Created, Paused, Restarting) */
    private String state;

    /** 启动时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startedAt;

    /** 退出时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date finishedAt;

    /** 退出代码 */
    private Integer exitCode;

    /** 是否正在运行 */
    private Boolean running;

    /** 是否暂停 */
    private Boolean paused;

    /** 是否重启中 */
    private Boolean restarting;

    /** 重启次数 */
    private Integer restartCount;

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Date getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Date startedAt) {
        this.startedAt = startedAt;
    }

    public Date getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(Date finishedAt) {
        this.finishedAt = finishedAt;
    }

    public Integer getExitCode() {
        return exitCode;
    }

    public void setExitCode(Integer exitCode) {
        this.exitCode = exitCode;
    }

    public Boolean getRunning() {
        return running;
    }

    public void setRunning(Boolean running) {
        this.running = running;
    }

    public Boolean getPaused() {
        return paused;
    }

    public void setPaused(Boolean paused) {
        this.paused = paused;
    }

    public Boolean getRestarting() {
        return restarting;
    }

    public void setRestarting(Boolean restarting) {
        this.restarting = restarting;
    }

    public Integer getRestartCount() {
        return restartCount;
    }

    public void setRestartCount(Integer restartCount) {
        this.restartCount = restartCount;
    }

    @Override
    public String toString() {
        return "DockerContainerStatus{" +
                "containerId='" + containerId + '\'' +
                ", containerName='" + containerName + '\'' +
                ", status='" + status + '\'' +
                ", state='" + state + '\'' +
                ", startedAt=" + startedAt +
                ", finishedAt=" + finishedAt +
                ", exitCode=" + exitCode +
                ", running=" + running +
                ", paused=" + paused +
                ", restarting=" + restarting +
                ", restartCount=" + restartCount +
                '}';
    }
}
