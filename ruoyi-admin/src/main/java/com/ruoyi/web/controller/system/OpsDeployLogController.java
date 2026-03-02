package com.ruoyi.web.controller.system;

import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.web.domain.OpsDeployLog;
import com.ruoyi.web.service.IOpsDeployLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 部署日志Controller
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/system/deployLog")
public class OpsDeployLogController extends BaseController {
    
    @Autowired
    private IOpsDeployLogService opsDeployLogService;
    
    /**
     * 查询部署日志列表
     */
    @PreAuthorize("@ss.hasPermi('ops:deployLog:list')")
    @GetMapping("/list")
    public TableDataInfo list(OpsDeployLog opsDeployLog) {
        startPage();
        List<OpsDeployLog> list = opsDeployLogService.selectOpsDeployLogList(opsDeployLog);
        return getDataTable(list);
    }
    
    /**
     * 获取部署日志详细信息
     */
    @PreAuthorize("@ss.hasPermi('ops:deployLog:query')")
    @GetMapping(value = "/{logId}")
    public AjaxResult getInfo(@PathVariable("logId") Long logId) {
        return success(opsDeployLogService.selectOpsDeployLogById(logId));
    }
    
    /**
     * 删除部署日志
     */
    @PreAuthorize("@ss.hasPermi('ops:deployLog:remove')")
    @Log(title = "部署日志", businessType = BusinessType.DELETE)
    @DeleteMapping("/{logIds}")
    public AjaxResult remove(@PathVariable Long[] logIds) {
        return toAjax(opsDeployLogService.deleteOpsDeployLogByIds(logIds));
    }
    
    /**
     * 开始部署
     */
    @PreAuthorize("@ss.hasPermi('ops:app:deploy')")
    @Log(title = "应用部署", businessType = BusinessType.OTHER)
    @PostMapping("/start")
    public AjaxResult startDeploy(@RequestParam Long appId, 
                                   @RequestParam Long serverId,
                                   @RequestParam(defaultValue = "deploy") String deployType) {
        Long logId = opsDeployLogService.startDeploy(appId, serverId, deployType);
        return success(logId);
    }
}
