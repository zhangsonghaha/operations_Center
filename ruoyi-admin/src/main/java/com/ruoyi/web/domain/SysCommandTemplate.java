package com.ruoyi.web.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 批量命令模板对象 sys_command_template
 */
public class SysCommandTemplate extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 模板ID */
    private Long templateId;

    /** 模板名称 */
    private String templateName;

    /** 脚本/命令内容 */
    private String commandContent;

    /** 超时时间(秒) */
    private Integer timeout;

    public void setTemplateId(Long templateId) 
    {
        this.templateId = templateId;
    }

    public Long getTemplateId() 
    {
        return templateId;
    }
    public void setTemplateName(String templateName) 
    {
        this.templateName = templateName;
    }

    public String getTemplateName() 
    {
        return templateName;
    }
    public void setCommandContent(String commandContent) 
    {
        this.commandContent = commandContent;
    }

    public String getCommandContent() 
    {
        return commandContent;
    }
    public void setTimeout(Integer timeout) 
    {
        this.timeout = timeout;
    }

    public Integer getTimeout() 
    {
        return timeout;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("templateId", getTemplateId())
            .append("templateName", getTemplateName())
            .append("commandContent", getCommandContent())
            .append("timeout", getTimeout())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
