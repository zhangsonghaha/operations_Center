package com.ruoyi.web.mapper;

import java.util.List;
import com.ruoyi.web.domain.OpsDeployTemplateVersion;

/**
 * 部署模板版本Mapper接口
 * 
 * @author ruoyi
 */
public interface OpsDeployTemplateVersionMapper 
{
    /**
     * 查询部署模板版本
     * 
     * @param id 部署模板版本主键
     * @return 部署模板版本
     */
    public OpsDeployTemplateVersion selectOpsDeployTemplateVersionById(Long id);

    /**
     * 查询部署模板版本列表
     * 
     * @param opsDeployTemplateVersion 部署模板版本
     * @return 部署模板版本集合
     */
    public List<OpsDeployTemplateVersion> selectOpsDeployTemplateVersionList(OpsDeployTemplateVersion opsDeployTemplateVersion);

    /**
     * 新增部署模板版本
     * 
     * @param opsDeployTemplateVersion 部署模板版本
     * @return 结果
     */
    public int insertOpsDeployTemplateVersion(OpsDeployTemplateVersion opsDeployTemplateVersion);

    /**
     * 修改部署模板版本
     * 
     * @param opsDeployTemplateVersion 部署模板版本
     * @return 结果
     */
    public int updateOpsDeployTemplateVersion(OpsDeployTemplateVersion opsDeployTemplateVersion);

    /**
     * 删除部署模板版本
     * 
     * @param id 部署模板版本主键
     * @return 结果
     */
    public int deleteOpsDeployTemplateVersionById(Long id);

    /**
     * 批量删除部署模板版本
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteOpsDeployTemplateVersionByIds(Long[] ids);
}
