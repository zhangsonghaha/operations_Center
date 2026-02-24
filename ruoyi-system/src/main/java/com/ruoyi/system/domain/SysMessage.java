package com.ruoyi.system.domain;

import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;

/**
 * 站内信对象 sys_message
 * 
 * @author ruoyi
 * @date 2024-05-20
 */
public class SysMessage extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 消息ID */
    private Long messageId;

    /** 发送者ID */
    @Excel(name = "发送者ID")
    private Long senderId;

    /** 接收者ID */
    @Excel(name = "接收者ID")
    private Long receiverId;

    /** 标题 */
    @Excel(name = "标题")
    private String title;

    /** 内容 */
    @Excel(name = "内容")
    private String content;

    /** 消息类型（1通知 2待办 3催办 4完结） */
    @Excel(name = "消息类型", readConverterExp = "1=通知,2=待办,3=催办,4=完结")
    private String messageType;

    /** 阅读状态（0未读 1已读） */
    @Excel(name = "阅读状态", readConverterExp = "0=未读,1=已读")
    private String readStatus;

    /** 删除状态（0正常 1回收站 2彻底删除） */
    private String deleteStatus;

    /** 附件路径 */
    @Excel(name = "附件路径")
    private String attachment;

    /** 关联业务ID */
    private String businessId;

    /** 阅读时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date readTime;

    /** 接收者ID列表（非数据库字段） */
    private List<Long> receiverIds;

    public void setReceiverIds(List<Long> receiverIds)
    {
        this.receiverIds = receiverIds;
    }

    public List<Long> getReceiverIds()
    {
        return receiverIds;
    }

    public void setMessageId(Long messageId) 
    {
        this.messageId = messageId;
    }

    public Long getMessageId() 
    {
        return messageId;
    }
    public void setSenderId(Long senderId) 
    {
        this.senderId = senderId;
    }

    public Long getSenderId() 
    {
        return senderId;
    }
    public void setReceiverId(Long receiverId) 
    {
        this.receiverId = receiverId;
    }

    public Long getReceiverId() 
    {
        return receiverId;
    }
    public void setTitle(String title) 
    {
        this.title = title;
    }

    public String getTitle() 
    {
        return title;
    }
    public void setContent(String content) 
    {
        this.content = content;
    }

    public String getContent() 
    {
        return content;
    }
    public void setMessageType(String messageType) 
    {
        this.messageType = messageType;
    }

    public String getMessageType() 
    {
        return messageType;
    }
    public void setReadStatus(String readStatus) 
    {
        this.readStatus = readStatus;
    }

    public String getReadStatus() 
    {
        return readStatus;
    }
    public void setDeleteStatus(String deleteStatus) 
    {
        this.deleteStatus = deleteStatus;
    }

    public String getDeleteStatus() 
    {
        return deleteStatus;
    }
    public void setAttachment(String attachment) 
    {
        this.attachment = attachment;
    }

    public String getAttachment() 
    {
        return attachment;
    }
    public void setBusinessId(String businessId) 
    {
        this.businessId = businessId;
    }

    public String getBusinessId() 
    {
        return businessId;
    }
    public void setReadTime(Date readTime) 
    {
        this.readTime = readTime;
    }

    public Date getReadTime() 
    {
        return readTime;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("messageId", getMessageId())
            .append("senderId", getSenderId())
            .append("receiverId", getReceiverId())
            .append("title", getTitle())
            .append("content", getContent())
            .append("messageType", getMessageType())
            .append("readStatus", getReadStatus())
            .append("deleteStatus", getDeleteStatus())
            .append("attachment", getAttachment())
            .append("businessId", getBusinessId())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .append("readTime", getReadTime())
            .toString();
    }
}
