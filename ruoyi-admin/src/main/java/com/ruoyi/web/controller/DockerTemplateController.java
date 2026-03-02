package com.ruoyi.web.controller;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.web.domain.DockerTemplate;
import com.ruoyi.web.service.IDockerTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * Docker模板管理Controller
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/ops/docker/template")
public class DockerTemplateController extends BaseController
{
    @Autowired
    private IDockerTemplateService dockerTemplateService;
    
    /**
     * 查询Docker模板列表
     */
    @PreAuthorize("@ss.hasPermi('ops:docker:template:list')")
    @GetMapping("/list")
    public TableDataInfo list(DockerTemplate dockerTemplate)
    {
        startPage();
        List<DockerTemplate> list = dockerTemplateService.selectDockerTemplateList(dockerTemplate);
        return getDataTable(list);
    }
    
    /**
     * 导出Docker模板列表
     */
    @PreAuthorize("@ss.hasPermi('ops:docker:template:export')")
    @Log(title = "Docker模板", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, DockerTemplate dockerTemplate)
    {
        List<DockerTemplate> list = dockerTemplateService.selectDockerTemplateList(dockerTemplate);
        ExcelUtil<DockerTemplate> util = new ExcelUtil<DockerTemplate>(DockerTemplate.class);
        util.exportExcel(response, list, "Docker模板数据");
    }
    
    /**
     * 获取Docker模板详细信息
     */
    @PreAuthorize("@ss.hasPermi('ops:docker:template:query')")
    @GetMapping(value = "/{templateId}")
    public AjaxResult getInfo(@PathVariable("templateId") Long templateId)
    {
        return success(dockerTemplateService.selectDockerTemplateById(templateId));
    }
    
    /**
     * 新增Docker模板
     */
    @PreAuthorize("@ss.hasPermi('ops:docker:template:add')")
    @Log(title = "Docker模板", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody DockerTemplate dockerTemplate)
    {
        return toAjax(dockerTemplateService.insertDockerTemplate(dockerTemplate));
    }
    
    /**
     * 修改Docker模板
     */
    @PreAuthorize("@ss.hasPermi('ops:docker:template:edit')")
    @Log(title = "Docker模板", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody DockerTemplate dockerTemplate)
    {
        return toAjax(dockerTemplateService.updateDockerTemplate(dockerTemplate));
    }
    
    /**
     * 删除Docker模板
     */
    @PreAuthorize("@ss.hasPermi('ops:docker:template:remove')")
    @Log(title = "Docker模板", businessType = BusinessType.DELETE)
    @DeleteMapping("/{templateIds}")
    public AjaxResult remove(@PathVariable Long[] templateIds)
    {
        return toAjax(dockerTemplateService.deleteDockerTemplateByIds(templateIds));
    }
}
