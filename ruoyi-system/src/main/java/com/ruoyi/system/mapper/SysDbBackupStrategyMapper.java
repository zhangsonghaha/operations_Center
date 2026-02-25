package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.SysDbBackupStrategy;

/**
 * 数据库备份策略Mapper接口
 * 
 * @author ruoyi
 */
public interface SysDbBackupStrategyMapper 
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
     * 删除数据库备份策略
     * 
     * @param strategyId 数据库备份策略主键
     * @return 结果
     */
    public int deleteSysDbBackupStrategyByStrategyId(Long strategyId);

    /**
     * 批量删除数据库备份策略
     * 
     * @param strategyIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteSysDbBackupStrategyByStrategyIds(Long[] strategyIds);
}
