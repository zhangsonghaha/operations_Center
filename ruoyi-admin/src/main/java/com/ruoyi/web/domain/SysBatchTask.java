package com.ruoyi.web.domain;

import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 批量任务主对象 sys_batch_task
 */
public class SysBatchTask extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 任务ID */
    private Long taskId;

    /** 任务名称 */
    private String taskName;

    /** 任务类型（1命令执行 2文件分发） */
    private String taskType;

    /** 执行命令内容 */
    private String commandContent;

    /** 源文件路径 */
    private String sourceFile;

    /** 目标路径 */
    private String destPath;

    /** 状态（0等待 1执行中 2完成 3失败 4部分成功） */
    private String status;

    /** 总主机数 */
    private Integer totalHost;

    /** 成功主机数 */
    private Integer successHost;

    /** 失败主机数 */
    private Integer failHost;

    /** 开始执行时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    /** 结束执行时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;

    /** 任务明细信息 */
    private List<SysBatchTaskDetail> taskDetailList;

    public void setTaskId(Long taskId) 
    {
        this.taskId = taskId;
    }

    public Long getTaskId() 
    {
        return taskId;
    }
    public void setTaskName(String taskName) 
    {
        this.taskName = taskName;
    }

    public String getTaskName() 
    {
        return taskName;
    }
    public void setTaskType(String taskType) 
    {
        this.taskType = taskType;
    }

    public String getTaskType() 
    {
        return taskType;
    }
    public void setCommandContent(String commandContent) 
    {
        this.commandContent = commandContent;
    }

    public String getCommandContent() 
    {
        return commandContent;
    }
    public void setSourceFile(String sourceFile) 
    {
        this.sourceFile = sourceFile;
    }

    public String getSourceFile() 
    {
        return sourceFile;
    }
    public void setDestPath(String destPath) 
    {
        this.destPath = destPath;
    }

    public String getDestPath() 
    {
        return destPath;
    }
    public void setStatus(String status) 
    {
        this.status = status;
    }

    public String getStatus() 
    {
        return status;
    }
    public void setTotalHost(Integer totalHost) 
    {
        this.totalHost = totalHost;
    }

    public Integer getTotalHost() 
    {
        return totalHost;
    }
    public void setSuccessHost(Integer successHost) 
    {
        this.successHost = successHost;
    }

    public Integer getSuccessHost() 
    {
        return successHost;
    }
    public void setFailHost(Integer failHost) 
    {
        this.failHost = failHost;
    }

    public Integer getFailHost() 
    {
        return failHost;
    }
    public void setStartTime(Date startTime) 
    {
        this.startTime = startTime;
    }

    public Date getStartTime() 
    {
        return startTime;
    }
    public void setEndTime(Date endTime) 
    {
        this.endTime = endTime;
    }

    public Date getEndTime() 
    {
        return endTime;
    }

    public List<SysBatchTaskDetail> getTaskDetailList()
    {
        return taskDetailList;
    }

    public void setTaskDetailList(List<SysBatchTaskDetail> taskDetailList)
    {
        this.taskDetailList = taskDetailList;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("taskId", getTaskId())
            .append("taskName", getTaskName())
            .append("taskType", getTaskType())
            .append("commandContent", getCommandContent())
            .append("sourceFile", getSourceFile())
            .append("destPath", getDestPath())
            .append("status", getStatus())
            .append("totalHost", getTotalHost())
            .append("successHost", getSuccessHost())
            .append("failHost", getFailHost())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("startTime", getStartTime())
            .append("endTime", getEndTime())
            .append("taskDetailList", getTaskDetailList())
            .toString();
    }
}
