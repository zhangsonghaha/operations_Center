package com.ruoyi.system.service.monitor;

import java.util.List;
import com.ruoyi.system.domain.monitor.JvmAlertRule;
import com.ruoyi.system.domain.monitor.JvmMetric;
import com.ruoyi.system.domain.monitor.JvmTarget;

/**
 * JVM 监控服务接口
 * 
 * @author ruoyi
 */
public interface IJvmMonitorService 
{
    /**
     * 查询监控目标
     * 
     * @param targetId 监控目标ID
     * @return 监控目标
     */
    public JvmTarget selectJvmTargetById(Long targetId);

    /**
     * 查询监控目标列表
     * 
     * @param jvmTarget 监控目标
     * @return 监控目标集合
     */
    public List<JvmTarget> selectJvmTargetList(JvmTarget jvmTarget);

    /**
     * 新增监控目标
     * 
     * @param jvmTarget 监控目标
     * @return 结果
     */
    public int insertJvmTarget(JvmTarget jvmTarget);

    /**
     * 修改监控目标
     * 
     * @param jvmTarget 监控目标
     * @return 结果
     */
    public int updateJvmTarget(JvmTarget jvmTarget);

    /**
     * 批量删除监控目标
     * 
     * @param targetIds 需要删除的监控目标ID
     * @return 结果
     */
    public int deleteJvmTargetByIds(Long[] targetIds);

    /**
     * 删除监控目标信息
     * 
     * @param targetId 监控目标ID
     * @return 结果
     */
    public int deleteJvmTargetById(Long targetId);

    /**
     * 采集指定目标的监控数据并保存
     * 
     * @param targetId 目标ID
     * @return 采集结果
     */
    public JvmMetric collectAndSaveMetrics(Long targetId);

    /**
     * 获取指定目标的最新监控数据
     * 
     * @param targetId 目标ID
     * @return 监控数据
     */
    public JvmMetric getLatestMetric(Long targetId);

    /**
     * 获取历史监控数据
     * 
     * @param jvmMetric 查询条件
     * @return 监控数据列表
     */
    public List<JvmMetric> selectJvmMetricList(JvmMetric jvmMetric);

    /**
     * 触发指定目标的 GC
     * 
     * @param targetId 目标ID
     */
    public void triggerGc(Long targetId);

    // --- 告警规则相关 ---

    /**
     * 查询告警规则列表
     * 
     * @param jvmAlertRule 告警规则
     * @return 告警规则集合
     */
    public List<JvmAlertRule> selectJvmAlertRuleList(JvmAlertRule jvmAlertRule);

    /**
     * 新增告警规则
     * 
     * @param jvmAlertRule 告警规则
     * @return 结果
     */
    public int insertJvmAlertRule(JvmAlertRule jvmAlertRule);

    /**
     * 修改告警规则
     * 
     * @param jvmAlertRule 告警规则
     * @return 结果
     */
    public int updateJvmAlertRule(JvmAlertRule jvmAlertRule);

    /**
     * 批量删除告警规则
     * 
     * @param ruleIds 需要删除的告警规则ID
     * @return 结果
     */
    public int deleteJvmAlertRuleByIds(Long[] ruleIds);
}
