package com.ruoyi.system.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.domain.monitor.DbMonitorRule;
import com.ruoyi.system.mapper.monitor.DbMonitorRuleMapper;
import com.ruoyi.system.service.IDbExecuteService;
import com.ruoyi.system.service.ISysDbMonitorService;

/**
 * 数据库监控Service业务层处理
 * 
 * @author ruoyi
 */
@Service
public class SysDbMonitorServiceImpl implements ISysDbMonitorService 
{
    @Autowired
    private IDbExecuteService dbExecuteService;

    @Autowired
    private DbMonitorRuleMapper dbMonitorRuleMapper;

    /**
     * 获取实时连接列表
     * 
     * @param connId 连接ID
     * @return 连接列表
     */
    @Override
    public List<Map<String, Object>> getProcessList(Long connId)
    {
        return dbExecuteService.executeSelect(connId, "SHOW FULL PROCESSLIST");
    }

    /**
     * 获取表空间统计
     * 
     * @param connId 连接ID
     * @return 表统计列表
     */
    @Override
    public List<Map<String, Object>> getTableStats(Long connId)
    {
        String sql = "SELECT table_name, table_comment, engine, table_rows, data_length, index_length, create_time, " +
                     "(data_length + index_length) as total_length " +
                     "FROM information_schema.tables WHERE table_schema = DATABASE() ORDER BY data_length DESC";
        return dbExecuteService.executeSelect(connId, sql);
    }

    /**
     * 获取慢查询分析
     * 
     * @param connId 连接ID
     * @param limit 限制条数
     * @return 慢查询列表
     */
    @Override
    public List<Map<String, Object>> getSlowQueries(Long connId, Integer limit)
    {
        if (limit == null || limit <= 0) {
            limit = 20;
        }
        // 首先尝试从sys schema获取（MySQL 5.7+）
        String sysSql = "SELECT query, db, exec_count, total_latency, avg_latency, max_latency, " +
                       "rows_sent_avg, rows_examined_avg, first_seen, last_seen " +
                       "FROM sys.statements_with_runtimes_in_95th_percentile " +
                       "ORDER BY total_latency DESC LIMIT " + limit;
        
        try {
            List<Map<String, Object>> result = dbExecuteService.executeSelect(connId, sysSql);
            if (result != null && !result.isEmpty()) {
                return result;
            }
        } catch (Exception e) {
            // sys schema不可用，使用performance_schema
        }
        
        // 使用performance_schema.events_statements_summary_by_digest
        String psSql = "SELECT DIGEST_TEXT as query, SCHEMA_NAME as db, COUNT_STAR as exec_count, " +
                      "ROUND(SUM_TIMER_WAIT/1000000000000, 2) as total_latency, " +
                      "ROUND(AVG_TIMER_WAIT/1000000000000, 2) as avg_latency, " +
                      "ROUND(MAX_TIMER_WAIT/1000000000000, 2) as max_latency, " +
                      "ROUND(SUM_ROWS_SENT/COUNT_STAR, 0) as rows_sent_avg, " +
                      "ROUND(SUM_ROWS_EXAMINED/COUNT_STAR, 0) as rows_examined_avg, " +
                      "FIRST_SEEN as first_seen, LAST_SEEN as last_seen " +
                      "FROM performance_schema.events_statements_summary_by_digest " +
                      "WHERE SCHEMA_NAME IS NOT NULL " +
                      "ORDER BY SUM_TIMER_WAIT DESC LIMIT " + limit;
        
        try {
            return dbExecuteService.executeSelect(connId, psSql);
        } catch (Exception e) {
            // performance_schema不可用，返回空列表
            return new ArrayList<>();
        }
    }

    /**
     * 获取SQL执行统计
     * 
     * @param connId 连接ID
     * @param limit 限制条数
     * @return SQL统计列表
     */
    @Override
    public List<Map<String, Object>> getSqlStats(Long connId, Integer limit)
    {
        if (limit == null || limit <= 0) {
            limit = 20;
        }
        
        // 首先尝试从sys schema获取
        String sysSql = "SELECT query, db, exec_count, errors, warnings, total_latency, " +
                       "avg_latency, rows_sent, rows_examined, tmp_tables, tmp_disk_tables " +
                       "FROM sys.statement_analysis " +
                       "ORDER BY total_latency DESC LIMIT " + limit;
        
        try {
            List<Map<String, Object>> result = dbExecuteService.executeSelect(connId, sysSql);
            if (result != null && !result.isEmpty()) {
                return result;
            }
        } catch (Exception e) {
            // sys schema不可用
        }
        
        // 使用performance_schema
        String psSql = "SELECT DIGEST_TEXT as query, SCHEMA_NAME as db, COUNT_STAR as exec_count, " +
                      "SUM_ERRORS as errors, SUM_WARNINGS as warnings, " +
                      "ROUND(SUM_TIMER_WAIT/1000000000000, 2) as total_latency, " +
                      "ROUND(AVG_TIMER_WAIT/1000000000000, 2) as avg_latency, " +
                      "SUM_ROWS_SENT as rows_sent, SUM_ROWS_EXAMINED as rows_examined, " +
                      "SUM_CREATED_TMP_TABLES as tmp_tables, SUM_CREATED_TMP_DISK_TABLES as tmp_disk_tables " +
                      "FROM performance_schema.events_statements_summary_by_digest " +
                      "WHERE SCHEMA_NAME IS NOT NULL " +
                      "ORDER BY SUM_TIMER_WAIT DESC LIMIT " + limit;
        
        try {
            return dbExecuteService.executeSelect(connId, psSql);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    /**
     * 终止数据库连接
     * 
     * @param connId 连接ID
     * @param processId 进程ID
     * @return 结果
     */
    @Override
    public int killProcess(Long connId, Long processId)
    {
        String sql = "KILL " + processId;
        return dbExecuteService.executeUpdate(connId, sql);
    }

    /**
     * 获取连接数统计
     * 
     * @param connId 连接ID
     * @return 连接统计信息
     */
    @Override
    public Map<String, Object> getConnectionStats(Long connId)
    {
        Map<String, Object> stats = new HashMap<>();
        
        // 总连接数
        String totalSql = "SELECT COUNT(*) as total FROM information_schema.PROCESSLIST";
        List<Map<String, Object>> totalResult = dbExecuteService.executeSelect(connId, totalSql);
        int totalConnections = totalResult.isEmpty() ? 0 : ((Number) totalResult.get(0).get("total")).intValue();
        stats.put("totalConnections", totalConnections);
        
        // 活跃连接数（非Sleep状态）
        String activeSql = "SELECT COUNT(*) as active FROM information_schema.PROCESSLIST WHERE COMMAND != 'Sleep'";
        List<Map<String, Object>> activeResult = dbExecuteService.executeSelect(connId, activeSql);
        int activeConnections = activeResult.isEmpty() ? 0 : ((Number) activeResult.get(0).get("active")).intValue();
        stats.put("activeConnections", activeConnections);
        
        // 最大连接数配置
        String maxSql = "SHOW VARIABLES LIKE 'max_connections'";
        try {
            List<Map<String, Object>> maxResult = dbExecuteService.executeSelect(connId, maxSql);
            if (!maxResult.isEmpty()) {
                stats.put("maxConnections", maxResult.get(0).get("Value"));
            }
        } catch (Exception e) {
            stats.put("maxConnections", 0);
        }
        
        // 慢查询数量（执行时间>2秒）
        String slowSql = "SELECT COUNT(*) as slow FROM information_schema.PROCESSLIST WHERE TIME > 2 AND COMMAND != 'Sleep'";
        try {
            List<Map<String, Object>> slowResult = dbExecuteService.executeSelect(connId, slowSql);
            int slowQueries = slowResult.isEmpty() ? 0 : ((Number) slowResult.get(0).get("slow")).intValue();
            stats.put("slowQueries", slowQueries);
        } catch (Exception e) {
            stats.put("slowQueries", 0);
        }
        
        return stats;
    }

    // ==================== 监控规则管理 ====================

    /**
     * 查询监控规则
     */
    @Override
    public DbMonitorRule selectDbMonitorRuleById(Long ruleId)
    {
        return dbMonitorRuleMapper.selectDbMonitorRuleById(ruleId);
    }

    /**
     * 查询监控规则列表
     */
    @Override
    public List<DbMonitorRule> selectDbMonitorRuleList(DbMonitorRule dbMonitorRule)
    {
        return dbMonitorRuleMapper.selectDbMonitorRuleList(dbMonitorRule);
    }

    /**
     * 新增监控规则
     */
    @Override
    public int insertDbMonitorRule(DbMonitorRule dbMonitorRule)
    {
        return dbMonitorRuleMapper.insertDbMonitorRule(dbMonitorRule);
    }

    /**
     * 修改监控规则
     */
    @Override
    public int updateDbMonitorRule(DbMonitorRule dbMonitorRule)
    {
        return dbMonitorRuleMapper.updateDbMonitorRule(dbMonitorRule);
    }

    /**
     * 删除监控规则
     */
    @Override
    public int deleteDbMonitorRuleById(Long ruleId)
    {
        return dbMonitorRuleMapper.deleteDbMonitorRuleById(ruleId);
    }

    /**
     * 批量删除监控规则
     */
    @Override
    public int deleteDbMonitorRuleByIds(Long[] ruleIds)
    {
        return dbMonitorRuleMapper.deleteDbMonitorRuleByIds(ruleIds);
    }
}
