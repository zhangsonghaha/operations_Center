package com.ruoyi.web.controller.system;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.web.domain.OpsDeployRecord;
import com.ruoyi.web.service.IOpsDeployRecordService;

/**
 * 部署记录 Controller
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/ops/deployRecord")
public class OpsDeployRecordController extends BaseController
{
    @Autowired
    private IOpsDeployRecordService opsDeployRecordService;

    /**
     * 查询部署记录
     */
    @PreAuthorize("@ss.hasPermi('ops:deployRecord:query')")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return AjaxResult.success(opsDeployRecordService.selectOpsDeployRecordById(id));
    }

    /**
     * 查询部署记录列表
     */
    @PreAuthorize("@ss.hasPermi('ops:deployRecord:list')")
    @GetMapping("/list")
    public AjaxResult list(OpsDeployRecord opsDeployRecord)
    {
        startPage();
        return AjaxResult.success(opsDeployRecordService.selectOpsDeployRecordList(opsDeployRecord));
    }
}
