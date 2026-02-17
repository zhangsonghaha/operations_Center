package com.ruoyi.web.controller.system;

import java.io.File;
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
import com.ruoyi.common.constant.UserConstants;
import com.ruoyi.web.domain.OpsApp;
import com.ruoyi.web.service.IOpsAppService;

/**
 * 应用注册Controller
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/system/app")
public class OpsAppController extends BaseController
{
    @Autowired
    private IOpsAppService opsAppService;

    /**
     * 查询应用注册列表
     */
    @PreAuthorize("@ss.hasPermi('ops:app:list')")
    @GetMapping("/list")
    public TableDataInfo list(OpsApp opsApp)
    {
        startPage();
        List<OpsApp> list = opsAppService.selectOpsAppList(opsApp);
        return getDataTable(list);
    }

    /**
     * 导出应用注册列表
     */
    @PreAuthorize("@ss.hasPermi('ops:app:export')")
    @Log(title = "应用注册", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, OpsApp opsApp)
    {
        List<OpsApp> list = opsAppService.selectOpsAppList(opsApp);
        ExcelUtil<OpsApp> util = new ExcelUtil<OpsApp>(OpsApp.class);
        util.exportExcel(response, list, "应用注册数据");
    }

    /**
     * 获取应用注册详细信息
     */
    @PreAuthorize("@ss.hasPermi('ops:app:query')")
    @GetMapping(value = "/{appId}")
    public AjaxResult getInfo(@PathVariable("appId") Long appId)
    {
        return AjaxResult.success(opsAppService.selectOpsAppById(appId));
    }

    /**
     * 新增应用注册
     */
    @PreAuthorize("@ss.hasPermi('ops:app:add')")
    @Log(title = "应用注册", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody OpsApp opsApp)
    {
        if (!opsAppService.checkAppNameUnique(opsApp))
        {
            return AjaxResult.error("新增应用'" + opsApp.getAppName() + "'失败，应用名称已存在");
        }
        // 校验脚本文件是否存在 (仅本地校验，若为远程管理需配合ServerID)
        // checkFileExists(opsApp.getStartScript());
        
        opsApp.setCreateBy(getUsername());
        return toAjax(opsAppService.insertOpsApp(opsApp));
    }

    /**
     * 修改应用注册
     */
    @PreAuthorize("@ss.hasPermi('ops:app:edit')")
    @Log(title = "应用注册", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody OpsApp opsApp)
    {
        if (!opsAppService.checkAppNameUnique(opsApp))
        {
            return AjaxResult.error("修改应用'" + opsApp.getAppName() + "'失败，应用名称已存在");
        }
        // checkFileExists(opsApp.getStartScript());

        opsApp.setUpdateBy(getUsername());
        return toAjax(opsAppService.updateOpsApp(opsApp));
    }

    /**
     * 删除应用注册
     */
    @PreAuthorize("@ss.hasPermi('ops:app:remove')")
    @Log(title = "应用注册", businessType = BusinessType.DELETE)
	@DeleteMapping("/{appIds}")
    public AjaxResult remove(@PathVariable Long[] appIds)
    {
        return toAjax(opsAppService.deleteOpsAppByIds(appIds));
    }

    private void checkFileExists(String path) {
        if (path == null) return;
        File file = new File(path);
        if (!file.exists()) {
            // throw new RuntimeException("脚本文件不存在: " + path);
            // 暂时注释，避免影响远程应用注册
        }
    }
}
