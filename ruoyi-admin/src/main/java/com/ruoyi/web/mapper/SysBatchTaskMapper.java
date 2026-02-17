package com.ruoyi.web.mapper;

import java.util.List;
import com.ruoyi.web.domain.SysBatchTask;

/**
 * 批量任务主Mapper接口
 */
public interface SysBatchTaskMapper 
{
    /**
     * 查询批量任务主
     */
    public SysBatchTask selectSysBatchTaskById(Long taskId);

    /**
     * 查询批量任务主列表
     */
    public List<SysBatchTask> selectSysBatchTaskList(SysBatchTask sysBatchTask);

    /**
     * 新增批量任务主
     */
    public int insertSysBatchTask(SysBatchTask sysBatchTask);

    /**
     * 修改批量任务主
     */
    public int updateSysBatchTask(SysBatchTask sysBatchTask);

    /**
     * 删除批量任务主
     */
    public int deleteSysBatchTaskById(Long taskId);

    /**
     * 批量删除批量任务主
     */
    public int deleteSysBatchTaskByIds(Long[] taskIds);
}
