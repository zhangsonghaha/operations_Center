package com.ruoyi.system.service.impl;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.system.domain.SysDbBackupLog;
import com.ruoyi.system.mapper.SysDbBackupLogMapper;
import com.ruoyi.system.service.ISysDbBackupLogService;

/**
 * 数据库备份日志Service业务层处理
 * 
 * @author ruoyi
 */
@Service
public class SysDbBackupLogServiceImpl implements ISysDbBackupLogService 
{
    @Autowired
    private SysDbBackupLogMapper logMapper;

    @Override
    public SysDbBackupLog selectSysDbBackupLogByLogId(Long logId)
    {
        return logMapper.selectSysDbBackupLogByLogId(logId);
    }

    @Override
    public List<SysDbBackupLog> selectSysDbBackupLogList(SysDbBackupLog log)
    {
        return logMapper.selectSysDbBackupLogList(log);
    }

    @Override
    public int insertSysDbBackupLog(SysDbBackupLog log)
    {
        log.setCreateTime(DateUtils.getNowDate());
        return logMapper.insertSysDbBackupLog(log);
    }

    @Override
    public int updateSysDbBackupLog(SysDbBackupLog log)
    {
        return logMapper.updateSysDbBackupLog(log);
    }

    @Override
    public int deleteSysDbBackupLogByLogIds(Long[] logIds)
    {
        return logMapper.deleteSysDbBackupLogByLogIds(logIds);
    }

    @Override
    public int deleteSysDbBackupLogByLogId(Long logId)
    {
        return logMapper.deleteSysDbBackupLogByLogId(logId);
    }

    @Override
    public Long recordStartLog(Long backupId, Long strategyId, Long connId, String dbType, String operationType)
    {
        SysDbBackupLog log = new SysDbBackupLog();
        log.setBackupId(backupId);
        log.setStrategyId(strategyId);
        log.setConnId(connId);
        log.setDbType(dbType);
        log.setOperationType(operationType);
        log.setOperationStatus("2"); // 进行中
        log.setStartTime(new Date());
        log.setCreateBy(SecurityUtils.getUsername());
        log.setCreateTime(DateUtils.getNowDate());
        
        logMapper.insertSysDbBackupLog(log);
        return log.getLogId();
    }

    @Override
    public void recordEndLog(Long logId, String status, String message, String errorMsg, Long fileSize)
    {
        SysDbBackupLog log = new SysDbBackupLog();
        log.setLogId(logId);
        log.setOperationStatus(status);
        log.setEndTime(new Date());
        log.setMessage(message);
        log.setErrorMsg(errorMsg);
        log.setFileSize(fileSize);
        
        // 计算耗时
        SysDbBackupLog existingLog = logMapper.selectSysDbBackupLogByLogId(logId);
        if (existingLog != null && existingLog.getStartTime() != null)
        {
            long duration = (System.currentTimeMillis() - existingLog.getStartTime().getTime()) / 1000;
            log.setDuration((int) duration);
        }
        
        logMapper.updateSysDbBackupLog(log);
    }
}
