package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.SysDbBackupStrategy;

/**
 * 数据库备份策略Service接口
 * 
 * @author ruoyi
 */
public interface ISysDbBackupStrategyService 
{
    /**
     * 查询数据库备份策略
     * 
     * @param strategyId 数据库备份策略主键
     * @return 数据库备份策略
     */
    public SysDbBackupStrategy selectSysDbBackupStrategyByStrategyId(Long strategyId);

    /**
     * 查询数据库备份策略列表
     * 
     * @param sysDbBackupStrategy 数据库备份策略
     * @return 数据库备份策略集合
     */
    public List<SysDbBackupStrategy> selectSysDbBackupStrategyList(SysDbBackupStrategy sysDbBackupStrategy);

    /**
     * 查询启用的策略列表
     * 
     * @return 数据库备份策略集合
     */
    public List<SysDbBackupStrategy> selectEnabledStrategyList();

    /**
     * 新增数据库备份策略
     * 
     * @param sysDbBackupStrategy 数据库备份策略
     * @return 结果
     */
    public int insertSysDbBackupStrategy(SysDbBackupStrategy sysDbBackupStrategy);

    /**
     * 修改数据库备份策略
     * 
     * @param sysDbBackupStrategy 数据库备份策略
     * @return 结果
     */
    public int updateSysDbBackupStrategy(SysDbBackupStrategy sysDbBackupStrategy);

    /**
     * 批量删除数据库备份策略
     * 
     * @param strategyIds 需要删除的数据库备份策略主键集合
     * @return 结果
     */
    public int deleteSysDbBackupStrategyByStrategyIds(Long[] strategyIds);

    /**
     * 删除数据库备份策略信息
     * 
     * @param strategyId 数据库备份策略主键
     * @return 结果
     */
    public int deleteSysDbBackupStrategyByStrategyId(Long strategyId);

    /**
     * 根据策略执行备份
     * 
     * @param strategyId 策略ID
     * @return 备份结果
     */
    public void executeBackupByStrategy(Long strategyId) throws Exception;
}
