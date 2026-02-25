package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.SysDbBackupLog;

/**
 * 数据库备份日志Service接口
 * 
 * @author ruoyi
 */
public interface ISysDbBackupLogService 
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
     * 批量删除数据库备份日志
     * 
     * @param logIds 需要删除的数据库备份日志主键集合
     * @return 结果
     */
    public int deleteSysDbBackupLogByLogIds(Long[] logIds);

    /**
     * 删除数据库备份日志信息
     * 
     * @param logId 数据库备份日志主键
     * @return 结果
     */
    public int deleteSysDbBackupLogByLogId(Long logId);

    /**
     * 记录备份开始日志
     * 
     * @param backupId 备份ID
     * @param strategyId 策略ID
     * @param connId 连接ID
     * @param dbType 数据库类型
     * @param operationType 操作类型
     * @return 日志ID
     */
    public Long recordStartLog(Long backupId, Long strategyId, Long connId, String dbType, String operationType);

    /**
     * 记录备份结束日志
     * 
     * @param logId 日志ID
     * @param status 状态
     * @param message 消息
     * @param errorMsg 错误消息
     * @param fileSize 文件大小
     */
    public void recordEndLog(Long logId, String status, String message, String errorMsg, Long fileSize);
}
