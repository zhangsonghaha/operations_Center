package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.SysDbBackup;

/**
 * 数据库备份记录Mapper接口
 * 
 * @author ruoyi
 */
public interface SysDbBackupMapper 
{
    /**
     * 查询数据库备份记录
     * 
     * @param backupId 数据库备份记录主键
     * @return 数据库备份记录
     */
    public SysDbBackup selectSysDbBackupByBackupId(Long backupId);

    /**
     * 查询数据库备份记录列表
     * 
     * @param sysDbBackup 数据库备份记录
     * @return 数据库备份记录集合
     */
    public List<SysDbBackup> selectSysDbBackupList(SysDbBackup sysDbBackup);

    /**
     * 新增数据库备份记录
     * 
     * @param sysDbBackup 数据库备份记录
     * @return 结果
     */
    public int insertSysDbBackup(SysDbBackup sysDbBackup);

    /**
     * 修改数据库备份记录
     * 
     * @param sysDbBackup 数据库备份记录
     * @return 结果
     */
    public int updateSysDbBackup(SysDbBackup sysDbBackup);

    /**
     * 删除数据库备份记录
     * 
     * @param backupId 数据库备份记录主键
     * @return 结果
     */
    public int deleteSysDbBackupByBackupId(Long backupId);

    /**
     * 批量删除数据库备份记录
     * 
     * @param backupIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteSysDbBackupByBackupIds(Long[] backupIds);
}
