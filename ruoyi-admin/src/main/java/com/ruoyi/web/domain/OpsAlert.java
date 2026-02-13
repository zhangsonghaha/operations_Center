package com.ruoyi.web.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 运维告警对象 t_ops_alert
 */
public class OpsAlert extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 告警ID */
    private Long alertId;

    /** 告警内容 */
    private String alertContent;

    /** 类型(danger/warning/info) */
    private String alertType;

    /** 状态（0待处理 1已处理） */
    private String status;

    /** 告警时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date alertTime;

    public void setAlertId(Long alertId) 
    {
        this.alertId = alertId;
    }

    public Long getAlertId() 
    {
        return alertId;
    }
    public void setAlertContent(String alertContent) 
    {
        this.alertContent = alertContent;
    }

    public String getAlertContent() 
    {
        return alertContent;
    }
    public void setAlertType(String alertType) 
    {
        this.alertType = alertType;
    }

    public String getAlertType() 
    {
        return alertType;
    }
    public void setStatus(String status) 
    {
        this.status = status;
    }

    public String getStatus() 
    {
        return status;
    }
    public void setAlertTime(Date alertTime) 
    {
        this.alertTime = alertTime;
    }

    public Date getAlertTime() 
    {
        return alertTime;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("alertId", getAlertId())
            .append("alertContent", getAlertContent())
            .append("alertType", getAlertType())
            .append("status", getStatus())
            .append("alertTime", getAlertTime())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
