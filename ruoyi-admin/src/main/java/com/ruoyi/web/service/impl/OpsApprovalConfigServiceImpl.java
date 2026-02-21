package com.ruoyi.web.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.web.mapper.OpsApprovalConfigMapper;
import com.ruoyi.web.domain.OpsApprovalConfig;
import com.ruoyi.web.service.IOpsApprovalConfigService;

/**
 * 审批流程配置Service业务层处理
 * 
 * @author ruoyi
 * @date 2024-02-18
 */
@Service
public class OpsApprovalConfigServiceImpl implements IOpsApprovalConfigService 
{
    @Autowired
    private OpsApprovalConfigMapper opsApprovalConfigMapper;

    /**
     * 查询审批流程配置
     * 
     * @param id 审批流程配置主键
     * @return 审批流程配置
     */
    @Override
    public OpsApprovalConfig selectOpsApprovalConfigById(Long id)
    {
        return opsApprovalConfigMapper.selectOpsApprovalConfigById(id);
    }

    /**
     * 查询审批流程配置列表
     * 
     * @param opsApprovalConfig 审批流程配置
     * @return 审批流程配置
     */
    @Override
    public List<OpsApprovalConfig> selectOpsApprovalConfigList(OpsApprovalConfig opsApprovalConfig)
    {
        return opsApprovalConfigMapper.selectOpsApprovalConfigList(opsApprovalConfig);
    }

    /**
     * 新增审批流程配置
     * 
     * @param opsApprovalConfig 审批流程配置
     * @return 结果
     */
    @Override
    public int insertOpsApprovalConfig(OpsApprovalConfig opsApprovalConfig)
    {
        opsApprovalConfig.setCreateTime(DateUtils.getNowDate());
        opsApprovalConfig.setCreateBy(SecurityUtils.getUsername());
        return opsApprovalConfigMapper.insertOpsApprovalConfig(opsApprovalConfig);
    }

    /**
     * 修改审批流程配置
     * 
     * @param opsApprovalConfig 审批流程配置
     * @return 结果
     */
    @Override
    public int updateOpsApprovalConfig(OpsApprovalConfig opsApprovalConfig)
    {
        opsApprovalConfig.setUpdateTime(DateUtils.getNowDate());
        opsApprovalConfig.setUpdateBy(SecurityUtils.getUsername());
        return opsApprovalConfigMapper.updateOpsApprovalConfig(opsApprovalConfig);
    }

    /**
     * 批量删除审批流程配置
     * 
     * @param ids 需要删除的审批流程配置主键
     * @return 结果
     */
    @Override
    public int deleteOpsApprovalConfigByIds(Long[] ids)
    {
        return opsApprovalConfigMapper.deleteOpsApprovalConfigByIds(ids);
    }

    /**
     * 删除审批流程配置信息
     * 
     * @param id 审批流程配置主键
     * @return 结果
     */
    @Override
    public int deleteOpsApprovalConfigById(Long id)
    {
        return opsApprovalConfigMapper.deleteOpsApprovalConfigById(id);
    }
}
