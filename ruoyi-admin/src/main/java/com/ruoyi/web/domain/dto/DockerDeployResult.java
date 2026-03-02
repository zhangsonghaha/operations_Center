package com.ruoyi.web.domain.dto;

import java.io.Serializable;

/**
 * Docker部署结果
 * 
 * @author ruoyi
 */
public class DockerDeployResult implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 是否成功 */
    private Boolean success;

    /** 容器ID */
    private String containerId;

    /** 容器名称 */
    private String containerName;

    /** 部署日志ID */
    private Long deployLogId;

    /** 结果消息 */
    private String message;

    /** 错误信息 */
    private String errorMessage;

    /** 容器记录ID */
    private Long containerRecordId;

    public DockerDeployResult() {
    }

    public DockerDeployResult(Boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public static DockerDeployResult success(String message) {
        return new DockerDeployResult(true, message);
    }

    public static DockerDeployResult fail(String errorMessage) {
        DockerDeployResult result = new DockerDeployResult(false, "部署失败");
        result.setErrorMessage(errorMessage);
        return result;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

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

    public Long getDeployLogId() {
        return deployLogId;
    }

    public void setDeployLogId(Long deployLogId) {
        this.deployLogId = deployLogId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public Long getContainerRecordId() {
        return containerRecordId;
    }

    public void setContainerRecordId(Long containerRecordId) {
        this.containerRecordId = containerRecordId;
    }

    @Override
    public String toString() {
        return "DockerDeployResult{" +
                "success=" + success +
                ", containerId='" + containerId + '\'' +
                ", containerName='" + containerName + '\'' +
                ", deployLogId=" + deployLogId +
                ", message='" + message + '\'' +
                ", errorMessage='" + errorMessage + '\'' +
                ", containerRecordId=" + containerRecordId +
                '}';
    }
}
