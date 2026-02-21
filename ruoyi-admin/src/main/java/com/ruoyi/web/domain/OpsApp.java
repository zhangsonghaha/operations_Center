package com.ruoyi.web.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 应用注册对象 t_ops_app
 * 
 * @author ruoyi
 */
public class OpsApp extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 应用ID */
    private Long appId;

    /** 应用名称 */
    private String appName;

    /** 应用类型 */
    private String appType;

    /** 部署路径 */
    private String deployPath;

    /** 启动脚本 */
    private String startScript;

    /** 监控端口 */
    private String monitorPorts;

    /** 负责人 */
    private String owner;

    /** 技术栈 */
    private String techStack;

    /** 所属部门ID */
    private Long deptId;

    /** 停止脚本 */
    private String stopScript;

    /** 应用描述 */
    private String description;

    /** 关联服务器ID集合 */
    private String serverIds;

    /** 状态（0正常 1停用） */
    private String status;

    /** 健康检查地址 */
    private String healthCheckUrl;

    /** 部署超时时间(秒) */
    private Integer deployTimeout;

    /** 重试次数 */
    private Integer retryCount;

    public void setAppId(Long appId) 
    {
        this.appId = appId;
    }

    public Long getAppId() 
    {
        return appId;
    }
    public void setAppName(String appName) 
    {
        this.appName = appName;
    }

    public String getAppName() 
    {
        return appName;
    }
    public void setAppType(String appType) 
    {
        this.appType = appType;
    }

    public String getAppType() 
    {
        return appType;
    }
    public void setDeployPath(String deployPath) 
    {
        this.deployPath = deployPath;
    }

    public String getDeployPath() 
    {
        return deployPath;
    }
    public void setStartScript(String startScript) 
    {
        this.startScript = startScript;
    }

    public String getStartScript() 
    {
        return startScript;
    }
    /** 应用包路径 */
    private String packagePath;

    public void setPackagePath(String packagePath) 
    {
        this.packagePath = packagePath;
    }

    public String getPackagePath() 
    {
        return packagePath;
    }
    
    public void setMonitorPorts(String monitorPorts) 
    {
        this.monitorPorts = monitorPorts;
    }

    public String getMonitorPorts() 
    {
        return monitorPorts;
    }
    public void setOwner(String owner) 
    {
        this.owner = owner;
    }

    public String getOwner() 
    {
        return owner;
    }
    public void setTechStack(String techStack) 
    {
        this.techStack = techStack;
    }

    public String getTechStack() 
    {
        return techStack;
    }
    public void setDeptId(Long deptId) 
    {
        this.deptId = deptId;
    }

    public Long getDeptId() 
    {
        return deptId;
    }
    public void setStopScript(String stopScript) 
    {
        this.stopScript = stopScript;
    }

    public String getStopScript() 
    {
        return stopScript;
    }
    public void setDescription(String description) 
    {
        this.description = description;
    }

    public String getDescription() 
    {
        return description;
    }
    public void setServerIds(String serverIds) 
    {
        this.serverIds = serverIds;
    }

    public String getServerIds() 
    {
        return serverIds;
    }
    public void setStatus(String status) 
    {
        this.status = status;
    }

    public String getStatus() 
    {
        return status;
    }

    public void setHealthCheckUrl(String healthCheckUrl) 
    {
        this.healthCheckUrl = healthCheckUrl;
    }

    public String getHealthCheckUrl() 
    {
        return healthCheckUrl;
    }

    public void setDeployTimeout(Integer deployTimeout) 
    {
        this.deployTimeout = deployTimeout;
    }

    public Integer getDeployTimeout() 
    {
        return deployTimeout;
    }

    public void setRetryCount(Integer retryCount) 
    {
        this.retryCount = retryCount;
    }

    public Integer getRetryCount() 
    {
        return retryCount;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("appId", getAppId())
            .append("appName", getAppName())
            .append("appType", getAppType())
            .append("deployPath", getDeployPath())
            .append("startScript", getStartScript())
            .append("monitorPorts", getMonitorPorts())
            .append("owner", getOwner())
            .append("techStack", getTechStack())
            .append("deptId", getDeptId())
            .append("stopScript", getStopScript())
            .append("description", getDescription())
            .append("serverIds", getServerIds())
            .append("status", getStatus())
            .append("healthCheckUrl", getHealthCheckUrl())
            .append("deployTimeout", getDeployTimeout())
            .append("retryCount", getRetryCount())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
