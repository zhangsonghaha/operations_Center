package com.ruoyi.system.service.impl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.system.domain.SysDbBackup;
import com.ruoyi.system.domain.SysDbConn;
import com.ruoyi.system.mapper.SysDbBackupMapper;
import com.ruoyi.system.mapper.SysDbConnMapper;
import com.ruoyi.system.service.IDbExecuteService;
import com.ruoyi.system.service.ISysDbBackupService;

/**
 * 数据库备份记录Service业务层处理
 * 
 * @author ruoyi
 */
@Service
public class SysDbBackupServiceImpl implements ISysDbBackupService 
{
    @Autowired
    private SysDbBackupMapper sysDbBackupMapper;

    @Autowired
    private SysDbConnMapper sysDbConnMapper;

    @Autowired
    private IDbExecuteService dbExecuteService;

    /**
     * 查询数据库备份记录
     * 
     * @param backupId 数据库备份记录主键
     * @return 数据库备份记录
     */
    @Override
    public SysDbBackup selectSysDbBackupByBackupId(Long backupId)
    {
        return sysDbBackupMapper.selectSysDbBackupByBackupId(backupId);
    }

    /**
     * 查询数据库备份记录列表
     * 
     * @param sysDbBackup 数据库备份记录
     * @return 数据库备份记录
     */
    @Override
    public List<SysDbBackup> selectSysDbBackupList(SysDbBackup sysDbBackup)
    {
        return sysDbBackupMapper.selectSysDbBackupList(sysDbBackup);
    }

    /**
     * 新增数据库备份记录
     * 
     * @param sysDbBackup 数据库备份记录
     * @return 结果
     */
    @Override
    public int insertSysDbBackup(SysDbBackup sysDbBackup)
    {
        sysDbBackup.setCreateTime(DateUtils.getNowDate());
        return sysDbBackupMapper.insertSysDbBackup(sysDbBackup);
    }

    /**
     * 修改数据库备份记录
     * 
     * @param sysDbBackup 数据库备份记录
     * @return 结果
     */
    @Override
    public int updateSysDbBackup(SysDbBackup sysDbBackup)
    {
        return sysDbBackupMapper.updateSysDbBackup(sysDbBackup);
    }

    /**
     * 批量删除数据库备份记录
     * 
     * @param backupIds 需要删除的数据库备份记录主键
     * @return 结果
     */
    @Override
    public int deleteSysDbBackupByBackupIds(Long[] backupIds)
    {
        return sysDbBackupMapper.deleteSysDbBackupByBackupIds(backupIds);
    }

    /**
     * 删除数据库备份记录信息
     * 
     * @param backupId 数据库备份记录主键
     * @return 结果
     */
    @Override
    public int deleteSysDbBackupByBackupId(Long backupId)
    {
        return sysDbBackupMapper.deleteSysDbBackupByBackupId(backupId);
    }

    @Override
    public void backup(Long connId) throws Exception
    {
        SysDbConn connInfo = sysDbConnMapper.selectSysDbConnByConnId(connId);
        if (connInfo == null)
        {
            throw new Exception("数据库连接不存在");
        }

        // 生成文件名
        String fileName = connInfo.getDbName() + "_" + DateUtils.dateTimeNow() + ".sql";
        String filePath = RuoYiConfig.getProfile() + "/backup/" + fileName;
        File file = new File(filePath);
        if (!file.getParentFile().exists())
        {
            file.getParentFile().mkdirs();
        }

        // 开始备份
        try (FileWriter writer = new FileWriter(file);
             Connection conn = DriverManager.getConnection(
                 "jdbc:mysql://" + connInfo.getHost() + ":" + connInfo.getPort() + "/" + connInfo.getDbName() 
                 + "?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=GMT%2B8", 
                 connInfo.getUsername(), connInfo.getPassword()))
        {
            // 获取所有表
            List<String> tables = dbExecuteService.getTableList(connId);

            for (String table : tables)
            {
                // 1. 导出表结构
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SHOW CREATE TABLE " + table))
                {
                    if (rs.next())
                    {
                        writer.write("-- Table structure for " + table + "\n");
                        writer.write("DROP TABLE IF EXISTS `" + table + "`;\n");
                        writer.write(rs.getString(2) + ";\n\n");
                    }
                }

                // 2. 导出表数据
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery("SELECT * FROM " + table))
                {
                    ResultSetMetaData metaData = rs.getMetaData();
                    int columnCount = metaData.getColumnCount();

                    writer.write("-- Dumping data for table " + table + "\n");
                    while (rs.next())
                    {
                        StringBuilder insert = new StringBuilder("INSERT INTO `" + table + "` VALUES (");
                        for (int i = 1; i <= columnCount; i++)
                        {
                            Object value = rs.getObject(i);
                            if (value == null)
                            {
                                insert.append("NULL");
                            }
                            else if (value instanceof String || value instanceof java.sql.Date || value instanceof java.sql.Timestamp)
                            {
                                insert.append("'").append(value.toString().replace("'", "\\'")).append("'");
                            }
                            else
                            {
                                insert.append(value);
                            }
                            if (i < columnCount)
                            {
                                insert.append(", ");
                            }
                        }
                        insert.append(");\n");
                        writer.write(insert.toString());
                    }
                    writer.write("\n");
                }
            }
            
            // 记录备份信息
            SysDbBackup backup = new SysDbBackup();
            backup.setConnId(connId);
            backup.setFileName(fileName);
            backup.setFilePath(filePath);
            backup.setBackupType("0"); // 手动
            backup.setStatus("0"); // 成功
            backup.setCreateBy(SecurityUtils.getUsername());
            backup.setCreateTime(DateUtils.getNowDate());
            sysDbBackupMapper.insertSysDbBackup(backup);
        }
        catch (Exception e)
        {
            // 记录失败日志
            SysDbBackup backup = new SysDbBackup();
            backup.setConnId(connId);
            backup.setFileName(fileName);
            backup.setFilePath(filePath);
            backup.setBackupType("0");
            backup.setStatus("1"); // 失败
            backup.setLogMsg(e.getMessage());
            backup.setCreateBy(SecurityUtils.getUsername());
            backup.setCreateTime(DateUtils.getNowDate());
            sysDbBackupMapper.insertSysDbBackup(backup);
            throw e;
        }
    }
}
