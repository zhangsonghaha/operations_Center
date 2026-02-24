package com.ruoyi.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.mapper.SysDbLogMapper;
import com.ruoyi.system.domain.SysDbLog;
import com.ruoyi.system.service.ISysDbLogService;

/**
 * 数据库操作日志Service业务层处理
 * 
 * @author ruoyi
 */
@Service
public class SysDbLogServiceImpl implements ISysDbLogService 
{
    @Autowired
    private SysDbLogMapper sysDbLogMapper;

    /**
     * 查询数据库操作日志
     * 
     * @param logId 数据库操作日志主键
     * @return 数据库操作日志
     */
    @Override
    public SysDbLog selectSysDbLogByLogId(Long logId)
    {
        return sysDbLogMapper.selectSysDbLogByLogId(logId);
    }

    /**
     * 查询数据库操作日志列表
     * 
     * @param sysDbLog 数据库操作日志
     * @return 数据库操作日志
     */
    @Override
    public List<SysDbLog> selectSysDbLogList(SysDbLog sysDbLog)
    {
        return sysDbLogMapper.selectSysDbLogList(sysDbLog);
    }

    /**
     * 新增数据库操作日志
     * 
     * @param sysDbLog 数据库操作日志
     * @return 结果
     */
    @Override
    public int insertSysDbLog(SysDbLog sysDbLog)
    {
        return sysDbLogMapper.insertSysDbLog(sysDbLog);
    }
}
