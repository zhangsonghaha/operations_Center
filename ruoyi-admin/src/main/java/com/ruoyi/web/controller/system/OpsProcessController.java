package com.ruoyi.web.controller.system;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.annotation.RateLimiter;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.web.domain.OpsProcess;
import com.ruoyi.web.service.OpsProcessService;

/**
 * 进程管理Controller
 */
@RestController
@RequestMapping("/ops/process")
public class OpsProcessController extends BaseController
{
    @Autowired
    private OpsProcessService opsProcessService;

    /**
     * 获取服务器进程列表
     */
    @PreAuthorize("@ss.hasPermi('ops:process:list')")
    @RateLimiter(time = 1, count = 1) // 限制每秒1次请求，防止频繁刷新
    @GetMapping("/list")
    public TableDataInfo list(OpsProcess opsProcess)
    {
        if (opsProcess.getServerId() == null) {
            return getDataTable(java.util.Collections.emptyList());
        }
        startPage();
        List<OpsProcess> list = opsProcessService.selectProcessList(opsProcess.getServerId(), opsProcess);
        return getDataTable(list);
    }

    /**
     * 终止进程
     */
    @PreAuthorize("@ss.hasPermi('ops:process:kill')")
    @Log(title = "进程管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/kill/{serverId}/{pids}")
    public AjaxResult kill(@PathVariable Long serverId, @PathVariable String[] pids)
    {
        opsProcessService.killProcesses(serverId, Arrays.asList(pids));
        return success();
    }
}
