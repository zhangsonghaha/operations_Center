package com.ruoyi.web.mapper;

import java.util.List;
import com.ruoyi.web.domain.OpsHost;
import com.ruoyi.web.domain.OpsDeployment;
import com.ruoyi.web.domain.OpsAlert;
import com.ruoyi.web.domain.OpsMonitorLog;

/**
 * 运维仪表盘Mapper接口
 */
public interface OpsDashboardMapper 
{
    /**
     * 查询所有主机列表
     */
    public List<OpsHost> selectOpsHostList(OpsHost opsHost);

    /**
     * 查询所有部署记录
     */
    public List<OpsDeployment> selectOpsDeploymentList(OpsDeployment opsDeployment);

    /**
     * 查询所有告警记录
     */
    public List<OpsAlert> selectOpsAlertList(OpsAlert opsAlert);

    /**
     * 查询监控日志列表
     */
    public List<OpsMonitorLog> selectOpsMonitorLogList(OpsMonitorLog opsMonitorLog);

    /**
     * 统计在线主机数量
     */
    public int countOnlineHosts();

    /**
     * 统计待处理告警数量
     */
    public int countPendingAlerts();

    /**
     * 统计今日发布数量
     */
    public int countTodayDeployments();
}
