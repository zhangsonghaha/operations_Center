package com.ruoyi.web.controller.system;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.web.service.IOpsDashboardService;

/**
 * 运维仪表盘Controller
 */
@RestController
@RequestMapping("/system/ops")
public class OpsDashboardController extends BaseController
{
    @Autowired
    private IOpsDashboardService opsDashboardService;

    /**
     * 获取仪表盘数据
     */
    // @PreAuthorize("@ss.hasPermi('system:ops:dashboard')")
    @GetMapping("/dashboard")
    public AjaxResult getDashboardData()
    {
        Map<String, Object> data = opsDashboardService.getDashboardData();
        return AjaxResult.success(data);
    }
}
