package com.ruoyi.system.mapper.monitor;

import java.util.List;
import com.ruoyi.system.domain.monitor.JvmAlertRule;

/**
 * JVM 告警规则 Mapper 接口
 * 
 * @author ruoyi
 */
public interface JvmAlertRuleMapper 
{
    /**
     * 查询告警规则
     * 
     * @param ruleId 告警规则ID
     * @return 告警规则
     */
    public JvmAlertRule selectJvmAlertRuleById(Long ruleId);

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
     * 删除告警规则
     * 
     * @param ruleId 告警规则ID
     * @return 结果
     */
    public int deleteJvmAlertRuleById(Long ruleId);

    /**
     * 批量删除告警规则
     * 
     * @param ruleIds 需要删除的数据ID
     * @return 结果
     */
    public int deleteJvmAlertRuleByIds(Long[] ruleIds);
}
