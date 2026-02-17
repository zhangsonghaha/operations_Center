package com.ruoyi.system.task;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.ruoyi.system.domain.monitor.JvmTarget;
import com.ruoyi.system.service.monitor.IJvmMonitorService;

/**
 * JVM 监控定时任务
 * 
 * @author ruoyi
 */
@Component("jvmMonitorTask")
public class JvmMonitorTask
{
    @Autowired
    private IJvmMonitorService jvmMonitorService;

    /**
     * 采集所有启用目标的监控数据
     */
    public void collectAll()
    {
        JvmTarget query = new JvmTarget();
        query.setEnabled("0"); // 仅查询启用的
        List<JvmTarget> targets = jvmMonitorService.selectJvmTargetList(query);
        
        for (JvmTarget target : targets) {
            try {
                jvmMonitorService.collectAndSaveMetrics(target.getTargetId());
            } catch (Exception e) {
                // 记录日志但不中断其他目标的采集
                // log.error("采集目标[{}]失败", target.getName(), e);
                // System.out.println("采集目标[" + target.getName() + "]失败: " + e.getMessage());
            }
        }
    }
}
