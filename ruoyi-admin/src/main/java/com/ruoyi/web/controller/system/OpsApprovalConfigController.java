package com.ruoyi.web.controller.system;

import java.util.List;
import javax.servlet.http.HttpServletResponse;
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
import com.ruoyi.web.domain.OpsApprovalConfig;
import com.ruoyi.web.service.IOpsApprovalConfigService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 审批流程配置Controller
 * 
 * @author ruoyi
 * @date 2024-02-18
 */
@RestController
@RequestMapping("/ops/approvalConfig")
public class OpsApprovalConfigController extends BaseController
{
    @Autowired
    private IOpsApprovalConfigService opsApprovalConfigService;

    /**
     * 查询审批流程配置列表
     */
    @PreAuthorize("@ss.hasPermi('ops:approvalConfig:list')")
    @GetMapping("/list")
    public TableDataInfo list(OpsApprovalConfig opsApprovalConfig)
    {
        startPage();
        List<OpsApprovalConfig> list = opsApprovalConfigService.selectOpsApprovalConfigList(opsApprovalConfig);
        return getDataTable(list);
    }

    /**
     * 导出审批流程配置列表
     */
    @PreAuthorize("@ss.hasPermi('ops:approvalConfig:export')")
    @Log(title = "审批流程配置", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, OpsApprovalConfig opsApprovalConfig)
    {
        List<OpsApprovalConfig> list = opsApprovalConfigService.selectOpsApprovalConfigList(opsApprovalConfig);
        ExcelUtil<OpsApprovalConfig> util = new ExcelUtil<OpsApprovalConfig>(OpsApprovalConfig.class);
        util.exportExcel(response, list, "审批流程配置数据");
    }

    /**
     * 获取审批流程配置详细信息
     */
    @PreAuthorize("@ss.hasPermi('ops:approvalConfig:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return AjaxResult.success(opsApprovalConfigService.selectOpsApprovalConfigById(id));
    }

    /**
     * 新增审批流程配置
     */
    @PreAuthorize("@ss.hasPermi('ops:approvalConfig:add')")
    @Log(title = "审批流程配置", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody OpsApprovalConfig opsApprovalConfig)
    {
        return toAjax(opsApprovalConfigService.insertOpsApprovalConfig(opsApprovalConfig));
    }

    /**
     * 修改审批流程配置
     */
    @PreAuthorize("@ss.hasPermi('ops:approvalConfig:edit')")
    @Log(title = "审批流程配置", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody OpsApprovalConfig opsApprovalConfig)
    {
        return toAjax(opsApprovalConfigService.updateOpsApprovalConfig(opsApprovalConfig));
    }

    /**
     * 删除审批流程配置
     */
    @PreAuthorize("@ss.hasPermi('ops:approvalConfig:remove')")
    @Log(title = "审批流程配置", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(opsApprovalConfigService.deleteOpsApprovalConfigByIds(ids));
    }
}
