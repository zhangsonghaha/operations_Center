package com.ruoyi.system.service;

import java.util.List;
import java.util.Map;
import com.ruoyi.system.domain.monitor.DbMonitorRule;

/**
 * 数据库监控Service接口
 * 
 * @author ruoyi
 */
public interface ISysDbMonitorService 
{
    /**
     * 获取实时连接列表
     * 
     * @param connId 连接ID
     * @return 连接列表
     */
    public List<Map<String, Object>> getProcessList(Long connId);

    /**
     * 获取表空间统计
     * 
     * @param connId 连接ID
     * @return 表统计列表
     */
    public List<Map<String, Object>> getTableStats(Long connId);

    /**
     * 获取慢查询分析
     * 
     * @param connId 连接ID
     * @param limit 限制条数
     * @return 慢查询列表
     */
    public List<Map<String, Object>> getSlowQueries(Long connId, Integer limit);

    /**
     * 获取SQL执行统计
     * 
     * @param connId 连接ID
     * @param limit 限制条数
     * @return SQL统计列表
     */
    public List<Map<String, Object>> getSqlStats(Long connId, Integer limit);

    /**
     * 终止数据库连接
     * 
     * @param connId 连接ID
     * @param processId 进程ID
     * @return 结果
     */
    public int killProcess(Long connId, Long processId);

    /**
     * 获取连接数统计
     * 
     * @param connId 连接ID
     * @return 连接统计信息
     */
    public Map<String, Object> getConnectionStats(Long connId);

    // ==================== 监控规则管理 ====================

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
