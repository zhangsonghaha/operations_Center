package com.ruoyi.web.controller.system;

import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.system.domain.SysDbLog;
import com.ruoyi.system.service.ISysDbLogService;

/**
 * 数据库操作日志Controller
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/system/db/log")
public class SysDbLogController extends BaseController
{
    @Autowired
    private ISysDbLogService sysDbLogService;

    /**
     * 查询数据库操作日志列表
     */
    @PreAuthorize("@ss.hasPermi('system:db:log:list')")
    @GetMapping("/list")
    public TableDataInfo list(SysDbLog sysDbLog)
    {
        startPage();
        List<SysDbLog> list = sysDbLogService.selectSysDbLogList(sysDbLog);
        return getDataTable(list);
    }

    /**
     * 导出数据库操作日志列表
     */
    @PreAuthorize("@ss.hasPermi('system:db:log:export')")
    @Log(title = "数据库操作日志", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, SysDbLog sysDbLog)
    {
        List<SysDbLog> list = sysDbLogService.selectSysDbLogList(sysDbLog);
        ExcelUtil<SysDbLog> util = new ExcelUtil<SysDbLog>(SysDbLog.class);
        util.exportExcel(response, list, "数据库操作日志数据");
    }

    /**
     * 获取数据库操作日志详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:db:log:query')")
    @GetMapping(value = "/{logId}")
    public AjaxResult getInfo(@PathVariable("logId") Long logId)
    {
        return AjaxResult.success(sysDbLogService.selectSysDbLogByLogId(logId));
    }
}
