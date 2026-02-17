package com.ruoyi.system.service.monitor.impl;

import java.io.IOException;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.ThreadMXBean;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.core.text.Convert;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.system.domain.monitor.JvmAlertRule;
import com.ruoyi.system.domain.monitor.JvmMetric;
import com.ruoyi.system.domain.monitor.JvmTarget;
import com.ruoyi.system.mapper.monitor.JvmAlertRuleMapper;
import com.ruoyi.system.mapper.monitor.JvmMetricMapper;
import com.ruoyi.system.mapper.monitor.JvmTargetMapper;
import com.ruoyi.system.service.monitor.IJvmMonitorService;

/**
 * JVM 监控服务实现
 * 
 * @author ruoyi
 */
@Service
public class JvmMonitorServiceImpl implements IJvmMonitorService
{
    private static final Logger log = LoggerFactory.getLogger(JvmMonitorServiceImpl.class);

    @Autowired
    private JvmTargetMapper jvmTargetMapper;

    @Autowired
    private JvmMetricMapper jvmMetricMapper;

    @Autowired
    private JvmAlertRuleMapper jvmAlertRuleMapper;

    /**
     * 查询监控目标
     * 
     * @param targetId 监控目标ID
     * @return 监控目标
     */
    @Override
    public JvmTarget selectJvmTargetById(Long targetId)
    {
        return jvmTargetMapper.selectJvmTargetById(targetId);
    }

    /**
     * 查询监控目标列表
     * 
     * @param jvmTarget 监控目标
     * @return 监控目标
     */
    @Override
    public List<JvmTarget> selectJvmTargetList(JvmTarget jvmTarget)
    {
        return jvmTargetMapper.selectJvmTargetList(jvmTarget);
    }

    /**
     * 新增监控目标
     * 
     * @param jvmTarget 监控目标
     * @return 结果
     */
    @Override
    public int insertJvmTarget(JvmTarget jvmTarget)
    {
        return jvmTargetMapper.insertJvmTarget(jvmTarget);
    }

    /**
     * 修改监控目标
     * 
     * @param jvmTarget 监控目标
     * @return 结果
     */
    @Override
    public int updateJvmTarget(JvmTarget jvmTarget)
    {
        return jvmTargetMapper.updateJvmTarget(jvmTarget);
    }

    /**
     * 删除监控目标对象
     * 
     * @param targetIds 需要删除的数据ID
     * @return 结果
     */
    @Override
    public int deleteJvmTargetByIds(Long[] targetIds)
    {
        return jvmTargetMapper.deleteJvmTargetByIds(targetIds);
    }

    /**
     * 删除监控目标信息
     * 
     * @param targetId 监控目标ID
     * @return 结果
     */
    @Override
    public int deleteJvmTargetById(Long targetId)
    {
        return jvmTargetMapper.deleteJvmTargetById(targetId);
    }

    /**
     * 采集指定目标的监控数据并保存
     * 
     * @param targetId 目标ID
     * @return 采集结果
     */
    @Override
    public JvmMetric collectAndSaveMetrics(Long targetId) {
        JvmTarget target = selectJvmTargetById(targetId);
        if (target == null) {
            throw new ServiceException("监控目标不存在");
        }

        JvmMetric metric = new JvmMetric();
        metric.setTargetId(targetId);
        metric.setCreateTime(new Date());

        JMXConnector connector = null;
        try {
            // 本地连接特殊处理 (这里简化为只支持远程JMX URL格式，如果是本地可以通过配置 localhost)
            String serviceUrl = String.format("service:jmx:rmi:///jndi/rmi://%s:%d/jmxrmi", target.getHost(), target.getPort());
            JMXServiceURL url = new JMXServiceURL(serviceUrl);
            
            Map<String, Object> env = new HashMap<>();
            if (target.getUsername() != null && !target.getUsername().isEmpty()) {
                String[] credentials = new String[] { target.getUsername(), target.getPassword() };
                env.put(JMXConnector.CREDENTIALS, credentials);
            }

            connector = JMXConnectorFactory.connect(url, env);
            MBeanServerConnection mbsc = connector.getMBeanServerConnection();

            // 1. Memory
            MemoryMXBean memoryBean = ManagementFactory.newPlatformMXBeanProxy(mbsc, ManagementFactory.MEMORY_MXBEAN_NAME, MemoryMXBean.class);
            MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
            MemoryUsage nonHeapUsage = memoryBean.getNonHeapMemoryUsage();
            
            metric.setHeapUsed(heapUsage.getUsed());
            metric.setHeapMax(heapUsage.getMax());
            metric.setNonHeapUsed(nonHeapUsage.getUsed());
            metric.setNonHeapMax(nonHeapUsage.getMax());

            // 2. Thread
            ThreadMXBean threadBean = ManagementFactory.newPlatformMXBeanProxy(mbsc, ManagementFactory.THREAD_MXBEAN_NAME, ThreadMXBean.class);
            metric.setThreadActive(threadBean.getThreadCount());
            metric.setThreadPeak(threadBean.getPeakThreadCount());

            // 3. GC
            long gcCount = 0;
            long gcTime = 0;
            ObjectName gcName = new ObjectName(ManagementFactory.GARBAGE_COLLECTOR_MXBEAN_DOMAIN_TYPE + ",*");
            Set<ObjectName> gcMBeans = mbsc.queryNames(gcName, null);
            for (ObjectName name : gcMBeans) {
                GarbageCollectorMXBean gcBean = ManagementFactory.newPlatformMXBeanProxy(mbsc, name.getCanonicalName(), GarbageCollectorMXBean.class);
                gcCount += gcBean.getCollectionCount();
                gcTime += gcBean.getCollectionTime();
            }
            metric.setGcCount(gcCount);
            metric.setGcTime(gcTime);

            // 保存到数据库
            jvmMetricMapper.insertJvmMetric(metric);
            
            // 检查告警
            checkAlerts(metric);

        } catch (Exception e) {
            log.error("采集监控数据失败: targetId={}", targetId, e);
            throw new ServiceException("采集失败: " + e.getMessage());
        } finally {
            if (connector != null) {
                try {
                    connector.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
        return metric;
    }

    /**
     * 检查告警规则
     */
    private void checkAlerts(JvmMetric metric) {
        JvmAlertRule query = new JvmAlertRule();
        query.setTargetId(metric.getTargetId());
        query.setEnabled("0");
        List<JvmAlertRule> rules = jvmAlertRuleMapper.selectJvmAlertRuleList(query);
        
        // 同时查询全局规则
        query.setTargetId(0L);
        List<JvmAlertRule> globalRules = jvmAlertRuleMapper.selectJvmAlertRuleList(query);
        rules.addAll(globalRules);

        for (JvmAlertRule rule : rules) {
            boolean triggered = false;
            double currentValue = 0;

            if ("heap_usage".equals(rule.getMetricName()) && metric.getHeapMax() > 0) {
                currentValue = (double) metric.getHeapUsed() / metric.getHeapMax();
            } else if ("thread_count".equals(rule.getMetricName())) {
                currentValue = metric.getThreadActive();
            }

            if ("GT".equals(rule.getCondition())) {
                triggered = currentValue > rule.getThreshold();
            } else if ("LT".equals(rule.getCondition())) {
                triggered = currentValue < rule.getThreshold();
            }

            if (triggered) {
                log.warn("JVM 告警触发: Target={}, Metric={}, Value={}, Threshold={}", 
                        metric.getTargetId(), rule.getMetricName(), currentValue, rule.getThreshold());
                // 这里可以扩展发送邮件或短信通知
            }
        }
    }

    @Override
    public JvmMetric getLatestMetric(Long targetId) {
        // 简单实现：查询最近一条记录
        JvmMetric query = new JvmMetric();
        query.setTargetId(targetId);
        List<JvmMetric> list = jvmMetricMapper.selectJvmMetricList(query); // 假设Mapper已按时间倒序
        if (!list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }

    @Override
    public List<JvmMetric> selectJvmMetricList(JvmMetric jvmMetric) {
        return jvmMetricMapper.selectJvmMetricList(jvmMetric);
    }

    @Override
    public void triggerGc(Long targetId) {
        JvmTarget target = selectJvmTargetById(targetId);
        if (target == null) {
            throw new ServiceException("监控目标不存在");
        }

        JMXConnector connector = null;
        try {
            String serviceUrl = String.format("service:jmx:rmi:///jndi/rmi://%s:%d/jmxrmi", target.getHost(), target.getPort());
            JMXServiceURL url = new JMXServiceURL(serviceUrl);
            
            Map<String, Object> env = new HashMap<>();
            if (target.getUsername() != null && !target.getUsername().isEmpty()) {
                String[] credentials = new String[] { target.getUsername(), target.getPassword() };
                env.put(JMXConnector.CREDENTIALS, credentials);
            }

            connector = JMXConnectorFactory.connect(url, env);
            MBeanServerConnection mbsc = connector.getMBeanServerConnection();
            
            MemoryMXBean memoryBean = ManagementFactory.newPlatformMXBeanProxy(mbsc, ManagementFactory.MEMORY_MXBEAN_NAME, MemoryMXBean.class);
            memoryBean.gc();
            log.info("手动触发 GC 成功: targetId={}", targetId);

        } catch (Exception e) {
            log.error("触发 GC 失败: targetId={}", targetId, e);
            throw new ServiceException("触发 GC 失败: " + e.getMessage());
        } finally {
            if (connector != null) {
                try {
                    connector.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }

    @Override
    public List<JvmAlertRule> selectJvmAlertRuleList(JvmAlertRule jvmAlertRule) {
        return jvmAlertRuleMapper.selectJvmAlertRuleList(jvmAlertRule);
    }

    @Override
    public int insertJvmAlertRule(JvmAlertRule jvmAlertRule) {
        return jvmAlertRuleMapper.insertJvmAlertRule(jvmAlertRule);
    }

    @Override
    public int updateJvmAlertRule(JvmAlertRule jvmAlertRule) {
        return jvmAlertRuleMapper.updateJvmAlertRule(jvmAlertRule);
    }

    @Override
    public int deleteJvmAlertRuleByIds(Long[] ruleIds) {
        return jvmAlertRuleMapper.deleteJvmAlertRuleByIds(ruleIds);
    }
}
