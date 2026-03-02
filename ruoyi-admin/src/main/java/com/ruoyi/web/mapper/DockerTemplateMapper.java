package com.ruoyi.web.mapper;

import com.ruoyi.web.domain.DockerTemplate;
import org.apache.ibatis.annotations.Param;
import java.util.List;

/**
 * Docker模板Mapper接口
 * 
 * @author ruoyi
 */
public interface DockerTemplateMapper
{
    /**
     * 查询Docker模板
     * 
     * @param templateId 模板ID
     * @return Docker模板
     */
    public DockerTemplate selectDockerTemplateById(Long templateId);

    /**
     * 根据模板名称查询Docker模板
     * 
     * @param templateName 模板名称
     * @return Docker模板
     */
    public DockerTemplate selectDockerTemplateByName(String templateName);

    /**
     * 查询Docker模板列表
     * 
     * @param dockerTemplate Docker模板
     * @return Docker模板集合
     */
    public List<DockerTemplate> selectDockerTemplateList(DockerTemplate dockerTemplate);

    /**
     * 根据模板类型查询Docker模板列表
     * 
     * @param templateType 模板类型
     * @return Docker模板集合
     */
    public List<DockerTemplate> selectDockerTemplateListByType(String templateType);

    /**
     * 查询所有启用的Docker模板
     * 
     * @return Docker模板集合
     */
    public List<DockerTemplate> selectEnabledDockerTemplateList();

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
     * 删除Docker模板
     * 
     * @param templateId 模板ID
     * @return 结果
     */
    public int deleteDockerTemplateById(Long templateId);

    /**
     * 批量删除Docker模板
     * 
     * @param templateIds 需要删除的模板ID
     * @return 结果
     */
    public int deleteDockerTemplateByIds(Long[] templateIds);

    /**
     * 检查模板名称是否唯一
     * 
     * @param templateName 模板名称
     * @param templateId 模板ID（用于排除自身）
     * @return 结果
     */
    public int checkTemplateNameUnique(@Param("templateName") String templateName, 
                                        @Param("templateId") Long templateId);
}
