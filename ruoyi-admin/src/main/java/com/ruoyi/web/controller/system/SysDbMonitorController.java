package com.ruoyi.web.controller.system;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.system.domain.monitor.DbMonitorRule;
import com.ruoyi.system.service.ISysDbMonitorService;

/**
 * 数据库监控Controller
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/system/db/monitor")
public class SysDbMonitorController extends BaseController
{
    @Autowired
    private ISysDbMonitorService sysDbMonitorService;

    /**
     * 获取实时连接列表
     */
    @PreAuthorize("@ss.hasPermi('system:db:monitor:view')")
    @GetMapping("/process")
    public AjaxResult getProcessList(@RequestParam Long connId)
    {
        return AjaxResult.success(sysDbMonitorService.getProcessList(connId));
    }

    /**
     * 获取表空间统计
     */
    @PreAuthorize("@ss.hasPermi('system:db:monitor:view')")
    @GetMapping("/stats")
    public AjaxResult getTableStats(@RequestParam Long connId)
    {
        return AjaxResult.success(sysDbMonitorService.getTableStats(connId));
    }

    /**
     * 获取慢查询分析
     */
    @PreAuthorize("@ss.hasPermi('system:db:monitor:view')")
    @GetMapping("/slow-queries")
    public AjaxResult getSlowQueries(@RequestParam Long connId, @RequestParam(required = false) Integer limit)
    {
        return AjaxResult.success(sysDbMonitorService.getSlowQueries(connId, limit));
    }

    /**
     * 获取SQL执行统计
     */
    @PreAuthorize("@ss.hasPermi('system:db:monitor:view')")
    @GetMapping("/sql-stats")
    public AjaxResult getSqlStats(@RequestParam Long connId, @RequestParam(required = false) Integer limit)
    {
        return AjaxResult.success(sysDbMonitorService.getSqlStats(connId, limit));
    }

    /**
     * 获取连接数统计
     */
    @PreAuthorize("@ss.hasPermi('system:db:monitor:view')")
    @GetMapping("/connection-stats")
    public AjaxResult getConnectionStats(@RequestParam Long connId)
    {
        return AjaxResult.success(sysDbMonitorService.getConnectionStats(connId));
    }

    /**
     * 终止数据库连接（仅管理员）
     */
    @PreAuthorize("@ss.hasRole('admin')")
    @Log(title = "数据库监控", businessType = BusinessType.FORCE)
    @PostMapping("/kill")
    public AjaxResult killProcess(@RequestParam Long connId, @RequestParam Long processId)
    {
        return AjaxResult.success(sysDbMonitorService.killProcess(connId, processId));
    }

    // ==================== 监控规则管理（仅管理员） ====================

    /**
     * 查询监控规则列表
     */
    @PreAuthorize("@ss.hasRole('admin')")
    @GetMapping("/rules/list")
    public TableDataInfo list(DbMonitorRule dbMonitorRule)
    {
        startPage();
        List<DbMonitorRule> list = sysDbMonitorService.selectDbMonitorRuleList(dbMonitorRule);
        return getDataTable(list);
    }

    /**
     * 获取监控规则详细信息
     */
    @PreAuthorize("@ss.hasRole('admin')")
    @GetMapping("/rules/{ruleId}")
    public AjaxResult getInfo(@PathVariable("ruleId") Long ruleId)
    {
        return AjaxResult.success(sysDbMonitorService.selectDbMonitorRuleById(ruleId));
    }

    /**
     * 新增监控规则
     */
    @PreAuthorize("@ss.hasRole('admin')")
    @Log(title = "数据库监控规则", businessType = BusinessType.INSERT)
    @PostMapping("/rules")
    public AjaxResult add(@RequestBody DbMonitorRule dbMonitorRule)
    {
        return AjaxResult.success(sysDbMonitorService.insertDbMonitorRule(dbMonitorRule));
    }

    /**
     * 修改监控规则
     */
    @PreAuthorize("@ss.hasRole('admin')")
    @Log(title = "数据库监控规则", businessType = BusinessType.UPDATE)
    @PutMapping("/rules")
    public AjaxResult edit(@RequestBody DbMonitorRule dbMonitorRule)
    {
        return AjaxResult.success(sysDbMonitorService.updateDbMonitorRule(dbMonitorRule));
    }

    /**
     * 删除监控规则
     */
    @PreAuthorize("@ss.hasRole('admin')")
    @Log(title = "数据库监控规则", businessType = BusinessType.DELETE)
    @DeleteMapping("/rules/{ruleIds}")
    public AjaxResult remove(@PathVariable Long[] ruleIds)
    {
        return AjaxResult.success(sysDbMonitorService.deleteDbMonitorRuleByIds(ruleIds));
    }
}
