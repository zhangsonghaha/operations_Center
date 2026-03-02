package com.ruoyi.web.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import java.math.BigDecimal;

/**
 * Docker部署模板对象 t_docker_template
 * 
 * @author ruoyi
 */
public class DockerTemplate extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 模板ID */
    private Long templateId;

    /** 模板名称 */
    private String templateName;

    /** 模板类型(mysql, redis, nginx, mongodb, custom) */
    private String templateType;

    /** 镜像名称 */
    private String imageName;

    /** 镜像标签 */
    private String imageTag;

    /** 默认端口映射(JSON格式) */
    private String defaultPorts;

    /** 默认环境变量(JSON格式) */
    private String defaultEnvVars;

    /** 默认卷挂载(JSON格式) */
    private String defaultVolumes;

    /** 默认CPU限制 */
    private BigDecimal defaultCpuLimit;

    /** 默认内存限制 */
    private String defaultMemoryLimit;

    /** 模板描述 */
    private String description;

    /** 是否系统模板(0-否, 1-是) */
    private String isSystem;

    /** 排序 */
    private Integer sortOrder;

    /** 状态(0-正常, 1-停用) */
    private String status;

    public Long getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getTemplateType() {
        return templateType;
    }

    public void setTemplateType(String templateType) {
        this.templateType = templateType;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getImageTag() {
        return imageTag;
    }

    public void setImageTag(String imageTag) {
        this.imageTag = imageTag;
    }

    public String getDefaultPorts() {
        return defaultPorts;
    }

    public void setDefaultPorts(String defaultPorts) {
        this.defaultPorts = defaultPorts;
    }

    public String getDefaultEnvVars() {
        return defaultEnvVars;
    }

    public void setDefaultEnvVars(String defaultEnvVars) {
        this.defaultEnvVars = defaultEnvVars;
    }

    public String getDefaultVolumes() {
        return defaultVolumes;
    }

    public void setDefaultVolumes(String defaultVolumes) {
        this.defaultVolumes = defaultVolumes;
    }

    public BigDecimal getDefaultCpuLimit() {
        return defaultCpuLimit;
    }

    public void setDefaultCpuLimit(BigDecimal defaultCpuLimit) {
        this.defaultCpuLimit = defaultCpuLimit;
    }

    public String getDefaultMemoryLimit() {
        return defaultMemoryLimit;
    }

    public void setDefaultMemoryLimit(String defaultMemoryLimit) {
        this.defaultMemoryLimit = defaultMemoryLimit;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIsSystem() {
        return isSystem;
    }

    public void setIsSystem(String isSystem) {
        this.isSystem = isSystem;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(Integer sortOrder) {
        this.sortOrder = sortOrder;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
