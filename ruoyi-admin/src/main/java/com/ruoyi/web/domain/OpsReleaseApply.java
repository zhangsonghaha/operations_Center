package com.ruoyi.web.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * Release Apply object ops_release_apply
 * 
 * @author ruoyi
 */
public class OpsReleaseApply extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** ID */
    private Long id;

    /** App ID */
    @Excel(name = "App ID")
    private Long appId;

    /** Template ID */
    @Excel(name = "Template ID")
    private Long templateId;

    /** Version */
    @Excel(name = "Version")
    private String version;

    /** Title */
    @Excel(name = "Title")
    private String title;

    /** Description */
    @Excel(name = "Description")
    private String description;

    /** Risks */
    @Excel(name = "Risks")
    private String risks;

    /** Rollback Plan */
    @Excel(name = "Rollback Plan")
    private String rollbackPlan;

    /** Requirement IDs */
    @Excel(name = "Requirement IDs")
    private String requirementIds;

    /** Bug IDs */
    @Excel(name = "Bug IDs")
    private String bugIds;

    /** Status */
    @Excel(name = "Status")
    private String status;

    /** Schedule Time */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "Schedule Time", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date scheduleTime;

    /** Apply User ID */
    @Excel(name = "Apply User ID")
    private Long applyUserId;

    /** Apply User Name */
    private String applyUserName;

    /** Audit User ID */
    @Excel(name = "Audit User ID")
    private Long auditUserId;

    /** Audit Time */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "Audit Time", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date auditTime;

    /** Audit Reason */
    @Excel(name = "Audit Reason")
    private String auditReason;

    /** Script Content */
    private String scriptContent;

    /** Environment */
    @Excel(name = "Environment")
    private String environment;

    /** Current Step */
    @Excel(name = "Current Step")
    private Integer currentStep;

    /** Total Steps */
    @Excel(name = "Total Steps")
    private Integer totalSteps;

    /** Approval Status (0-Pending, 1-In Progress, 2-Passed, 3-Rejected) */
    @Excel(name = "Approval Status")
    private String approvalStatus;

    /** Process Instance ID */
    @Excel(name = "Process Instance ID")
    private String processInstanceId;

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

    public void setTemplateId(Long templateId) 
    {
        this.templateId = templateId;
    }

    public Long getTemplateId() 
    {
        return templateId;
    }

    public void setVersion(String version) 
    {
        this.version = version;
    }

    public String getVersion() 
    {
        return version;
    }

    public void setTitle(String title) 
    {
        this.title = title;
    }

    public String getTitle() 
    {
        return title;
    }

    public void setDescription(String description) 
    {
        this.description = description;
    }

    public String getDescription() 
    {
        return description;
    }

    public void setRisks(String risks) 
    {
        this.risks = risks;
    }

    public String getRisks() 
    {
        return risks;
    }

    public void setRollbackPlan(String rollbackPlan) 
    {
        this.rollbackPlan = rollbackPlan;
    }

    public String getRollbackPlan() 
    {
        return rollbackPlan;
    }

    public void setRequirementIds(String requirementIds) 
    {
        this.requirementIds = requirementIds;
    }

    public String getRequirementIds() 
    {
        return requirementIds;
    }

    public void setBugIds(String bugIds) 
    {
        this.bugIds = bugIds;
    }

    public String getBugIds() 
    {
        return bugIds;
    }

    public void setStatus(String status) 
    {
        this.status = status;
    }

    public String getStatus() 
    {
        return status;
    }

    public void setScheduleTime(Date scheduleTime) 
    {
        this.scheduleTime = scheduleTime;
    }

    public Date getScheduleTime() 
    {
        return scheduleTime;
    }

    public void setApplyUserId(Long applyUserId) 
    {
        this.applyUserId = applyUserId;
    }

    public Long getApplyUserId() 
    {
        return applyUserId;
    }

    public void setApplyUserName(String applyUserName) 
    {
        this.applyUserName = applyUserName;
    }

    public String getApplyUserName() 
    {
        return applyUserName;
    }

    public void setAuditUserId(Long auditUserId) 
    {
        this.auditUserId = auditUserId;
    }

    public Long getAuditUserId() 
    {
        return auditUserId;
    }

    public void setAuditTime(Date auditTime) 
    {
        this.auditTime = auditTime;
    }

    public Date getAuditTime() 
    {
        return auditTime;
    }

    public void setAuditReason(String auditReason) 
    {
        this.auditReason = auditReason;
    }

    public String getAuditReason() 
    {
        return auditReason;
    }

    public void setScriptContent(String scriptContent) 
    {
        this.scriptContent = scriptContent;
    }

    public String getScriptContent() 
    {
        return scriptContent;
    }

    public void setEnvironment(String environment) 
    {
        this.environment = environment;
    }

    public String getEnvironment() 
    {
        return environment;
    }

    public void setCurrentStep(Integer currentStep) 
    {
        this.currentStep = currentStep;
    }

    public Integer getCurrentStep() 
    {
        return currentStep;
    }

    public void setTotalSteps(Integer totalSteps) 
    {
        this.totalSteps = totalSteps;
    }

    public Integer getTotalSteps() 
    {
        return totalSteps;
    }

    public void setApprovalStatus(String approvalStatus) 
    {
        this.approvalStatus = approvalStatus;
    }

    public String getApprovalStatus() 
    {
        return approvalStatus;
    }

    public void setProcessInstanceId(String processInstanceId) 
    {
        this.processInstanceId = processInstanceId;
    }

    public String getProcessInstanceId() 
    {
        return processInstanceId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("appId", getAppId())
            .append("templateId", getTemplateId())
            .append("version", getVersion())
            .append("title", getTitle())
            .append("description", getDescription())
            .append("risks", getRisks())
            .append("rollbackPlan", getRollbackPlan())
            .append("requirementIds", getRequirementIds())
            .append("bugIds", getBugIds())
            .append("status", getStatus())
            .append("scheduleTime", getScheduleTime())
            .append("applyUserId", getApplyUserId())
            .append("auditUserId", getAuditUserId())
            .append("auditTime", getAuditTime())
            .append("auditReason", getAuditReason())
            .append("scriptContent", getScriptContent())
            .append("environment", getEnvironment())
            .append("currentStep", getCurrentStep())
            .append("totalSteps", getTotalSteps())
            .append("approvalStatus", getApprovalStatus())
            .append("processInstanceId", getProcessInstanceId())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
