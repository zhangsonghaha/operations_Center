package com.ruoyi.web.controller.monitor;

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
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.system.domain.monitor.JvmAlertRule;
import com.ruoyi.system.domain.monitor.JvmMetric;
import com.ruoyi.system.domain.monitor.JvmTarget;
import com.ruoyi.system.service.monitor.IJvmMonitorService;

/**
 * JVM 监控 Controller
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/monitor/jvm")
public class JvmMonitorController extends BaseController
{
    @Autowired
    private IJvmMonitorService jvmMonitorService;

    // --- 监控目标 ---

    @PreAuthorize("@ss.hasPermi('monitor:jvm:list')")
    @GetMapping("/target/list")
    public TableDataInfo listTargets(JvmTarget jvmTarget)
    {
        startPage();
        List<JvmTarget> list = jvmMonitorService.selectJvmTargetList(jvmTarget);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('monitor:jvm:query')")
    @GetMapping(value = "/target/{targetId}")
    public AjaxResult getTarget(@PathVariable("targetId") Long targetId)
    {
        return AjaxResult.success(jvmMonitorService.selectJvmTargetById(targetId));
    }

    @PreAuthorize("@ss.hasPermi('monitor:jvm:add')")
    @Log(title = "JVM监控目标", businessType = BusinessType.INSERT)
    @PostMapping("/target")
    public AjaxResult addTarget(@RequestBody JvmTarget jvmTarget)
    {
        jvmTarget.setCreateBy(getUsername());
        return toAjax(jvmMonitorService.insertJvmTarget(jvmTarget));
    }

    @PreAuthorize("@ss.hasPermi('monitor:jvm:edit')")
    @Log(title = "JVM监控目标", businessType = BusinessType.UPDATE)
    @PutMapping("/target")
    public AjaxResult editTarget(@RequestBody JvmTarget jvmTarget)
    {
        jvmTarget.setUpdateBy(getUsername());
        return toAjax(jvmMonitorService.updateJvmTarget(jvmTarget));
    }

    @PreAuthorize("@ss.hasPermi('monitor:jvm:remove')")
    @Log(title = "JVM监控目标", businessType = BusinessType.DELETE)
    @DeleteMapping("/target/{targetIds}")
    public AjaxResult removeTarget(@PathVariable Long[] targetIds)
    {
        return toAjax(jvmMonitorService.deleteJvmTargetByIds(targetIds));
    }

    // --- 实时监控与历史 ---

    @PreAuthorize("@ss.hasPermi('monitor:jvm:query')")
    @GetMapping(value = "/realtime/{targetId}")
    public AjaxResult getRealtimeInfo(@PathVariable("targetId") Long targetId)
    {
        // 尝试采集一次最新数据
        try {
            return AjaxResult.success(jvmMonitorService.collectAndSaveMetrics(targetId));
        } catch (Exception e) {
            return AjaxResult.error(e.getMessage());
        }
    }

    @PreAuthorize("@ss.hasPermi('monitor:jvm:query')")
    @GetMapping("/metric/list")
    public TableDataInfo listMetrics(JvmMetric jvmMetric)
    {
        startPage();
        List<JvmMetric> list = jvmMonitorService.selectJvmMetricList(jvmMetric);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('monitor:jvm:edit')")
    @Log(title = "JVM监控", businessType = BusinessType.OTHER)
    @PostMapping("/gc/{targetId}")
    public AjaxResult triggerGc(@PathVariable("targetId") Long targetId)
    {
        try {
            jvmMonitorService.triggerGc(targetId);
            return AjaxResult.success("GC 触发成功");
        } catch (Exception e) {
            return AjaxResult.error(e.getMessage());
        }
    }

    // --- 告警规则 ---

    @PreAuthorize("@ss.hasPermi('monitor:jvm:list')")
    @GetMapping("/rule/list")
    public TableDataInfo listRules(JvmAlertRule jvmAlertRule)
    {
        startPage();
        List<JvmAlertRule> list = jvmMonitorService.selectJvmAlertRuleList(jvmAlertRule);
        return getDataTable(list);
    }

    @PreAuthorize("@ss.hasPermi('monitor:jvm:add')")
    @Log(title = "JVM告警规则", businessType = BusinessType.INSERT)
    @PostMapping("/rule")
    public AjaxResult addRule(@RequestBody JvmAlertRule jvmAlertRule)
    {
        jvmAlertRule.setCreateBy(getUsername());
        return toAjax(jvmMonitorService.insertJvmAlertRule(jvmAlertRule));
    }

    @PreAuthorize("@ss.hasPermi('monitor:jvm:edit')")
    @Log(title = "JVM告警规则", businessType = BusinessType.UPDATE)
    @PutMapping("/rule")
    public AjaxResult editRule(@RequestBody JvmAlertRule jvmAlertRule)
    {
        jvmAlertRule.setUpdateBy(getUsername());
        return toAjax(jvmMonitorService.updateJvmAlertRule(jvmAlertRule));
    }

    @PreAuthorize("@ss.hasPermi('monitor:jvm:remove')")
    @Log(title = "JVM告警规则", businessType = BusinessType.DELETE)
    @DeleteMapping("/rule/{ruleIds}")
    public AjaxResult removeRule(@PathVariable Long[] ruleIds)
    {
        return toAjax(jvmMonitorService.deleteJvmAlertRuleByIds(ruleIds));
    }
}
