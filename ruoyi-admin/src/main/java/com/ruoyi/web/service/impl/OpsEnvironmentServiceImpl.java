package com.ruoyi.web.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.web.mapper.OpsEnvironmentMapper;
import com.ruoyi.web.domain.OpsEnvironment;
import com.ruoyi.web.service.IOpsEnvironmentService;

/**
 * 环境配置Service业务层处理
 * 
 * @author ruoyi
 */
@Service
public class OpsEnvironmentServiceImpl implements IOpsEnvironmentService 
{
    @Autowired
    private OpsEnvironmentMapper opsEnvironmentMapper;

    /**
     * 查询环境配置
     * 
     * @param id 环境配置ID
     * @return 环境配置
     */
    @Override
    public OpsEnvironment selectOpsEnvironmentById(Long id)
    {
        return opsEnvironmentMapper.selectOpsEnvironmentById(id);
    }

    /**
     * 查询环境配置列表
     * 
     * @param opsEnvironment 环境配置
     * @return 环境配置
     */
    @Override
    public List<OpsEnvironment> selectOpsEnvironmentList(OpsEnvironment opsEnvironment)
    {
        return opsEnvironmentMapper.selectOpsEnvironmentList(opsEnvironment);
    }

    /**
     * 新增环境配置
     * 
     * @param opsEnvironment 环境配置
     * @return 结果
     */
    @Override
    public int insertOpsEnvironment(OpsEnvironment opsEnvironment)
    {
        opsEnvironment.setCreateTime(DateUtils.getNowDate());
        return opsEnvironmentMapper.insertOpsEnvironment(opsEnvironment);
    }

    /**
     * 修改环境配置
     * 
     * @param opsEnvironment 环境配置
     * @return 结果
     */
    @Override
    public int updateOpsEnvironment(OpsEnvironment opsEnvironment)
    {
        opsEnvironment.setUpdateTime(DateUtils.getNowDate());
        return opsEnvironmentMapper.updateOpsEnvironment(opsEnvironment);
    }

    /**
     * 批量删除环境配置
     * 
     * @param ids 需要删除的环境配置ID
     * @return 结果
     */
    @Override
    public int deleteOpsEnvironmentByIds(Long[] ids)
    {
        return opsEnvironmentMapper.deleteOpsEnvironmentByIds(ids);
    }

    /**
     * 删除环境配置信息
     * 
     * @param id 环境配置ID
     * @return 结果
     */
    @Override
    public int deleteOpsEnvironmentById(Long id)
    {
        return opsEnvironmentMapper.deleteOpsEnvironmentById(id);
    }
    
    @Override
    public OpsEnvironment selectOpsEnvironmentByCode(String code) {
        return opsEnvironmentMapper.selectOpsEnvironmentByCode(code);
    }
}
