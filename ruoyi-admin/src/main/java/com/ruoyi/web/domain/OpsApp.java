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

    /** 状态（0正常 1停用） */
    private String status;

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
    public void setMonitorPorts(String monitorPorts) 
    {
        this.monitorPorts = monitorPorts;
    }

    public String getMonitorPorts() 
    {
        return monitorPorts;
    }
    public void setStatus(String status) 
    {
        this.status = status;
    }

    public String getStatus() 
    {
        return status;
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
            .append("status", getStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
