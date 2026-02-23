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
import com.ruoyi.web.domain.OpsReleaseApply;
import com.ruoyi.web.service.IOpsReleaseApplyService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

import com.ruoyi.web.service.WorkflowService;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.OutputStream;
import org.springframework.http.MediaType;

/**
 * Release Apply Controller
 *
 * @author ruoyi
 */
@RestController
@RequestMapping("/ops/release")
public class OpsReleaseApplyController extends BaseController
{
    @Autowired
    private IOpsReleaseApplyService opsReleaseApplyService;

    @Autowired
    private WorkflowService workflowService;

    /**
     * Get process image
     */
    @GetMapping("/process/image/{processInstanceId}")
    public void getProcessImage(@PathVariable String processInstanceId, HttpServletResponse response) {
        try {
            InputStream is = workflowService.generateProcessImage(processInstanceId);
            response.setContentType(MediaType.IMAGE_PNG_VALUE);
            
            OutputStream os = response.getOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) != -1) {
                os.write(buffer, 0, length);
            }
            os.flush();
            os.close();
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Query Release Apply List
     */
    @PreAuthorize("@ss.hasPermi('ops:release:list')")
    @GetMapping("/list")
    public TableDataInfo list(OpsReleaseApply opsReleaseApply)
    {
        startPage();
        List<OpsReleaseApply> list = opsReleaseApplyService.selectOpsReleaseApplyList(opsReleaseApply);
        return getDataTable(list);
    }

    /**
     * Query Pending Release Approvals for Current User
     */
    @PreAuthorize("@ss.hasPermi('ops:release:pending')")
    @GetMapping("/pending")
    public TableDataInfo pending(OpsReleaseApply opsReleaseApply)
    {
        startPage();
        List<OpsReleaseApply> list = opsReleaseApplyService.selectPendingApprovals(opsReleaseApply);
        return getDataTable(list);
    }

    /**
     * Export Release Apply List
     */
    @PreAuthorize("@ss.hasPermi('ops:release:export')")
    @Log(title = "Release Apply", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, OpsReleaseApply opsReleaseApply)
    {
        List<OpsReleaseApply> list = opsReleaseApplyService.selectOpsReleaseApplyList(opsReleaseApply);
        ExcelUtil<OpsReleaseApply> util = new ExcelUtil<OpsReleaseApply>(OpsReleaseApply.class);
        util.exportExcel(response, list, "Release Apply Data");
    }

    /**
     * Get Release Apply Detail
     */
    @PreAuthorize("@ss.hasPermi('ops:release:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return AjaxResult.success(opsReleaseApplyService.selectOpsReleaseApplyById(id));
    }

    /**
     * Add Release Apply
     */
    @PreAuthorize("@ss.hasPermi('ops:release:add')")
    @Log(title = "Release Apply", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody OpsReleaseApply opsReleaseApply)
    {
        return toAjax(opsReleaseApplyService.insertOpsReleaseApply(opsReleaseApply));
    }

    /**
     * Update Release Apply
     */
    @PreAuthorize("@ss.hasPermi('ops:release:edit')")
    @Log(title = "Release Apply", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody OpsReleaseApply opsReleaseApply)
    {
        return toAjax(opsReleaseApplyService.updateOpsReleaseApply(opsReleaseApply));
    }

    /**
     * Audit Release Apply
     */
    @PreAuthorize("@ss.hasPermi('ops:release:audit')")
    @Log(title = "Release Audit", businessType = BusinessType.UPDATE)
    @PutMapping("/audit")
    public AjaxResult audit(@RequestBody OpsReleaseApply opsReleaseApply)
    {
        // New logic: processApproval
        opsReleaseApplyService.processApproval(opsReleaseApply.getId(), opsReleaseApply.getStatus(), opsReleaseApply.getAuditReason());
        return AjaxResult.success();
    }
    
    /**
     * One-click approval for Super Admin
     */
    @PreAuthorize("@ss.hasRole('admin')")
    @Log(title = "Super Admin One-Click Approval", businessType = BusinessType.UPDATE)
    @PutMapping("/superAudit/{id}")
    public AjaxResult superAudit(@PathVariable Long id) {
        opsReleaseApplyService.superAdminApprove(id);
        return AjaxResult.success();
    }

    /**
     * Execute Release
     */
    @PreAuthorize("@ss.hasPermi('ops:release:deploy')")
    @Log(title = "Execute Release", businessType = BusinessType.OTHER)
    @PostMapping("/execute/{id}")
    public AjaxResult execute(@PathVariable Long id)
    {
        opsReleaseApplyService.executeRelease(id);
        return AjaxResult.success();
    }

    /**
     * Delete Release Apply
     */
    @PreAuthorize("@ss.hasPermi('ops:release:remove')")
    @Log(title = "Release Apply", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(opsReleaseApplyService.deleteOpsReleaseApplyByIds(ids));
    }
}
