package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.SysDbBackupLog;

/**
 * 数据库备份日志Mapper接口
 * 
 * @author ruoyi
 */
public interface SysDbBackupLogMapper 
{
    /**
     * 查询数据库备份日志
     * 
     * @param logId 数据库备份日志主键
     * @return 数据库备份日志
     */
    public SysDbBackupLog selectSysDbBackupLogByLogId(Long logId);

    /**
     * 查询数据库备份日志列表
     * 
     * @param sysDbBackupLog 数据库备份日志
     * @return 数据库备份日志集合
     */
    public List<SysDbBackupLog> selectSysDbBackupLogList(SysDbBackupLog sysDbBackupLog);

    /**
     * 新增数据库备份日志
     * 
     * @param sysDbBackupLog 数据库备份日志
     * @return 结果
     */
    public int insertSysDbBackupLog(SysDbBackupLog sysDbBackupLog);

    /**
     * 修改数据库备份日志
     * 
     * @param sysDbBackupLog 数据库备份日志
     * @return 结果
     */
    public int updateSysDbBackupLog(SysDbBackupLog sysDbBackupLog);

    /**
     * 删除数据库备份日志
     * 
     * @param logId 数据库备份日志主键
     * @return 结果
     */
    public int deleteSysDbBackupLogByLogId(Long logId);

    /**
     * 批量删除数据库备份日志
     * 
     * @param logIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteSysDbBackupLogByLogIds(Long[] logIds);
}
