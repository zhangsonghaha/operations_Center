package com.ruoyi.web.controller.system;

import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.system.domain.SysDbBackup;
import com.ruoyi.system.service.ISysDbBackupService;

/**
 * 数据库备份记录Controller
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/system/db/backup")
public class SysDbBackupController extends BaseController
{
    @Autowired
    private ISysDbBackupService sysDbBackupService;

    /**
     * 查询数据库备份记录列表
     */
    @PreAuthorize("@ss.hasPermi('system:db:backup:list')")
    @GetMapping("/list")
    public TableDataInfo list(SysDbBackup sysDbBackup)
    {
        startPage();
        List<SysDbBackup> list = sysDbBackupService.selectSysDbBackupList(sysDbBackup);
        return getDataTable(list);
    }

    /**
     * 导出数据库备份记录列表
     */
    @PreAuthorize("@ss.hasPermi('system:db:backup:export')")
    @Log(title = "数据库备份记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, SysDbBackup sysDbBackup)
    {
        List<SysDbBackup> list = sysDbBackupService.selectSysDbBackupList(sysDbBackup);
        ExcelUtil<SysDbBackup> util = new ExcelUtil<SysDbBackup>(SysDbBackup.class);
        util.exportExcel(response, list, "数据库备份记录数据");
    }

    /**
     * 获取数据库备份记录详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:db:backup:query')")
    @GetMapping(value = "/{backupId}")
    public AjaxResult getInfo(@PathVariable("backupId") Long backupId)
    {
        return AjaxResult.success(sysDbBackupService.selectSysDbBackupByBackupId(backupId));
    }

    /**
     * 执行备份
     */
    @PreAuthorize("@ss.hasPermi('system:db:backup:add')")
    @Log(title = "数据库备份", businessType = BusinessType.INSERT)
    @PostMapping("/backup")
    public AjaxResult backup(@RequestBody SysDbBackup sysDbBackup)
    {
        try {
            sysDbBackupService.backup(sysDbBackup.getConnId());
            return AjaxResult.success("备份成功");
        } catch (Exception e) {
            return AjaxResult.error("备份失败: " + e.getMessage());
        }
    }

    /**
     * 删除数据库备份记录
     */
    @PreAuthorize("@ss.hasPermi('system:db:backup:remove')")
    @Log(title = "数据库备份记录", businessType = BusinessType.DELETE)
	@DeleteMapping("/{backupIds}")
    public AjaxResult remove(@PathVariable Long[] backupIds)
    {
        return toAjax(sysDbBackupService.deleteSysDbBackupByBackupIds(backupIds));
    }
}
