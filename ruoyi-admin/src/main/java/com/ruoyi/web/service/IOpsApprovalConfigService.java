package com.ruoyi.web.service;

import java.util.List;
import com.ruoyi.web.domain.OpsApprovalConfig;

/**
 * 审批流程配置Service接口
 * 
 * @author ruoyi
 * @date 2024-02-18
 */
public interface IOpsApprovalConfigService 
{
    /**
     * 查询审批流程配置
     * 
     * @param id 审批流程配置主键
     * @return 审批流程配置
     */
    public OpsApprovalConfig selectOpsApprovalConfigById(Long id);

    /**
     * 查询审批流程配置列表
     * 
     * @param opsApprovalConfig 审批流程配置
     * @return 审批流程配置集合
     */
    public List<OpsApprovalConfig> selectOpsApprovalConfigList(OpsApprovalConfig opsApprovalConfig);

    /**
     * 新增审批流程配置
     * 
     * @param opsApprovalConfig 审批流程配置
     * @return 结果
     */
    public int insertOpsApprovalConfig(OpsApprovalConfig opsApprovalConfig);

    /**
     * 修改审批流程配置
     * 
     * @param opsApprovalConfig 审批流程配置
     * @return 结果
     */
    public int updateOpsApprovalConfig(OpsApprovalConfig opsApprovalConfig);

    /**
     * 批量删除审批流程配置
     * 
     * @param ids 需要删除的审批流程配置主键集合
     * @return 结果
     */
    public int deleteOpsApprovalConfigByIds(Long[] ids);

    /**
     * 删除审批流程配置信息
     * 
     * @param id 审批流程配置主键
     * @return 结果
     */
    public int deleteOpsApprovalConfigById(Long id);
}
