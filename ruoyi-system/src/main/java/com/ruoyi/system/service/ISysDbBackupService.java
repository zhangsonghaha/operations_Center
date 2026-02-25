package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.SysDbBackup;
import com.ruoyi.system.service.backup.RestoreProgressCallback;

/**
 * 数据库备份记录Service接口
 * 
 * @author ruoyi
 */
public interface ISysDbBackupService 
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
     * 批量删除数据库备份记录
     * 
     * @param backupIds 需要删除的数据库备份记录主键集合
     * @return 结果
     */
    public int deleteSysDbBackupByBackupIds(Long[] backupIds);

    /**
     * 删除数据库备份记录信息
     * 
     * @param backupId 数据库备份记录主键
     * @return 结果
     */
    public int deleteSysDbBackupByBackupId(Long backupId);

    /**
     * 执行备份
     * @param connId 连接ID
     * @return 备份结果
     */
    public void backup(Long connId) throws Exception;

    /**
     * 执行备份（高级选项）
     * @param connId 连接ID
     * @param dbType 数据库类型
     * @param backupMode 备份方式
     * @param backupLevel 备份级别
     * @param targetName 备份目标
     * @param storageType 存储类型
     * @param compressEnabled 是否压缩
     * @return 备份结果
     */
    public SysDbBackup backupWithOptions(Long connId, String dbType, String backupMode, 
                                         String backupLevel, String targetName, 
                                         String storageType, String compressEnabled) throws Exception;

    /**
     * 验证备份文件
     * @param backupId 备份ID
     * @return 验证结果
     */
    public boolean verifyBackup(Long backupId);

    /**
     * 清理过期备份
     * @param retentionDays 保留天数
     * @return 清理数量
     */
    public int cleanExpiredBackups(Integer retentionDays);

    /**
     * 恢复备份
     * @param backupId 备份ID
     * @param targetConnId 目标连接ID
     * @return 恢复结果
     */
    public boolean restoreBackup(Long backupId, Long targetConnId) throws Exception;

    /**
     * 恢复备份（带进度回调）
     * @param backupId 备份ID
     * @param targetConnId 目标连接ID
     * @param callback 进度回调
     * @return 恢复结果
     */
    public boolean restoreBackupWithProgress(Long backupId, Long targetConnId, RestoreProgressCallback callback) throws Exception;
}
