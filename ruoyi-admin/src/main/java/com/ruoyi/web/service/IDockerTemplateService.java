package com.ruoyi.web.service;

import com.ruoyi.web.domain.DockerTemplate;
import java.util.List;

/**
 * Docker模板Service接口
 * 
 * @author ruoyi
 */
public interface IDockerTemplateService
{
    /**
     * 查询Docker模板
     * 
     * @param templateId Docker模板主键
     * @return Docker模板
     */
    public DockerTemplate selectDockerTemplateById(Long templateId);

    /**
     * 查询Docker模板列表
     * 
     * @param dockerTemplate Docker模板
     * @return Docker模板集合
     */
    public List<DockerTemplate> selectDockerTemplateList(DockerTemplate dockerTemplate);

    /**
     * 新增Docker模板
     * 
     * @param dockerTemplate Docker模板
     * @return 结果
     */
    public int insertDockerTemplate(DockerTemplate dockerTemplate);

    /**
     * 修改Docker模板
     * 
     * @param dockerTemplate Docker模板
     * @return 结果
     */
    public int updateDockerTemplate(DockerTemplate dockerTemplate);

    /**
     * 批量删除Docker模板
     * 
     * @param templateIds 需要删除的Docker模板主键集合
     * @return 结果
     */
    public int deleteDockerTemplateByIds(Long[] templateIds);

    /**
     * 删除Docker模板信息
     * 
     * @param templateId Docker模板主键
     * @return 结果
     */
    public int deleteDockerTemplateById(Long templateId);
}
