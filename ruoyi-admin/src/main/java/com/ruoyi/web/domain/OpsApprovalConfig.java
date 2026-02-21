package com.ruoyi.web.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 审批流程配置对象 t_ops_approval_config
 * 
 * @author ruoyi
 * @date 2024-02-18
 */
public class OpsApprovalConfig extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 应用ID */
    @Excel(name = "应用ID")
    private Long appId;

    /** 环境 */
    @Excel(name = "环境")
    private String env;

    /** 审批节点序号 */
    @Excel(name = "审批节点序号")
    private Integer step;

    /** 审批人类型 */
    @Excel(name = "审批人类型")
    private String approverType;

    /** 审批人ID */
    @Excel(name = "审批人ID")
    private Long approverId;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }
    public void setAppId(Long appId) 
    {
        this.appId = appId;
    }

    public Long getAppId() 
    {
        return appId;
    }
    public void setEnv(String env) 
    {
        this.env = env;
    }

    public String getEnv() 
    {
        return env;
    }
    public void setStep(Integer step) 
    {
        this.step = step;
    }

    public Integer getStep() 
    {
        return step;
    }
    public void setApproverType(String approverType) 
    {
        this.approverType = approverType;
    }

    public String getApproverType() 
    {
        return approverType;
    }
    public void setApproverId(Long approverId) 
    {
        this.approverId = approverId;
    }

    public Long getApproverId() 
    {
        return approverId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("appId", getAppId())
            .append("env", getEnv())
            .append("step", getStep())
            .append("approverType", getApproverType())
            .append("approverId", getApproverId())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
