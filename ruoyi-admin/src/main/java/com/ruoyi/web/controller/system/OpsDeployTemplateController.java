package com.ruoyi.web.controller.system;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.web.domain.OpsDeployTemplate;
import com.ruoyi.web.domain.OpsDeployTemplateVersion;
import com.ruoyi.web.service.IOpsDeployTemplateService;
import com.ruoyi.common.core.page.TableDataInfo;

import java.util.Map;

/**
 * 部署模板Controller
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/ops/deployTemplate")
public class OpsDeployTemplateController extends BaseController
{
    @Autowired
    private IOpsDeployTemplateService opsDeployTemplateService;

    /**
     * 查询部署模板列表
     */
    @PreAuthorize("@ss.hasPermi('ops:deployTemplate:list')")
    @GetMapping("/list")
    public TableDataInfo list(OpsDeployTemplate opsDeployTemplate)
    {
        startPage();
        List<OpsDeployTemplate> list = opsDeployTemplateService.selectOpsDeployTemplateList(opsDeployTemplate);
        return getDataTable(list);
    }

    /**
     * 获取部署模板详细信息
     */
    @PreAuthorize("@ss.hasPermi('ops:deployTemplate:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return AjaxResult.success(opsDeployTemplateService.selectOpsDeployTemplateById(id));
    }

    /**
     * 新增部署模板
     */
    @PreAuthorize("@ss.hasPermi('ops:deployTemplate:add')")
    @Log(title = "部署模板", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody OpsDeployTemplate opsDeployTemplate)
    {
        return toAjax(opsDeployTemplateService.insertOpsDeployTemplate(opsDeployTemplate));
    }

    /**
     * 修改部署模板
     */
    @PreAuthorize("@ss.hasPermi('ops:deployTemplate:edit')")
    @Log(title = "部署模板", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody OpsDeployTemplate opsDeployTemplate)
    {
        return toAjax(opsDeployTemplateService.updateOpsDeployTemplate(opsDeployTemplate));
    }

    /**
     * 删除部署模板
     */
    @PreAuthorize("@ss.hasPermi('ops:deployTemplate:remove')")
    @Log(title = "部署模板", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(opsDeployTemplateService.deleteOpsDeployTemplateByIds(ids));
    }

    /**
     * 查询版本历史
     */
    @PreAuthorize("@ss.hasPermi('ops:deployTemplate:query')")
    @GetMapping("/{id}/versions")
    public TableDataInfo versions(@PathVariable("id") Long id)
    {
        startPage();
        List<OpsDeployTemplateVersion> list = opsDeployTemplateService.selectOpsDeployTemplateVersionList(id);
        return getDataTable(list);
    }

    /**
     * 回滚版本
     */
    @PreAuthorize("@ss.hasPermi('ops:deployTemplate:edit')")
    @Log(title = "部署模板回滚", businessType = BusinessType.UPDATE)
    @PostMapping("/{id}/rollback")
    public AjaxResult rollback(@PathVariable("id") Long id, Long versionId)
    {
        return toAjax(opsDeployTemplateService.rollbackTemplate(id, versionId));
    }

    /**
     * 执行部署
     */
    @PreAuthorize("@ss.hasPermi('ops:deployTemplate:deploy')")
    @Log(title = "部署模板执行", businessType = BusinessType.OTHER)
    @PostMapping("/{id}/deploy")
    public AjaxResult deploy(@PathVariable("id") Long id, @RequestBody Map<String, Object> body)
    {
        Long appId = Long.valueOf(body.get("appId").toString());
        Map<String, String> params = (Map<String, String>) body.get("params");
        return AjaxResult.success(opsDeployTemplateService.deploy(id, appId, params));
    }
}
