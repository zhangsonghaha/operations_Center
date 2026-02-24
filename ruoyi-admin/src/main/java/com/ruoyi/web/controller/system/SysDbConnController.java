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
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.system.domain.SysDbConn;
import com.ruoyi.system.service.ISysDbConnService;

/**
 * 数据库连接配置Controller
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/system/db/conn")
public class SysDbConnController extends BaseController
{
    @Autowired
    private ISysDbConnService sysDbConnService;

    /**
     * 查询数据库连接配置列表
     */
    @PreAuthorize("@ss.hasPermi('system:db:conn:list')")
    @GetMapping("/list")
    public TableDataInfo list(SysDbConn sysDbConn)
    {
        startPage();
        List<SysDbConn> list = sysDbConnService.selectSysDbConnList(sysDbConn);
        return getDataTable(list);
    }

    /**
     * 导出数据库连接配置列表
     */
    @PreAuthorize("@ss.hasPermi('system:db:conn:export')")
    @Log(title = "数据库连接配置", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, SysDbConn sysDbConn)
    {
        List<SysDbConn> list = sysDbConnService.selectSysDbConnList(sysDbConn);
        ExcelUtil<SysDbConn> util = new ExcelUtil<SysDbConn>(SysDbConn.class);
        util.exportExcel(response, list, "数据库连接配置数据");
    }

    /**
     * 获取数据库连接配置详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:db:conn:query')")
    @GetMapping(value = "/{connId}")
    public AjaxResult getInfo(@PathVariable("connId") Long connId)
    {
        return AjaxResult.success(sysDbConnService.selectSysDbConnByConnId(connId));
    }

    /**
     * 新增数据库连接配置
     */
    @PreAuthorize("@ss.hasPermi('system:db:conn:add')")
    @Log(title = "数据库连接配置", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody SysDbConn sysDbConn)
    {
        sysDbConn.setCreateBy(getUsername());
        return toAjax(sysDbConnService.insertSysDbConn(sysDbConn));
    }

    /**
     * 修改数据库连接配置
     */
    @PreAuthorize("@ss.hasPermi('system:db:conn:edit')")
    @Log(title = "数据库连接配置", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody SysDbConn sysDbConn)
    {
        sysDbConn.setUpdateBy(getUsername());
        return toAjax(sysDbConnService.updateSysDbConn(sysDbConn));
    }

    /**
     * 删除数据库连接配置
     */
    @PreAuthorize("@ss.hasPermi('system:db:conn:remove')")
    @Log(title = "数据库连接配置", businessType = BusinessType.DELETE)
	@DeleteMapping("/{connIds}")
    public AjaxResult remove(@PathVariable Long[] connIds)
    {
        return toAjax(sysDbConnService.deleteSysDbConnByConnIds(connIds));
    }

    /**
     * 测试数据库连接
     */
    @PreAuthorize("@ss.hasPermi('system:db:conn:list')")
    @PostMapping("/test")
    public AjaxResult testConnection(@RequestBody SysDbConn sysDbConn)
    {
        String errorMsg = sysDbConnService.testConnection(sysDbConn);
        if (errorMsg == null) {
            return AjaxResult.success("连接成功");
        } else {
            return AjaxResult.error("连接失败: " + errorMsg);
        }
    }
}
