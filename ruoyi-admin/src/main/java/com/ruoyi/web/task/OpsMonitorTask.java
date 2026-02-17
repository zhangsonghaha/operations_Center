package com.ruoyi.web.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import com.ruoyi.web.service.OpsMonitorService;

/**
 * 服务器监控定时任务
 */
@Component("opsMonitorTask")
public class OpsMonitorTask
{
    @Autowired
    private OpsMonitorService opsMonitorService;

    /**
     * 每5分钟执行一次采集
     */
    @Scheduled(cron = "0 0/5 * * * ?")
    public void collectTrendData()
    {
        opsMonitorService.collectTrendData();
    }
}
