package com.ruoyi.web.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 部署记录对象 t_ops_deployment
 */
public class OpsDeployment extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 部署ID */
    private Long deploymentId;

    /** 应用名称 */
    private String appName;

    /** 版本号 */
    private String version;

    /** 部署状态(success/failed) */
    private String deployStatus;

    /** 发布时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date deployTime;

    /** 操作人 */
    private String operator;

    public void setDeploymentId(Long deploymentId) 
    {
        this.deploymentId = deploymentId;
    }

    public Long getDeploymentId() 
    {
        return deploymentId;
    }
    public void setAppName(String appName) 
    {
        this.appName = appName;
    }

    public String getAppName() 
    {
        return appName;
    }
    public void setVersion(String version) 
    {
        this.version = version;
    }

    public String getVersion() 
    {
        return version;
    }
    public void setDeployStatus(String deployStatus) 
    {
        this.deployStatus = deployStatus;
    }

    public String getDeployStatus() 
    {
        return deployStatus;
    }
    public void setDeployTime(Date deployTime) 
    {
        this.deployTime = deployTime;
    }

    public Date getDeployTime() 
    {
        return deployTime;
    }
    public void setOperator(String operator) 
    {
        this.operator = operator;
    }

    public String getOperator() 
    {
        return operator;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("deploymentId", getDeploymentId())
            .append("appName", getAppName())
            .append("version", getVersion())
            .append("deployStatus", getDeployStatus())
            .append("deployTime", getDeployTime())
            .append("operator", getOperator())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
