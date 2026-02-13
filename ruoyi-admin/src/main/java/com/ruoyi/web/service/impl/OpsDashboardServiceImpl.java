package com.ruoyi.web.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.web.mapper.OpsDashboardMapper;
import com.ruoyi.web.domain.OpsHost;
import com.ruoyi.web.domain.OpsDeployment;
import com.ruoyi.web.domain.OpsAlert;
import com.ruoyi.web.domain.OpsMonitorLog;
import com.ruoyi.web.service.IOpsDashboardService;

/**
 * 运维仪表盘Service业务层处理
 */
@Service
public class OpsDashboardServiceImpl implements IOpsDashboardService 
{
    @Autowired
    private OpsDashboardMapper opsDashboardMapper;

    /**
     * 获取仪表盘聚合数据
     */
    @Override
    public Map<String, Object> getDashboardData()
    {
        Map<String, Object> data = new HashMap<>();

        // 1. 核心指标
        Map<String, Object> stats = new HashMap<>();
        stats.put("onlineHosts", opsDashboardMapper.countOnlineHosts());
        stats.put("pendingAlerts", opsDashboardMapper.countPendingAlerts());
        stats.put("systemAvailability", "99.9%"); // 模拟数据或复杂计算
        stats.put("todayDeployments", opsDashboardMapper.countTodayDeployments());
        data.put("stats", stats);

        // 2. 部署记录 (取最新的5条)
        OpsDeployment deployQuery = new OpsDeployment();
        List<OpsDeployment> deployments = opsDashboardMapper.selectOpsDeploymentList(deployQuery);
        data.put("deployments", deployments.stream().limit(5).toArray());

        // 3. 告警动态 (取最新的5条)
        OpsAlert alertQuery = new OpsAlert();
        List<OpsAlert> alerts = opsDashboardMapper.selectOpsAlertList(alertQuery);
        data.put("activities", alerts.stream().limit(5).toArray());

        // 4. 监控图表数据
        List<OpsMonitorLog> monitorLogs = opsDashboardMapper.selectOpsMonitorLogList(new OpsMonitorLog());
        data.put("monitorLogs", monitorLogs);

        return data;
    }
}
