package com.ruoyi.web.service;

import java.util.List;
import com.ruoyi.web.domain.OpsDeployTemplate;
import com.ruoyi.web.domain.OpsDeployTemplateVersion;

import java.util.Map;

/**
 * 部署模板Service接口
 * 
 * @author ruoyi
 */
public interface IOpsDeployTemplateService 
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
     * 批量删除部署模板
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteOpsDeployTemplateByIds(Long[] ids);

    /**
     * 删除部署模板信息
     * 
     * @param id 部署模板主键
     * @return 结果
     */
    public int deleteOpsDeployTemplateById(Long id);

    /**
     * 查询部署模板版本列表
     * @param templateId 模板ID
     * @return 版本列表
     */
    public List<OpsDeployTemplateVersion> selectOpsDeployTemplateVersionList(Long templateId);

    /**
     * 回滚到指定版本
     * @param templateId 模板ID
     * @param versionId 版本ID
     * @return 结果
     */
    public int rollbackTemplate(Long templateId, Long versionId);
    
    /**
     * 执行部署
     * @param templateId 模板ID
     * @param appId 应用ID
     * @param params 部署参数
     * @return 部署记录ID
     */
    public Long deploy(Long templateId, Long appId, Map<String, String> params);
}
