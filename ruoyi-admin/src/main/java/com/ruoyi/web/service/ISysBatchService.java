package com.ruoyi.web.service;

import java.util.List;
import com.ruoyi.web.domain.SysBatchTask;
import com.ruoyi.web.domain.SysBatchTaskDetail;
import com.ruoyi.web.domain.SysCommandTemplate;

/**
 * 批量操作Service接口
 */
public interface ISysBatchService 
{
    /**
     * 查询批量命令模板列表
     */
    public List<SysCommandTemplate> selectSysCommandTemplateList(SysCommandTemplate sysCommandTemplate);

    /**
     * 新增批量命令模板
     */
    public int insertSysCommandTemplate(SysCommandTemplate sysCommandTemplate);

    /**
     * 修改批量命令模板
     */
    public int updateSysCommandTemplate(SysCommandTemplate sysCommandTemplate);

    /**
     * 批量删除批量命令模板
     */
    public int deleteSysCommandTemplateByIds(Long[] templateIds);

    /**
     * 查询批量任务列表
     */
    public List<SysBatchTask> selectSysBatchTaskList(SysBatchTask sysBatchTask);

    /**
     * 查询批量任务详细
     */
    public SysBatchTask selectSysBatchTaskById(Long taskId);

    /**
     * 新增批量任务
     */
    public int insertSysBatchTask(SysBatchTask sysBatchTask);

    /**
     * 执行批量任务
     * @param taskId 任务ID
     * @param serverIds 目标服务器ID数组
     */
    public void executeBatchTask(Long taskId, Long[] serverIds);

    /**
     * 查询任务明细列表
     */
    public List<SysBatchTaskDetail> selectSysBatchTaskDetailList(SysBatchTaskDetail sysBatchTaskDetail);
}
