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
import com.ruoyi.web.domain.OpsEnvironment;
import com.ruoyi.web.service.IOpsEnvironmentService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 环境配置Controller
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/ops/environment")
public class OpsEnvironmentController extends BaseController
{
    @Autowired
    private IOpsEnvironmentService opsEnvironmentService;

    /**
     * 查询环境配置列表
     */
    @PreAuthorize("@ss.hasPermi('ops:environment:list')")
    @GetMapping("/list")
    public TableDataInfo list(OpsEnvironment opsEnvironment)
    {
        startPage();
        List<OpsEnvironment> list = opsEnvironmentService.selectOpsEnvironmentList(opsEnvironment);
        return getDataTable(list);
    }
    
    /**
     * 查询所有环境配置列表(不分页)
     */
    @GetMapping("/listAll")
    public AjaxResult listAll(OpsEnvironment opsEnvironment)
    {
        List<OpsEnvironment> list = opsEnvironmentService.selectOpsEnvironmentList(opsEnvironment);
        return AjaxResult.success(list);
    }

    /**
     * 导出环境配置列表
     */
    @PreAuthorize("@ss.hasPermi('ops:environment:export')")
    @Log(title = "环境配置", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, OpsEnvironment opsEnvironment)
    {
        List<OpsEnvironment> list = opsEnvironmentService.selectOpsEnvironmentList(opsEnvironment);
        ExcelUtil<OpsEnvironment> util = new ExcelUtil<OpsEnvironment>(OpsEnvironment.class);
        util.exportExcel(response, list, "环境配置数据");
    }

    /**
     * 获取环境配置详细信息
     */
    @PreAuthorize("@ss.hasPermi('ops:environment:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return AjaxResult.success(opsEnvironmentService.selectOpsEnvironmentById(id));
    }

    /**
     * 新增环境配置
     */
    @PreAuthorize("@ss.hasPermi('ops:environment:add')")
    @Log(title = "环境配置", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody OpsEnvironment opsEnvironment)
    {
        return toAjax(opsEnvironmentService.insertOpsEnvironment(opsEnvironment));
    }

    /**
     * 修改环境配置
     */
    @PreAuthorize("@ss.hasPermi('ops:environment:edit')")
    @Log(title = "环境配置", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody OpsEnvironment opsEnvironment)
    {
        return toAjax(opsEnvironmentService.updateOpsEnvironment(opsEnvironment));
    }

    /**
     * 删除环境配置
     */
    @PreAuthorize("@ss.hasPermi('ops:environment:remove')")
    @Log(title = "环境配置", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(opsEnvironmentService.deleteOpsEnvironmentByIds(ids));
    }
}
