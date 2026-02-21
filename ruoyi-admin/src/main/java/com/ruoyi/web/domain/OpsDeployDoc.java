package com.ruoyi.web.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 部署模板文档对象 ops_deploy_doc
 * 
 * @author ruoyi
 */
public class OpsDeployDoc extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 文档ID */
    private Long docId;

    /** 模板ID */
    private Long templateId;

    /** 版本号 */
    private String version;

    /** 文档名称 */
    private String docName;

    /** 文档路径 */
    private String docPath;

    /** 文档类型(pdf,docx,md) */
    private String docType;

    /** 文件大小 */
    private Long fileSize;

    public void setDocId(Long docId) 
    {
        this.docId = docId;
    }

    public Long getDocId() 
    {
        return docId;
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
    public void setDocName(String docName) 
    {
        this.docName = docName;
    }

    public String getDocName() 
    {
        return docName;
    }
    public void setDocPath(String docPath) 
    {
        this.docPath = docPath;
    }

    public String getDocPath() 
    {
        return docPath;
    }
    public void setDocType(String docType) 
    {
        this.docType = docType;
    }

    public String getDocType() 
    {
        return docType;
    }
    public void setFileSize(Long fileSize) 
    {
        this.fileSize = fileSize;
    }

    public Long getFileSize() 
    {
        return fileSize;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("docId", getDocId())
            .append("templateId", getTemplateId())
            .append("version", getVersion())
            .append("docName", getDocName())
            .append("docPath", getDocPath())
            .append("docType", getDocType())
            .append("fileSize", getFileSize())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
