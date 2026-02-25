package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.SysDbLog;

/**
 * 数据库操作日志Service接口
 * 
 * @author ruoyi
 */
public interface ISysDbLogService 
{
    /**
     * 查询数据库操作日志
     * 
     * @param logId 数据库操作日志主键
     * @return 数据库操作日志
     */
    public SysDbLog selectSysDbLogByLogId(Long logId);

    /**
     * 查询数据库操作日志列表
     * 
     * @param sysDbLog 数据库操作日志
     * @return 数据库操作日志集合
     */
    public List<SysDbLog> selectSysDbLogList(SysDbLog sysDbLog);

    /**
     * 新增数据库操作日志
     * 
     * @param sysDbLog 数据库操作日志
     * @return 结果
     */
    public int insertSysDbLog(SysDbLog sysDbLog);

    /**
     * 删除数据库操作日志
     * 
     * @param logId 数据库操作日志主键
     * @return 结果
     */
    public int deleteSysDbLogByLogId(Long logId);

    /**
     * 批量删除数据库操作日志
     * 
     * @param logIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteSysDbLogByLogIds(Long[] logIds);
}
