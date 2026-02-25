package com.ruoyi.system.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.system.domain.SysDbBackup;
import com.ruoyi.system.domain.SysDbBackupStrategy;
import com.ruoyi.system.domain.SysDbConn;
import com.ruoyi.system.mapper.SysDbBackupMapper;
import com.ruoyi.system.mapper.SysDbBackupStrategyMapper;
import com.ruoyi.system.mapper.SysDbConnMapper;
import com.ruoyi.system.service.ISysDbBackupLogService;
import com.ruoyi.system.service.ISysDbBackupStrategyService;
import com.ruoyi.system.service.backup.IDbBackupAdapter;

/**
 * 数据库备份策略Service业务层处理
 * 
 * @author ruoyi
 */
@Service
public class SysDbBackupStrategyServiceImpl implements ISysDbBackupStrategyService 
{
    @Autowired
    private SysDbBackupStrategyMapper strategyMapper;

    @Autowired
    private SysDbBackupMapper backupMapper;

    @Autowired
    private SysDbConnMapper connMapper;

    @Autowired
    private ISysDbBackupLogService logService;

    @Autowired
    private List<IDbBackupAdapter> backupAdapters;

    @Override
    public SysDbBackupStrategy selectSysDbBackupStrategyByStrategyId(Long strategyId)
    {
        return strategyMapper.selectSysDbBackupStrategyByStrategyId(strategyId);
    }

    @Override
    public List<SysDbBackupStrategy> selectSysDbBackupStrategyList(SysDbBackupStrategy strategy)
    {
        return strategyMapper.selectSysDbBackupStrategyList(strategy);
    }

    @Override
    public List<SysDbBackupStrategy> selectEnabledStrategyList()
    {
        return strategyMapper.selectEnabledStrategyList();
    }

    @Override
    public int insertSysDbBackupStrategy(SysDbBackupStrategy strategy)
    {
        strategy.setCreateTime(DateUtils.getNowDate());
        return strategyMapper.insertSysDbBackupStrategy(strategy);
    }

    @Override
    public int updateSysDbBackupStrategy(SysDbBackupStrategy strategy)
    {
        strategy.setUpdateTime(DateUtils.getNowDate());
        return strategyMapper.updateSysDbBackupStrategy(strategy);
    }

    @Override
    public int deleteSysDbBackupStrategyByStrategyIds(Long[] strategyIds)
    {
        return strategyMapper.deleteSysDbBackupStrategyByStrategyIds(strategyIds);
    }

    @Override
    public int deleteSysDbBackupStrategyByStrategyId(Long strategyId)
    {
        return strategyMapper.deleteSysDbBackupStrategyByStrategyId(strategyId);
    }

    @Override
    public void executeBackupByStrategy(Long strategyId) throws Exception
    {
        SysDbBackupStrategy strategy = strategyMapper.selectSysDbBackupStrategyByStrategyId(strategyId);
        if (strategy == null)
        {
            throw new Exception("备份策略不存在");
        }

        SysDbConn conn = connMapper.selectSysDbConnByConnId(strategy.getConnId());
        if (conn == null)
        {
            throw new Exception("数据库连接不存在");
        }

        // 查找对应的备份适配器
        IDbBackupAdapter adapter = getAdapter(strategy.getDbType());
        if (adapter == null)
        {
            throw new Exception("不支持的数据库类型: " + strategy.getDbType());
        }

        // 记录开始日志
        Long logId = logService.recordStartLog(null, strategyId, strategy.getConnId(), 
                strategy.getDbType(), "BACKUP");

        try {
            // 构建连接配置
            Map<String, String> connConfig = new HashMap<>();
            connConfig.put("host", conn.getHost());
            connConfig.put("port", conn.getPort());
            connConfig.put("username", conn.getUsername());
            connConfig.put("password", conn.getPassword());
            connConfig.put("database", conn.getDbName());

            // 确定备份路径
            String backupPath = getBackupPath();

            // 执行备份
            java.io.File backupFile;
            if ("incremental".equals(strategy.getBackupMode()))
            {
                backupFile = adapter.executeIncrementalBackup(connConfig, backupPath, strategy.getTargetName());
            }
            else
            {
                backupFile = adapter.executeFullBackup(connConfig, backupPath, strategy.getTargetName());
            }

            // 计算MD5
            String md5 = adapter.calculateMd5(backupFile);

            // 保存备份记录
            SysDbBackup backup = new SysDbBackup();
            backup.setConnId(strategy.getConnId());
            backup.setFileName(backupFile.getName());
            backup.setFilePath(backupFile.getAbsolutePath());
            backup.setBackupType("1"); // 自动备份
            backup.setStatus("0"); // 成功
            backup.setDbType(strategy.getDbType());
            backup.setBackupMode(strategy.getBackupMode());
            backup.setBackupLevel(strategy.getBackupLevel());
            backup.setTargetName(strategy.getTargetName());
            backup.setFileSize(backupFile.length());
            backup.setFileMd5(md5);
            backup.setStorageType(strategy.getStorageType());
            backup.setVerifyStatus("0");
            backup.setCreateTime(DateUtils.getNowDate());

            // 计算过期时间
            if (strategy.getRetentionDays() != null)
            {
                java.util.Calendar cal = java.util.Calendar.getInstance();
                cal.add(java.util.Calendar.DAY_OF_MONTH, strategy.getRetentionDays());
                backup.setExpireTime(cal.getTime());
            }

            backupMapper.insertSysDbBackup(backup);

            // 更新日志
            logService.recordEndLog(logId, "0", "备份成功: " + backupFile.getName(), null, backupFile.length());

            // 验证备份（如果启用）
            if ("1".equals(strategy.getCompressEnabled()))
            {
                boolean verified = adapter.verifyBackup(backupFile, connConfig);
                backup.setVerifyStatus(verified ? "1" : "2");
                backup.setVerifyMsg(verified ? "验证成功" : "验证失败");
                backupMapper.updateSysDbBackup(backup);
            }

            // 清理过期备份
            cleanExpiredBackups(strategy.getRetentionDays(), strategy.getRetentionCount());

        } catch (Exception e) {
            logService.recordEndLog(logId, "1", null, e.getMessage(), null);
            throw e;
        }
    }

    /**
     * 获取备份适配器
     */
    private IDbBackupAdapter getAdapter(String dbType)
    {
        for (IDbBackupAdapter adapter : backupAdapters)
        {
            if (adapter.getDbType().equalsIgnoreCase(dbType))
            {
                return adapter;
            }
        }
        return null;
    }

    /**
     * 获取备份路径
     */
    private String getBackupPath()
    {
        String backupPath = com.ruoyi.common.config.RuoYiConfig.getProfile() + "/backup";
        java.io.File dir = new java.io.File(backupPath);
        if (!dir.exists())
        {
            dir.mkdirs();
        }
        return backupPath;
    }

    /**
     * 清理过期备份
     */
    private void cleanExpiredBackups(Integer retentionDays, Integer retentionCount)
    {
        // 逻辑删除过期备份
        if (retentionDays != null)
        {
            List<SysDbBackup> expiredBackups = backupMapper.selectExpiredBackups();
            for (SysDbBackup backup : expiredBackups)
            {
                backupMapper.logicDeleteSysDbBackup(backup.getBackupId());
            }
        }

        // TODO: 按数量清理
        if (retentionCount != null)
        {
            // 实现保留最近N个备份的逻辑
        }
    }
}
