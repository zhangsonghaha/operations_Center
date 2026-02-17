package com.ruoyi.web.mapper;

import java.util.List;
import com.ruoyi.web.domain.SysCommandTemplate;

/**
 * 批量命令模板Mapper接口
 */
public interface SysCommandTemplateMapper 
{
    /**
     * 查询批量命令模板
     */
    public SysCommandTemplate selectSysCommandTemplateById(Long templateId);

    /**
     * 查询批量命令模板列表
     */
    public List<SysCommandTemplate> selectSysCommandTemplateList(SysCommandTemplate sysCommandTemplate);

    /**
     * 新增批量命令模板
     */
    public int insertSysCommandTemplate(SysCommandTemplate sysCommandTemplate);

    /**
     * 修改批量命令模板
     */
    public int updateSysCommandTemplate(SysCommandTemplate sysCommandTemplate);

    /**
     * 删除批量命令模板
     */
    public int deleteSysCommandTemplateById(Long templateId);

    /**
     * 批量删除批量命令模板
     */
    public int deleteSysCommandTemplateByIds(Long[] templateIds);
}
