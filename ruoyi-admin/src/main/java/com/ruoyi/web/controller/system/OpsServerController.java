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
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.web.domain.OpsServer;
import com.ruoyi.web.service.IOpsServerService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 服务器资产Controller
 */
@RestController
@RequestMapping("/system/server")
public class OpsServerController extends BaseController
{
    @Autowired
    private IOpsServerService opsServerService;

    /**
     * 查询服务器资产列表
     */
    @PreAuthorize("@ss.hasPermi('ops:server:list')")
    @GetMapping("/list")
    public TableDataInfo list(OpsServer opsServer)
    {
        startPage();
        List<OpsServer> list = opsServerService.selectOpsServerList(opsServer);
        return getDataTable(list);
    }

    /**
     * 导出服务器资产列表
     */
    @PreAuthorize("@ss.hasPermi('ops:server:export')")
    @Log(title = "服务器资产", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, OpsServer opsServer)
    {
        List<OpsServer> list = opsServerService.selectOpsServerList(opsServer);
        ExcelUtil<OpsServer> util = new ExcelUtil<OpsServer>(OpsServer.class);
        util.exportExcel(response, list, "服务器资产数据");
    }

    /**
     * 获取服务器资产详细信息
     */
    @PreAuthorize("@ss.hasPermi('ops:server:query')")
    @GetMapping(value = "/{serverId}")
    public AjaxResult getInfo(@PathVariable("serverId") Long serverId)
    {
        return AjaxResult.success(opsServerService.selectOpsServerByServerId(serverId));
    }

    /**
     * 新增服务器资产
     */
    @PreAuthorize("@ss.hasPermi('ops:server:add')")
    @Log(title = "服务器资产", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody OpsServer opsServer)
    {
        opsServer.setCreateBy(getUsername());
        return toAjax(opsServerService.insertOpsServer(opsServer));
    }

    /**
     * 修改服务器资产
     */
    @PreAuthorize("@ss.hasPermi('ops:server:edit')")
    @Log(title = "服务器资产", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody OpsServer opsServer)
    {
        opsServer.setUpdateBy(getUsername());
        return toAjax(opsServerService.updateOpsServer(opsServer));
    }

    /**
     * 删除服务器资产
     */
    @PreAuthorize("@ss.hasPermi('ops:server:remove')")
    @Log(title = "服务器资产", businessType = BusinessType.DELETE)
	@DeleteMapping("/{serverIds}")
    public AjaxResult remove(@PathVariable Long[] serverIds)
    {
        return toAjax(opsServerService.deleteOpsServerByServerIds(serverIds));
    }

    /**
     * 检测服务器连接
     */
    @PreAuthorize("@ss.hasPermi('ops:server:query')")
    @GetMapping("/check/{serverId}")
    public AjaxResult checkConnection(@PathVariable("serverId") Long serverId)
    {
        String result = opsServerService.checkConnection(serverId);
        // 所有检测结果都通过AjaxResult.success返回，由前端根据内容区分显示样式
        // 包括：🟢 内网可达、🔵 公网可达、🟡 认证失败、🔴 不可达
        return AjaxResult.success(result);
    }
}
