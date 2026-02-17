package com.ruoyi.web.controller.system;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.web.domain.OpsMonitorLog;
import com.ruoyi.web.service.OpsMonitorService;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * 服务器监控Controller
 */
@RestController
@RequestMapping("/ops/monitor")
public class OpsMonitorController extends BaseController
{
    @Autowired
    private OpsMonitorService opsMonitorService;

    /**
     * 获取实时监控数据
     */
    @PreAuthorize("@ss.hasPermi('ops:server:query')")
    @GetMapping("/realtime/{serverId}")
    public AjaxResult getRealtime(@PathVariable Long serverId)
    {
        return AjaxResult.success(opsMonitorService.getRealtimeData(serverId));
    }

    /**
     * 获取历史趋势数据
     * range: 24h (24小时), 7d (7天)
     */
    @PreAuthorize("@ss.hasPermi('ops:server:query')")
    @GetMapping("/trend/{serverId}")
    public AjaxResult getTrend(@PathVariable Long serverId, @RequestParam(defaultValue = "24h") String range)
    {
        OpsMonitorLog params = new OpsMonitorLog();
        params.setServerId(serverId);
        Map<String, Object> queryParams = new HashMap<>();
        
        Calendar cal = Calendar.getInstance();
        if ("7d".equals(range)) {
            cal.add(Calendar.DATE, -7);
        } else {
            cal.add(Calendar.HOUR_OF_DAY, -24);
        }
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        queryParams.put("beginTime", sdf.format(cal.getTime()));
        params.setParams(queryParams);
        
        List<OpsMonitorLog> list = opsMonitorService.selectOpsMonitorLogList(params);
        return AjaxResult.success(list);
    }
}
