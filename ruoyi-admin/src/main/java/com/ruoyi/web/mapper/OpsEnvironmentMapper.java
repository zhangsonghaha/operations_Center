package com.ruoyi.web.mapper;

import java.util.List;
import com.ruoyi.web.domain.OpsEnvironment;

/**
 * 环境配置Mapper接口
 * 
 * @author ruoyi
 */
public interface OpsEnvironmentMapper 
{
    /**
     * 查询环境配置
     * 
     * @param id 环境配置ID
     * @return 环境配置
     */
    public OpsEnvironment selectOpsEnvironmentById(Long id);

    /**
     * 查询环境配置列表
     * 
     * @param opsEnvironment 环境配置
     * @return 环境配置集合
     */
    public List<OpsEnvironment> selectOpsEnvironmentList(OpsEnvironment opsEnvironment);

    /**
     * 新增环境配置
     * 
     * @param opsEnvironment 环境配置
     * @return 结果
     */
    public int insertOpsEnvironment(OpsEnvironment opsEnvironment);

    /**
     * 修改环境配置
     * 
     * @param opsEnvironment 环境配置
     * @return 结果
     */
    public int updateOpsEnvironment(OpsEnvironment opsEnvironment);

    /**
     * 删除环境配置
     * 
     * @param id 环境配置ID
     * @return 结果
     */
    public int deleteOpsEnvironmentById(Long id);

    /**
     * 批量删除环境配置
     * 
     * @param ids 需要删除的数据ID
     * @return 结果
     */
    public int deleteOpsEnvironmentByIds(Long[] ids);
    
    /**
     * 根据Code查询环境
     * @param code 环境代码
     * @return 环境配置
     */
    public OpsEnvironment selectOpsEnvironmentByCode(String code);
}
