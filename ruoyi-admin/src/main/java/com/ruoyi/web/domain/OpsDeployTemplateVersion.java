package com.ruoyi.web.domain;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 部署模板版本对象 ops_deploy_template_version
 * 
 * @author ruoyi
 */
public class OpsDeployTemplateVersion extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 模板ID */
    private Long templateId;

    /** 版本号 */
    private String version;

    /** 脚本内容 */
    private String scriptContent;

    /** 脚本SHA256 */
    private String sha256;

    /** 使用指南(Markdown) */
    private String guideContent;

    public void setGuideContent(String guideContent) 
    {
        this.guideContent = guideContent;
    }

    public String getGuideContent() 
    {
        return guideContent;
    }

    /** 变更日志 */
    private String changeLog;

    /** 创建人 */
    private String creator;

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
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
    public void setScriptContent(String scriptContent) 
    {
        this.scriptContent = scriptContent;
    }

    public String getScriptContent() 
    {
        return scriptContent;
    }
    public void setSha256(String sha256) 
    {
        this.sha256 = sha256;
    }

    public String getSha256() 
    {
        return sha256;
    }
    public void setChangeLog(String changeLog) 
    {
        this.changeLog = changeLog;
    }

    public String getChangeLog() 
    {
        return changeLog;
    }
    public void setCreator(String creator) 
    {
        this.creator = creator;
    }

    public String getCreator() 
    {
        return creator;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("templateId", getTemplateId())
            .append("version", getVersion())
            .append("scriptContent", getScriptContent())
            .append("sha256", getSha256())
            .append("guideContent", getGuideContent())
            .append("changeLog", getChangeLog())
            .append("creator", getCreator())
            .append("createTime", getCreateTime())
            .toString();
    }
}
