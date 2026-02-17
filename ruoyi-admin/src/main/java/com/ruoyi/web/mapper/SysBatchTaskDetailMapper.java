package com.ruoyi.web.mapper;

import java.util.List;
import com.ruoyi.web.domain.SysBatchTaskDetail;

/**
 * 批量任务明细Mapper接口
 */
public interface SysBatchTaskDetailMapper 
{
    /**
     * 查询批量任务明细
     */
    public SysBatchTaskDetail selectSysBatchTaskDetailById(Long detailId);

    /**
     * 查询批量任务明细列表
     */
    public List<SysBatchTaskDetail> selectSysBatchTaskDetailList(SysBatchTaskDetail sysBatchTaskDetail);

    /**
     * 新增批量任务明细
     */
    public int insertSysBatchTaskDetail(SysBatchTaskDetail sysBatchTaskDetail);

    /**
     * 修改批量任务明细
     */
    public int updateSysBatchTaskDetail(SysBatchTaskDetail sysBatchTaskDetail);

    /**
     * 删除批量任务明细
     */
    public int deleteSysBatchTaskDetailById(Long detailId);

    /**
     * 批量删除批量任务明细
     */
    public int deleteSysBatchTaskDetailByIds(Long[] detailIds);

    /**
     * 根据任务ID删除明细
     */
    public int deleteSysBatchTaskDetailByTaskId(Long taskId);
    
    /**
     * 根据任务ID查询明细列表
     */
    public List<SysBatchTaskDetail> selectSysBatchTaskDetailListByTaskId(Long taskId);
}
