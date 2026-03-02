package com.ruoyi.web.service.impl;

import com.ruoyi.web.domain.DockerTemplate;
import com.ruoyi.web.mapper.DockerTemplateMapper;
import com.ruoyi.web.service.IDockerTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Docker模板Service业务层处理
 * 
 * @author ruoyi
 */
@Service
public class DockerTemplateServiceImpl implements IDockerTemplateService
{
    @Autowired
    private DockerTemplateMapper dockerTemplateMapper;
    
    /**
     * 查询Docker模板
     */
    @Override
    public DockerTemplate selectDockerTemplateById(Long templateId)
    {
        return dockerTemplateMapper.selectDockerTemplateById(templateId);
    }
    
    /**
     * 查询Docker模板列表
     */
    @Override
    public List<DockerTemplate> selectDockerTemplateList(DockerTemplate dockerTemplate)
    {
        return dockerTemplateMapper.selectDockerTemplateList(dockerTemplate);
    }
    
    /**
     * 新增Docker模板
     */
    @Override
    public int insertDockerTemplate(DockerTemplate dockerTemplate)
    {
        return dockerTemplateMapper.insertDockerTemplate(dockerTemplate);
    }
    
    /**
     * 修改Docker模板
     */
    @Override
    public int updateDockerTemplate(DockerTemplate dockerTemplate)
    {
        return dockerTemplateMapper.updateDockerTemplate(dockerTemplate);
    }
    
    /**
     * 批量删除Docker模板
     */
    @Override
    public int deleteDockerTemplateByIds(Long[] templateIds)
    {
        return dockerTemplateMapper.deleteDockerTemplateByIds(templateIds);
    }
    
    /**
     * 删除Docker模板信息
     */
    @Override
    public int deleteDockerTemplateById(Long templateId)
    {
        return dockerTemplateMapper.deleteDockerTemplateById(templateId);
    }
}
