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
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.system.domain.SysDbBackupStrategy;
import com.ruoyi.system.service.ISysDbBackupStrategyService;

/**
 * 数据库备份策略Controller
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/system/db/backupStrategy")
public class SysDbBackupStrategyController extends BaseController
{
    @Autowired
    private ISysDbBackupStrategyService strategyService;

    /**
     * 查询备份策略列表
     */
    @PreAuthorize("@ss.hasPermi('system:db:backupStrategy:list')")
    @GetMapping("/list")
    public TableDataInfo list(SysDbBackupStrategy strategy)
    {
        startPage();
        List<SysDbBackupStrategy> list = strategyService.selectSysDbBackupStrategyList(strategy);
        return getDataTable(list);
    }

    /**
     * 获取策略详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:db:backupStrategy:query')")
    @GetMapping(value = "/{strategyId}")
    public AjaxResult getInfo(@PathVariable("strategyId") Long strategyId)
    {
        return AjaxResult.success(strategyService.selectSysDbBackupStrategyByStrategyId(strategyId));
    }

    /**
     * 新增备份策略
     */
    @PreAuthorize("@ss.hasPermi('system:db:backupStrategy:add')")
    @Log(title = "数据库备份策略", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody SysDbBackupStrategy strategy)
    {
        return toAjax(strategyService.insertSysDbBackupStrategy(strategy));
    }

    /**
     * 修改备份策略
     */
    @PreAuthorize("@ss.hasPermi('system:db:backupStrategy:edit')")
    @Log(title = "数据库备份策略", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody SysDbBackupStrategy strategy)
    {
        return toAjax(strategyService.updateSysDbBackupStrategy(strategy));
    }

    /**
     * 删除备份策略
     */
    @PreAuthorize("@ss.hasPermi('system:db:backupStrategy:remove')")
    @Log(title = "数据库备份策略", businessType = BusinessType.DELETE)
    @DeleteMapping("/{strategyIds}")
    public AjaxResult remove(@PathVariable Long[] strategyIds)
    {
        return toAjax(strategyService.deleteSysDbBackupStrategyByStrategyIds(strategyIds));
    }

    /**
     * 立即执行策略备份
     */
    @PreAuthorize("@ss.hasPermi('system:db:backupStrategy:add')")
    @Log(title = "数据库备份策略", businessType = BusinessType.OTHER)
    @PostMapping("/execute/{strategyId}")
    public AjaxResult execute(@PathVariable Long strategyId)
    {
        try {
            strategyService.executeBackupByStrategy(strategyId);
            return AjaxResult.success("备份任务已启动");
        } catch (Exception e) {
            return AjaxResult.error("备份失败：" + e.getMessage());
        }
    }
}
