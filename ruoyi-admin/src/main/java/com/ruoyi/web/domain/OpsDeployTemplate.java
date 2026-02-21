package com.ruoyi.web.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 部署模板对象 ops_deploy_template
 * 
 * @author ruoyi
 */
public class OpsDeployTemplate extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** 模板名称 */
    private String templateName;

    /** 适用应用类型 */
    private String appType;

    /** 脚本内容 */
    private String scriptContent;

    /** 当前版本 */
    private String version;

    /** 内置标志(1是 0否) */
    private String builtIn;

    /** 状态(0正常 1停用) */
    private String status;

    /** 脚本SHA256 */
    private String sha256;

    /** 使用指南(Markdown) */
    private String guideContent;

    /** 参数配置JSON */
    private String parameterConfig;

    public void setParameterConfig(String parameterConfig) 
    {
        this.parameterConfig = parameterConfig;
    }

    public String getParameterConfig() 
    {
        return parameterConfig;
    }

    public void setGuideContent(String guideContent) 
    {
        this.guideContent = guideContent;
    }

    public String getGuideContent() 
    {
        return guideContent;
    }

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }
    public void setTemplateName(String templateName) 
    {
        this.templateName = templateName;
    }

    public String getTemplateName() 
    {
        return templateName;
    }
    public void setAppType(String appType) 
    {
        this.appType = appType;
    }

    public String getAppType() 
    {
        return appType;
    }
    public void setScriptContent(String scriptContent) 
    {
        this.scriptContent = scriptContent;
    }

    public String getScriptContent() 
    {
        return scriptContent;
    }
    public void setVersion(String version) 
    {
        this.version = version;
    }

    public String getVersion() 
    {
        return version;
    }
    public void setBuiltIn(String builtIn) 
    {
        this.builtIn = builtIn;
    }

    public String getBuiltIn() 
    {
        return builtIn;
    }
    public void setStatus(String status) 
    {
        this.status = status;
    }

    public String getStatus() 
    {
        return status;
    }
    public void setSha256(String sha256) 
    {
        this.sha256 = sha256;
    }

    public String getSha256() 
    {
        return sha256;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("templateName", getTemplateName())
            .append("appType", getAppType())
            .append("scriptContent", getScriptContent())
            .append("version", getVersion())
            .append("builtIn", getBuiltIn())
            .append("status", getStatus())
            .append("sha256", getSha256())
            .append("guideContent", getGuideContent())
            .append("remark", getRemark())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
