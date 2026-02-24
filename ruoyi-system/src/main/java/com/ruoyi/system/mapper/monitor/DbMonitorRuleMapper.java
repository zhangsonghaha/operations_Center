package com.ruoyi.system.mapper.monitor;

import java.util.List;
import com.ruoyi.system.domain.monitor.DbMonitorRule;

/**
 * 数据库监控规则Mapper接口
 * 
 * @author ruoyi
 */
public interface DbMonitorRuleMapper 
{
    /**
     * 查询监控规则
     * 
     * @param ruleId 监控规则主键
     * @return 监控规则
     */
    public DbMonitorRule selectDbMonitorRuleById(Long ruleId);

    /**
     * 查询监控规则列表
     * 
     * @param dbMonitorRule 监控规则
     * @return 监控规则集合
     */
    public List<DbMonitorRule> selectDbMonitorRuleList(DbMonitorRule dbMonitorRule);

    /**
     * 新增监控规则
     * 
     * @param dbMonitorRule 监控规则
     * @return 结果
     */
    public int insertDbMonitorRule(DbMonitorRule dbMonitorRule);

    /**
     * 修改监控规则
     * 
     * @param dbMonitorRule 监控规则
     * @return 结果
     */
    public int updateDbMonitorRule(DbMonitorRule dbMonitorRule);

    /**
     * 删除监控规则
     * 
     * @param ruleId 监控规则主键
     * @return 结果
     */
    public int deleteDbMonitorRuleById(Long ruleId);

    /**
     * 批量删除监控规则
     * 
     * @param ruleIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteDbMonitorRuleByIds(Long[] ruleIds);
}
