package com.ruoyi.system.service.impl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.system.domain.SysDbBackup;
import com.ruoyi.system.domain.SysDbConn;
import com.ruoyi.system.domain.SysDbLog;
import com.ruoyi.system.mapper.SysDbBackupMapper;
import com.ruoyi.system.mapper.SysDbConnMapper;
import com.ruoyi.system.service.IDbExecuteService;
import com.ruoyi.system.service.ISysDbBackupService;
import com.ruoyi.system.service.ISysDbLogService;
import com.ruoyi.system.service.backup.IDbBackupAdapter;
import com.ruoyi.system.service.backup.RestoreProgressCallback;

/**
 * 数据库备份记录Service业务层处理
 * 
 * @author ruoyi
 */
@Service
public class SysDbBackupServiceImpl implements ISysDbBackupService 
{
    private static final Logger logger = LoggerFactory.getLogger(SysDbBackupServiceImpl.class);
    @Autowired
    private SysDbBackupMapper sysDbBackupMapper;

    @Autowired
    private SysDbConnMapper sysDbConnMapper;

    @Autowired
    private IDbExecuteService dbExecuteService;

    @Autowired
    private ISysDbLogService sysDbLogService;

    @Autowired
    private List<IDbBackupAdapter> backupAdapters;

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

        // 先生成文件名，确保入库时有值
        String fileName = generateBackupFileName(connInfo.getDbName(), "mysql", "full");
        String filePath = RuoYiConfig.getProfile() + "/backup/" + fileName;
        
        // 预创建备份记录
        SysDbBackup backup = new SysDbBackup();
        backup.setConnId(connId);
        backup.setFileName(fileName);
        backup.setFilePath(filePath);
        backup.setBackupType("0"); // 手动
        backup.setStatus("2"); // 进行中
        backup.setDbType("mysql");
        backup.setBackupMode("full");
        backup.setBackupLevel("database");
        backup.setStorageType("local");
        backup.setCreateBy(SecurityUtils.getUsername());
        backup.setCreateTime(DateUtils.getNowDate());
        sysDbBackupMapper.insertSysDbBackup(backup);

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
            
            // 更新备份成功状态
            backup.setStatus("0"); // 成功
            backup.setFileSize(file.length());
            sysDbBackupMapper.updateSysDbBackup(backup);
        }
        catch (Exception e)
        {
            // 更新失败状态
            backup.setStatus("1"); // 失败
            backup.setLogMsg(e.getMessage());
            sysDbBackupMapper.updateSysDbBackup(backup);
            throw e;
        }
    }

    @Override
    public SysDbBackup backupWithOptions(Long connId, String dbType, String backupMode, 
                                         String backupLevel, String targetName, 
                                         String storageType, String compressEnabled) throws Exception 
    {
        SysDbConn connInfo = sysDbConnMapper.selectSysDbConnByConnId(connId);
        if (connInfo == null)
        {
            throw new Exception("数据库连接不存在");
        }

        // 使用连接的数据库类型或指定类型
        String actualDbType = dbType != null ? dbType : connInfo.getDbType();
        if (actualDbType == null) {
            actualDbType = "mysql";
        }
        
        String actualBackupMode = backupMode != null ? backupMode : "full";
        
        // 先确定备份路径和文件名
        String backupPath = RuoYiConfig.getProfile() + "/backup";
        java.io.File backupDir = new java.io.File(backupPath);
        if (!backupDir.exists()) {
            backupDir.mkdirs();
        }
        
        // 保存用于日志记录的信息
        final Long logConnId = connId;
        final String logDbName = connInfo.getDbName();
        
        // 先生成文件名，确保入库时有值
        String fileName = generateBackupFileName(connInfo.getDbName(), actualDbType, actualBackupMode);
        String filePath = backupPath + "/" + fileName;

        // 创建备份记录（带完整初始信息）
        SysDbBackup backup = new SysDbBackup();
        backup.setConnId(connId);
        backup.setFileName(fileName);
        backup.setFilePath(filePath);
        backup.setDbType(actualDbType);
        backup.setBackupMode(actualBackupMode);
        backup.setBackupLevel(backupLevel != null ? backupLevel : "database");
        backup.setTargetName(targetName);
        backup.setStorageType(storageType != null ? storageType : "local");
        backup.setBackupType("0"); // 手动
        backup.setStatus("2"); // 进行中
        backup.setCreateBy(SecurityUtils.getUsername());
        backup.setCreateTime(DateUtils.getNowDate());
        sysDbBackupMapper.insertSysDbBackup(backup);

        try {
            // 查找对应的备份适配器
            IDbBackupAdapter adapter = getAdapter(actualDbType);
            if (adapter == null)
            {
                throw new Exception("不支持的数据库类型: " + actualDbType);
            }

            // 构建连接配置
            Map<String, String> connConfig = new HashMap<>();
            connConfig.put("host", connInfo.getHost());
            connConfig.put("port", connInfo.getPort());
            connConfig.put("username", connInfo.getUsername());
            connConfig.put("password", connInfo.getPassword());
            connConfig.put("database", connInfo.getDbName());
            connConfig.put("compressEnabled", compressEnabled != null ? compressEnabled : "1");

            // 执行备份
            File backupFile;
            if ("incremental".equals(actualBackupMode))
            {
                backupFile = adapter.executeIncrementalBackup(connConfig, backupPath, targetName);
            }
            else
            {
                backupFile = adapter.executeFullBackup(connConfig, backupPath, targetName);
            }

            // 计算MD5
            String md5 = adapter.calculateMd5(backupFile);

            // 更新备份记录
            backup.setFileName(backupFile.getName());
            backup.setFilePath(backupFile.getAbsolutePath());
            backup.setFileSize(backupFile.length());
            backup.setFileMd5(md5);
            backup.setStatus("0"); // 成功
            backup.setVerifyStatus("0");
            sysDbBackupMapper.updateSysDbBackup(backup);

            // 记录操作日志
            SysDbLog log = new SysDbLog();
            log.setConnId(logConnId);
            log.setOperationType("BACKUP");
            log.setSqlContent("备份数据库: " + logDbName + ", 文件: " + backupFile.getName());
            log.setStatus("0");
            log.setCostTime(System.currentTimeMillis() - backup.getCreateTime().getTime());
            log.setCreateBy(SecurityUtils.getUsername());
            log.setCreateTime(DateUtils.getNowDate());
            sysDbLogService.insertSysDbLog(log);

            return backup;
        } catch (Exception e) {
            backup.setStatus("1"); // 失败
            backup.setLogMsg(e.getMessage());
            sysDbBackupMapper.updateSysDbBackup(backup);

            // 记录操作日志
            SysDbLog log = new SysDbLog();
            log.setConnId(logConnId);
            log.setOperationType("BACKUP");
            log.setSqlContent("备份数据库: " + logDbName);
            log.setStatus("1");
            log.setErrorMsg(e.getMessage());
            log.setCreateBy(SecurityUtils.getUsername());
            log.setCreateTime(DateUtils.getNowDate());
            sysDbLogService.insertSysDbLog(log);

            throw e;
        }
    }

    /**
     * 获取备份适配器
     */
    private IDbBackupAdapter getAdapter(String dbType)
    {
        for (IDbBackupAdapter adapter : backupAdapters)
        {
            if (adapter.getDbType().equalsIgnoreCase(dbType))
            {
                return adapter;
            }
        }
        return null;
    }

    @Override
    public boolean verifyBackup(Long backupId) {
        SysDbBackup backup = sysDbBackupMapper.selectSysDbBackupByBackupId(backupId);
        if (backup == null || !"0".equals(backup.getStatus())) {
            return false;
        }

        java.io.File file = new java.io.File(backup.getFilePath());
        if (!file.exists() || file.length() == 0) {
            backup.setVerifyStatus("2");
            backup.setVerifyMsg("备份文件不存在或为空");
            sysDbBackupMapper.updateSysDbBackup(backup);
            return false;
        }

        // 简单的文件头验证
        try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(file))) {
            String line = reader.readLine();
            if (line != null && (line.contains("MySQL dump") || line.contains("PostgreSQL") || 
                line.contains("SQL") || line.startsWith("--"))) {
                backup.setVerifyStatus("1");
                backup.setVerifyMsg("验证成功");
                sysDbBackupMapper.updateSysDbBackup(backup);
                return true;
            }
        } catch (Exception e) {
            backup.setVerifyStatus("2");
            backup.setVerifyMsg("验证异常: " + e.getMessage());
            sysDbBackupMapper.updateSysDbBackup(backup);
            return false;
        }

        backup.setVerifyStatus("2");
        backup.setVerifyMsg("备份文件格式不正确");
        sysDbBackupMapper.updateSysDbBackup(backup);
        return false;
    }

    @Override
    public int cleanExpiredBackups(Integer retentionDays) {
        if (retentionDays == null) {
            retentionDays = 7;
        }
        
        List<SysDbBackup> expiredBackups = sysDbBackupMapper.selectExpiredBackups();
        int count = 0;
        
        for (SysDbBackup backup : expiredBackups) {
            try {
                // 删除文件
                java.io.File file = new java.io.File(backup.getFilePath());
                if (file.exists()) {
                    file.delete();
                }
                
                // 逻辑删除记录
                sysDbBackupMapper.logicDeleteSysDbBackup(backup.getBackupId());
                count++;
            } catch (Exception e) {
                logger.error("清理备份失败: " + backup.getBackupId(), e);
            }
        }
        
        return count;
    }

    @Override
    public boolean restoreBackup(Long backupId, Long targetConnId) throws Exception {
        return restoreBackupWithProgress(backupId, targetConnId, null);
    }

    @Override
    public boolean restoreBackupWithProgress(Long backupId, Long targetConnId, RestoreProgressCallback callback) throws Exception {
        long startTime = System.currentTimeMillis();
        String taskId = "restore_" + backupId + "_" + startTime;
        
        // 保存用于日志记录的最终值
        final Long finalTargetConnId = targetConnId;
        
        try {
            // 1. 参数验证
            notifyProgress(callback, 0, "初始化恢复任务", "正在验证备份记录...");
            
            SysDbBackup backup = sysDbBackupMapper.selectSysDbBackupByBackupId(backupId);
            if (backup == null) {
                throw new Exception("备份记录不存在");
            }
            notifyLog(callback, "[INFO] 备份记录验证通过, 备份ID: " + backupId);

            if (!"0".equals(backup.getStatus())) {
                throw new Exception("备份文件不可用, 当前状态: " + backup.getStatus());
            }

            SysDbConn targetConn = sysDbConnMapper.selectSysDbConnByConnId(targetConnId);
            if (targetConn == null) {
                throw new Exception("目标连接不存在");
            }
            notifyLog(callback, "[INFO] 目标连接: " + targetConn.getHost() + ":" + targetConn.getPort() + "/" + targetConn.getDbName());

            java.io.File backupFile = new java.io.File(backup.getFilePath());
            if (!backupFile.exists()) {
                throw new Exception("备份文件不存在: " + backup.getFilePath());
            }
            notifyLog(callback, "[INFO] 备份文件: " + backupFile.getAbsolutePath() + " (" + formatFileSize(backupFile.length()) + ")");

            notifyScope(callback, "数据库: " + targetConn.getDbName() + ", 备份文件: " + backup.getFileName());

            // 2. 解压缩（如果需要）
            notifyProgress(callback, 5, "准备备份文件", "检查并解压备份文件...");
            java.io.File sqlFile = backupFile;
            boolean needCleanup = false;
            
            if (backupFile.getName().endsWith(".gz")) {
                notifyLog(callback, "[INFO] 检测到压缩文件, 开始解压...");
                sqlFile = decompressFile(backupFile);
                needCleanup = true;
                notifyLog(callback, "[INFO] 解压完成: " + sqlFile.getAbsolutePath());
            }

            // 3. 解析SQL文件统计表数量
            notifyProgress(callback, 10, "分析备份内容", "解析SQL文件结构...");
            List<String> tableNames = extractTableNamesFromSql(sqlFile);
            int totalTables = tableNames.size();
            notifyLog(callback, "[INFO] 检测到 " + totalTables + " 个表需要恢复");
            if (!tableNames.isEmpty()) {
                notifyLog(callback, "[INFO] 表列表: " + String.join(", ", tableNames.subList(0, Math.min(10, tableNames.size())))
                        + (tableNames.size() > 10 ? " 等..." : ""));
            }
            notifyTableProgress(callback, totalTables, 0);

            // 4. 执行恢复
            notifyProgress(callback, 15, "连接数据库", "建立数据库连接...");
            String jdbcUrl = buildRestoreJdbcUrl(targetConn);
            notifyLog(callback, "[INFO] 连接URL: " + jdbcUrl);

            try (java.sql.Connection conn = java.sql.DriverManager.getConnection(
                    jdbcUrl, targetConn.getUsername(), targetConn.getPassword())) {
                
                notifyLog(callback, "[INFO] 数据库连接成功");
                notifyProgress(callback, 20, "开始恢复", "正在执行SQL语句...");

                // 读取并执行SQL
                int completedTables = 0;
                int totalStatements = 0;
                int completedStatements = 0;
                String currentTable = "";
                
                try (java.io.BufferedReader reader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(new java.io.FileInputStream(sqlFile), "UTF-8"))) {
                    
                    StringBuilder sqlBuffer = new StringBuilder();
                    String line;
                    boolean inMultiLineComment = false;
                    
                    while ((line = reader.readLine()) != null) {
                        String trimmedLine = line.trim();
                        
                        // 跳过空行和注释
                        if (trimmedLine.isEmpty()) {
                            continue;
                        }
                        
                        // 处理多行注释
                        if (trimmedLine.startsWith("/*")) {
                            inMultiLineComment = true;
                        }
                        if (inMultiLineComment) {
                            if (trimmedLine.endsWith("*/")) {
                                inMultiLineComment = false;
                            }
                            continue;
                        }
                        
                        // 跳过单行注释
                        if (trimmedLine.startsWith("--") || trimmedLine.startsWith("#")) {
                            continue;
                        }
                        
                        sqlBuffer.append(line).append("\n");
                        
                        // 执行完整的SQL语句
                        if (trimmedLine.endsWith(";")) {
                            String sql = sqlBuffer.toString().trim();
                            sqlBuffer.setLength(0);
                            
                            if (!sql.isEmpty()) {
                                totalStatements++;
                                
                                // 检测当前处理的表
                                if (sql.toUpperCase().contains("CREATE TABLE")) {
                                    String tableName = extractTableNameFromSql(sql);
                                    if (!tableName.isEmpty() && !tableName.equals(currentTable)) {
                                        currentTable = tableName;
                                        completedTables++;
                                        notifyLog(callback, "[PROGRESS] 正在恢复表: " + tableName + " (" + completedTables + "/" + totalTables + ")");
                                        notifyTableProgress(callback, totalTables, completedTables);
                                    }
                                }
                                
                                // 执行SQL
                                try (java.sql.Statement stmt = conn.createStatement()) {
                                    stmt.execute(sql);
                                    completedStatements++;
                                } catch (java.sql.SQLException e) {
                                    // 某些错误可以忽略（如表已存在）
                                    if (!isIgnorableError(e)) {
                                        notifyLog(callback, "[WARN] SQL执行警告: " + e.getMessage());
                                    }
                                }
                                
                                // 更新进度
                                if (totalStatements % 100 == 0) {
                                    int progress = 20 + (int) ((completedTables * 1.0 / totalTables) * 75);
                                    progress = Math.min(progress, 95);
                                    notifyProgress(callback, progress, "恢复数据中", 
                                            "已处理 " + completedTables + "/" + totalTables + " 个表, " + completedStatements + " 条语句");
                                }
                            }
                        }
                    }
                }
                
                notifyLog(callback, "[INFO] 共执行 " + completedStatements + " 条SQL语句");
            }

            // 5. 清理临时文件
            if (needCleanup && sqlFile.exists()) {
                sqlFile.delete();
                notifyLog(callback, "[INFO] 临时文件已清理");
            }

            // 6. 记录恢复日志
            long elapsedTime = (System.currentTimeMillis() - startTime) / 1000;
            notifyLog(callback, "[SUCCESS] 恢复完成, 耗时: " + elapsedTime + " 秒");
            notifyProgress(callback, 100, "恢复完成", "数据库恢复成功");
            notifyComplete(callback, true, "恢复成功");
            
            // 更新备份记录中的恢复信息
            backup.setLogMsg((backup.getLogMsg() != null ? backup.getLogMsg() + "\n" : "") + 
                    "恢复时间: " + DateUtils.dateTimeNow() + ", 恢复到: " + targetConn.getHost() + "/" + targetConn.getDbName());
            sysDbBackupMapper.updateSysDbBackup(backup);

            // 记录操作日志
            SysDbLog log = new SysDbLog();
            log.setConnId(targetConnId);
            log.setOperationType("RESTORE");
            log.setSqlContent("恢复数据库: " + backup.getFileName() + " -> " + targetConn.getDbName());
            log.setStatus("0");
            log.setCostTime(System.currentTimeMillis() - startTime);
            log.setCreateBy(SecurityUtils.getUsername());
            log.setCreateTime(DateUtils.getNowDate());
            sysDbLogService.insertSysDbLog(log);

            return true;
            
        } catch (Exception e) {
            long elapsedTime = (System.currentTimeMillis() - startTime) / 1000;
            notifyLog(callback, "[ERROR] 恢复失败: " + e.getMessage());
            notifyProgress(callback, 0, "恢复失败", e.getMessage());
            notifyComplete(callback, false, e.getMessage());
            logger.error("恢复备份失败, backupId={}, targetConnId={}", backupId, targetConnId, e);

            // 记录操作日志
            SysDbLog log = new SysDbLog();
            log.setConnId(targetConnId);
            log.setOperationType("RESTORE");
            log.setSqlContent("恢复数据库, 备份ID: " + backupId);
            log.setStatus("1");
            log.setErrorMsg(e.getMessage());
            log.setCostTime(System.currentTimeMillis() - startTime);
            log.setCreateBy(SecurityUtils.getUsername());
            log.setCreateTime(DateUtils.getNowDate());
            sysDbLogService.insertSysDbLog(log);

            throw e;
        }
    }

    // ==================== 辅助方法 ====================

    private void notifyProgress(RestoreProgressCallback callback, int progress, String step, String message) {
        if (callback != null) {
            callback.onProgress(progress, message);
            callback.onStep(step);
        }
    }

    private void notifyLog(RestoreProgressCallback callback, String log) {
        if (callback != null) {
            callback.onLog(log);
        }
    }

    private void notifyTableProgress(RestoreProgressCallback callback, int total, int completed) {
        if (callback != null) {
            callback.onTableProgress(total, completed);
        }
    }

    private void notifyScope(RestoreProgressCallback callback, String scope) {
        if (callback != null) {
            callback.onScope(scope);
        }
    }

    private void notifyComplete(RestoreProgressCallback callback, boolean success, String message) {
        if (callback != null) {
            callback.onComplete(success, message);
        }
    }

    private String buildRestoreJdbcUrl(SysDbConn conn) {
        StringBuilder url = new StringBuilder("jdbc:mysql://");
        url.append(conn.getHost()).append(":").append(conn.getPort() != null ? conn.getPort() : "3306");
        url.append("/").append(conn.getDbName());
        url.append("?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=GMT%2B8&allowPublicKeyRetrieval=true");
        return url.toString();
    }

    private java.io.File decompressFile(java.io.File gzFile) throws Exception {
        String sqlFileName = gzFile.getAbsolutePath().replaceAll("\\.gz$", "");
        java.io.File sqlFile = new java.io.File(sqlFileName);
        
        try (java.io.FileInputStream fis = new java.io.FileInputStream(gzFile);
             java.util.zip.GZIPInputStream gzis = new java.util.zip.GZIPInputStream(fis);
             java.io.FileOutputStream fos = new java.io.FileOutputStream(sqlFile)) {
            byte[] buffer = new byte[8192];
            int len;
            while ((len = gzis.read(buffer)) > 0) {
                fos.write(buffer, 0, len);
            }
        }
        return sqlFile;
    }

    private List<String> extractTableNamesFromSql(java.io.File sqlFile) throws Exception {
        List<String> tableNames = new ArrayList<>();
        try (java.io.BufferedReader reader = new java.io.BufferedReader(
                new java.io.InputStreamReader(new java.io.FileInputStream(sqlFile), "UTF-8"))) {
            StringBuilder sqlBuffer = new StringBuilder();
            String line;
            boolean inMultiLineComment = false;
            
            while ((line = reader.readLine()) != null) {
                String trimmedLine = line.trim();
                
                // 跳过空行
                if (trimmedLine.isEmpty()) {
                    continue;
                }
                
                // 处理多行注释
                if (trimmedLine.startsWith("/*")) {
                    inMultiLineComment = true;
                }
                if (inMultiLineComment) {
                    if (trimmedLine.endsWith("*/")) {
                        inMultiLineComment = false;
                    }
                    continue;
                }
                
                // 跳过单行注释
                if (trimmedLine.startsWith("--") || trimmedLine.startsWith("#")) {
                    continue;
                }
                
                sqlBuffer.append(line).append("\n");
                
                // 处理完整的SQL语句
                if (trimmedLine.endsWith(";")) {
                    String sql = sqlBuffer.toString().trim();
                    sqlBuffer.setLength(0);
                    
                    if (!sql.isEmpty() && sql.toUpperCase().contains("CREATE TABLE")) {
                        String tableName = extractTableNameFromSql(sql);
                        if (!tableName.isEmpty() && !tableNames.contains(tableName)) {
                            tableNames.add(tableName);
                        }
                    }
                }
            }
        }
        return tableNames;
    }

    private String extractTableNameFromSql(String sql) {
        // 匹配 CREATE TABLE `table_name` 或 CREATE TABLE table_name
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(
                "CREATE\\s+TABLE\\s+(?:IF\\s+NOT\\s+EXISTS\\s+)?[`\"]?([^`\\s(]+)[`\"]?", 
                java.util.regex.Pattern.CASE_INSENSITIVE);
        java.util.regex.Matcher matcher = pattern.matcher(sql);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }

    private boolean isIgnorableError(java.sql.SQLException e) {
        String message = e.getMessage().toLowerCase();
        // 忽略表已存在、数据库已存在等错误
        return message.contains("already exists") || 
               message.contains("duplicate") ||
               message.contains("can't drop");
    }

    private String formatFileSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.2f KB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", size / (1024.0 * 1024));
        } else {
            return String.format("%.2f GB", size / (1024.0 * 1024 * 1024));
        }
    }

    private String generateBackupFileName(String dbName, String dbType, String backupMode) {
        String timestamp = DateUtils.dateTimeNow("yyyyMMdd_HHmmss");
        String mode = backupMode != null ? backupMode : "full";
        return String.format("%s_%s_%s_%s.sql", dbType, dbName != null ? dbName : "backup", mode, timestamp);
    }

    private void executeBackupLogic(SysDbConn connInfo, String filePath, String targetName) throws Exception {
        // 简化的备份逻辑，调用原有的备份方法的核心逻辑
        java.io.File file = new java.io.File(filePath);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        try (java.io.FileWriter writer = new java.io.FileWriter(file);
             java.sql.Connection conn = java.sql.DriverManager.getConnection(
                 "jdbc:mysql://" + connInfo.getHost() + ":" + connInfo.getPort() + "/" + connInfo.getDbName() 
                 + "?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=GMT%2B8", 
                 connInfo.getUsername(), connInfo.getPassword()))
        {
            java.util.List<String> tables = dbExecuteService.getTableList(connInfo.getConnId());

            for (String table : tables) {
                // 导出表结构
                try (java.sql.Statement stmt = conn.createStatement();
                     java.sql.ResultSet rs = stmt.executeQuery("SHOW CREATE TABLE " + table))
                {
                    if (rs.next()) {
                        writer.write("-- Table structure for " + table + "\n");
                        writer.write("DROP TABLE IF EXISTS `" + table + "`;\n");
                        writer.write(rs.getString(2) + ";\n\n");
                    }
                }

                // 导出表数据
                try (java.sql.Statement stmt = conn.createStatement();
                     java.sql.ResultSet rs = stmt.executeQuery("SELECT * FROM " + table))
                {
                    java.sql.ResultSetMetaData metaData = rs.getMetaData();
                    int columnCount = metaData.getColumnCount();

                    writer.write("-- Dumping data for table " + table + "\n");
                    while (rs.next()) {
                        StringBuilder insert = new StringBuilder("INSERT INTO `" + table + "` VALUES (");
                        for (int i = 1; i <= columnCount; i++) {
                            Object value = rs.getObject(i);
                            if (value == null) {
                                insert.append("NULL");
                            } else if (value instanceof String || value instanceof java.sql.Date || value instanceof java.sql.Timestamp) {
                                insert.append("'").append(value.toString().replace("'", "\\'")).append("'");
                            } else {
                                insert.append(value);
                            }
                            if (i < columnCount) {
                                insert.append(", ");
                            }
                        }
                        insert.append(");\n");
                        writer.write(insert.toString());
                    }
                    writer.write("\n");
                }
            }
        }
    }

    private String compressFile(String filePath) throws Exception {
        java.io.File sourceFile = new java.io.File(filePath);
        String compressedPath = filePath + ".gz";
        
        try (java.io.FileInputStream fis = new java.io.FileInputStream(sourceFile);
             java.io.FileOutputStream fos = new java.io.FileOutputStream(compressedPath);
             java.util.zip.GZIPOutputStream gzos = new java.util.zip.GZIPOutputStream(fos)) {
            byte[] buffer = new byte[8192];
            int len;
            while ((len = fis.read(buffer)) != -1) {
                gzos.write(buffer, 0, len);
            }
        }
        
        sourceFile.delete();
        return compressedPath;
    }
}
