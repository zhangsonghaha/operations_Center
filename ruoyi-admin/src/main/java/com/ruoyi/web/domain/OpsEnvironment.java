package com.ruoyi.web.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 环境配置对象 ops_environment
 * 
 * @author ruoyi
 */
public class OpsEnvironment extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 环境ID */
    private Long id;

    /** 环境名称 */
    @Excel(name = "环境名称")
    private String name;

    /** 环境代码 */
    @Excel(name = "环境代码")
    private String code;

    /** 显示顺序 */
    @Excel(name = "显示顺序")
    private Integer sort;

    /** 状态(0正常 1停用) */
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    /** 是否需要审批(0否 1是) */
    @Excel(name = "是否需要审批", readConverterExp = "0=否,1=是")
    private String needApproval;

    /** 审批流程Key */
    @Excel(name = "审批流程Key")
    private String approvalProcessKey;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }
    public void setName(String name) 
    {
        this.name = name;
    }

    public String getName() 
    {
        return name;
    }
    public void setCode(String code) 
    {
        this.code = code;
    }

    public String getCode() 
    {
        return code;
    }
    public void setSort(Integer sort) 
    {
        this.sort = sort;
    }

    public Integer getSort() 
    {
        return sort;
    }
    public void setStatus(String status) 
    {
        this.status = status;
    }

    public String getStatus() 
    {
        return status;
    }
    public void setNeedApproval(String needApproval) 
    {
        this.needApproval = needApproval;
    }

    public String getNeedApproval() 
    {
        return needApproval;
    }
    public void setApprovalProcessKey(String approvalProcessKey) 
    {
        this.approvalProcessKey = approvalProcessKey;
    }

    public String getApprovalProcessKey() 
    {
        return approvalProcessKey;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("name", getName())
            .append("code", getCode())
            .append("sort", getSort())
            .append("status", getStatus())
            .append("needApproval", getNeedApproval())
            .append("approvalProcessKey", getApprovalProcessKey())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
