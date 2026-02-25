package com.ruoyi.web.controller.system;

import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger logger = LoggerFactory.getLogger(SysDbBackupController.class);

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
     * 执行高级备份
     */
    @PreAuthorize("@ss.hasPermi('system:db:backup:add')")
    @Log(title = "数据库高级备份", businessType = BusinessType.INSERT)
    @PostMapping("/backup-advanced")
    public AjaxResult backupAdvanced(@RequestBody SysDbBackup sysDbBackup)
    {
        logger.info("开始执行高级备份, 连接ID: {}, 数据库类型: {}, 备份方式: {}, 备份级别: {}", 
                sysDbBackup.getConnId(), sysDbBackup.getDbType(), 
                sysDbBackup.getBackupMode(), sysDbBackup.getBackupLevel());
        
        try {
            long startTime = System.currentTimeMillis();
            SysDbBackup result = sysDbBackupService.backupWithOptions(
                sysDbBackup.getConnId(),
                sysDbBackup.getDbType(),
                sysDbBackup.getBackupMode(),
                sysDbBackup.getBackupLevel(),
                sysDbBackup.getTargetName(),
                sysDbBackup.getStorageType(),
                sysDbBackup.getCompressEnabled()
            );
            long endTime = System.currentTimeMillis();
            
            logger.info("高级备份成功, 备份ID: {}, 文件名: {}, 耗时: {}ms, 文件大小: {} bytes", 
                    result.getBackupId(), result.getFileName(), (endTime - startTime), result.getFileSize());
            
            return AjaxResult.success("备份成功", result);
        } catch (Exception e) {
            logger.error("高级备份失败, 连接ID: " + sysDbBackup.getConnId() + ", 错误: " + e.getMessage(), e);
            return AjaxResult.error("备份失败: " + e.getMessage());
        }
    }

    /**
     * 验证备份
     */
    @PreAuthorize("@ss.hasPermi('system:db:backup:query')")
    @PostMapping("/verify/{backupId}")
    public AjaxResult verify(@PathVariable("backupId") Long backupId)
    {
        try {
            boolean result = sysDbBackupService.verifyBackup(backupId);
            return result ? AjaxResult.success("验证成功") : AjaxResult.error("验证失败");
        } catch (Exception e) {
            return AjaxResult.error("验证异常: " + e.getMessage());
        }
    }

    /**
     * 恢复备份
     */
    @PreAuthorize("@ss.hasPermi('system:db:backup:restore')")
    @Log(title = "数据库恢复", businessType = BusinessType.INSERT)
    @PostMapping("/restore")
    public AjaxResult restore(@RequestBody java.util.Map<String, Long> params)
    {
        try {
            Long backupId = params.get("backupId");
            Long targetConnId = params.get("targetConnId");
            boolean result = sysDbBackupService.restoreBackup(backupId, targetConnId);
            return result ? AjaxResult.success("恢复成功") : AjaxResult.error("恢复失败");
        } catch (Exception e) {
            return AjaxResult.error("恢复异常: " + e.getMessage());
        }
    }

    /**
     * 恢复备份（带进度轮询）
     */
    @PreAuthorize("@ss.hasPermi('system:db:backup:restore')")
    @Log(title = "数据库恢复", businessType = BusinessType.INSERT)
    @PostMapping("/restore-async")
    public AjaxResult restoreAsync(@RequestBody java.util.Map<String, Object> params)
    {
        try {
            Long backupId = Long.valueOf(params.get("backupId").toString());
            Long targetConnId = Long.valueOf(params.get("targetConnId").toString());
            
            String taskId = "restore_" + backupId + "_" + System.currentTimeMillis();
            
            // 创建进度对象
            com.ruoyi.system.domain.RestoreProgress progress = com.ruoyi.system.domain.RestoreProgress.create(taskId);
            
            // 异步执行恢复
            new Thread(() -> {
                try {
                    sysDbBackupService.restoreBackupWithProgress(backupId, targetConnId, 
                            new com.ruoyi.system.service.backup.RestoreProgressCallback() {
                                @Override
                                public void onProgress(int progressVal, String message) {
                                    progress.setStatus("running");
                                    progress.addLog("[PROGRESS] " + progressVal + "% - " + message);
                                }

                                @Override
                                public void onStep(String step) {
                                    progress.addLog("[STEP] " + step);
                                }

                                @Override
                                public void onLog(String log) {
                                    progress.addLog(log);
                                }

                                @Override
                                public void onTableProgress(int totalTables, int completedTables) {
                                    progress.setTotalTables(totalTables);
                                    progress.setCurrentTable("已完成 " + completedTables + "/" + totalTables + " 个表");
                                    progress.incrementCompletedTables();
                                }

                                @Override
                                public void onScope(String scope) {
                                    progress.setTargetDatabase(scope);
                                    progress.setBackupFileName(scope);
                                }

                                @Override
                                public void onComplete(boolean success, String message) {
                                    if (success) {
                                        progress.setComplete();
                                        progress.addLog("[SUCCESS] " + message);
                                    } else {
                                        progress.setFailed(message);
                                        progress.addLog("[ERROR] " + message);
                                    }
                                }
                            });
                } catch (Exception e) {
                    progress.setFailed(e.getMessage());
                    progress.addLog("[ERROR] " + e.getMessage());
                    logger.error("异步恢复失败", e);
                }
            }).start();

            return AjaxResult.success("恢复任务已启动", taskId);
        } catch (Exception e) {
            logger.error("启动恢复任务失败", e);
            return AjaxResult.error("启动恢复任务异常: " + e.getMessage());
        }
    }

    /**
     * 清理过期备份
     */
    @PreAuthorize("@ss.hasPermi('system:db:backup:remove')")
    @Log(title = "清理过期备份", businessType = BusinessType.DELETE)
    @PostMapping("/clean-expired")
    public AjaxResult cleanExpired(@RequestBody java.util.Map<String, Integer> params)
    {
        try {
            Integer retentionDays = params.get("retentionDays");
            int count = sysDbBackupService.cleanExpiredBackups(retentionDays);
            return AjaxResult.success("清理完成，共删除 " + count + " 个过期备份");
        } catch (Exception e) {
            return AjaxResult.error("清理失败: " + e.getMessage());
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

    /**
     * 获取恢复进度
     */
    @PreAuthorize("@ss.hasPermi('system:db:backup:query')")
    @GetMapping("/restore/progress/{taskId}")
    public AjaxResult getRestoreProgress(@PathVariable String taskId)
    {
        com.ruoyi.system.domain.RestoreProgress progress = com.ruoyi.system.domain.RestoreProgress.get(taskId);
        if (progress == null) {
            return AjaxResult.error("任务不存在或已过期");
        }
        return AjaxResult.success(progress);
    }
}
