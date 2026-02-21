package com.ruoyi.web.mapper;

import java.util.List;
import com.ruoyi.web.domain.OpsDeployTemplate;

/**
 * 部署模板Mapper接口
 * 
 * @author ruoyi
 */
public interface OpsDeployTemplateMapper 
{
    /**
     * 查询部署模板
     * 
     * @param id 部署模板主键
     * @return 部署模板
     */
    public OpsDeployTemplate selectOpsDeployTemplateById(Long id);

    /**
     * 查询部署模板列表
     * 
     * @param opsDeployTemplate 部署模板
     * @return 部署模板集合
     */
    public List<OpsDeployTemplate> selectOpsDeployTemplateList(OpsDeployTemplate opsDeployTemplate);

    /**
     * 新增部署模板
     * 
     * @param opsDeployTemplate 部署模板
     * @return 结果
     */
    public int insertOpsDeployTemplate(OpsDeployTemplate opsDeployTemplate);

    /**
     * 修改部署模板
     * 
     * @param opsDeployTemplate 部署模板
     * @return 结果
     */
    public int updateOpsDeployTemplate(OpsDeployTemplate opsDeployTemplate);

    /**
     * 删除部署模板
     * 
     * @param id 部署模板主键
     * @return 结果
     */
    public int deleteOpsDeployTemplateById(Long id);

    /**
     * 批量删除部署模板
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteOpsDeployTemplateByIds(Long[] ids);

    /**
     * 根据应用类型和内置标志查询模板
     * @param appType 应用类型
     * @param builtIn 内置标志
     * @return 模板
     */
    public OpsDeployTemplate selectOpsDeployTemplateByAppTypeAndBuiltIn(String appType, String builtIn);
}
