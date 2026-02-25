package com.ruoyi.system.service.backup.impl;

import java.io.*;
import java.math.BigInteger;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import org.springframework.stereotype.Component;

import com.ruoyi.system.service.backup.IDbBackupAdapter;

/**
 * MySQL/MariaDB 备份适配器
 * 使用JDBC方式进行备份，不依赖外部命令
 *
 * @author ruoyi
 */
@Component
public class MySqlBackupAdapter implements IDbBackupAdapter {

    @Override
    public String getDbType() {
        return "mysql";
    }

    @Override
    public File executeFullBackup(Map<String, String> connConfig, String backupPath, String targetName) throws Exception {
        String host = connConfig.get("host");
        String port = connConfig.get("port");
        String username = connConfig.get("username");
        String password = connConfig.get("password");
        String database = connConfig.get("database");
        String compressEnabled = connConfig.getOrDefault("compressEnabled", "1");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String timestamp = sdf.format(new Date());
        String backupFileName = String.format("mysql_%s_full_%s.sql",
                database != null ? database : "all", timestamp);
        File backupFile = new File(backupPath, backupFileName);
        backupFile.getParentFile().mkdirs();

        // 使用JDBC方式备份，使用UTF-8编码
        String jdbcUrl = buildJdbcUrl(host, port, database);

        try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password);
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(backupFile), "UTF-8"))) {

            writer.write("-- MySQL Database Backup\n");
            writer.write("-- Generated: " + new Date() + "\n");
            writer.write("-- Database: " + database + "\n");
            writer.write("-- Charset: UTF-8\n\n");
            writer.write("SET FOREIGN_KEY_CHECKS=0;\n\n");
            writer.write("SET NAMES utf8mb4;\n\n");

            // 解析targetName，获取要备份的表列表
            List<String> targetTables = parseTargetTables(targetName, database);

            if (targetTables != null && !targetTables.isEmpty()) {
                // 备份指定的表
                for (String tableName : targetTables) {
                    dumpTable(conn, writer, tableName);
                }
            } else {
                // 获取所有表
                String tableSql = database != null
                    ? "SELECT TABLE_NAME FROM information_schema.TABLES WHERE TABLE_SCHEMA = ? AND TABLE_TYPE = 'BASE TABLE'"
                    : "SHOW TABLES";

                try (PreparedStatement pstmt = conn.prepareStatement(tableSql)) {
                    if (database != null) {
                        pstmt.setString(1, database);
                    }

                    try (ResultSet rs = pstmt.executeQuery()) {
                        while (rs.next()) {
                            String tableName = rs.getString(1);
                            dumpTable(conn, writer, tableName);
                        }
                    }
                }
            }

            writer.write("SET FOREIGN_KEY_CHECKS=1;\n");
        }

        // 根据配置决定是否压缩
        if ("1".equals(compressEnabled) || "true".equalsIgnoreCase(compressEnabled)) {
            File compressedFile = compressFile(backupFile);
            backupFile.delete();
            return compressedFile;
        } else {
            return backupFile;
        }
    }

    /**
     * 解析targetName获取表列表
     */
    private List<String> parseTargetTables(String targetName, String database) {
        if (targetName == null || targetName.trim().isEmpty()) {
            return null;
        }

        List<String> tables = new ArrayList<>();
        String[] parts = targetName.split(",");

        for (String part : parts) {
            String trimmed = part.trim();
            if (trimmed.isEmpty()) {
                continue;
            }

            // 处理 db.table 格式
            if (trimmed.contains(".")) {
                String[] dbTable = trimmed.split("\\.");
                if (dbTable.length == 2) {
                    // 如果指定了数据库名，检查是否匹配当前数据库
                    if (database == null || database.equals(dbTable[0])) {
                        tables.add(dbTable[1]);
                    }
                }
            } else {
                tables.add(trimmed);
            }
        }

        return tables;
    }

    private void dumpTable(Connection conn, BufferedWriter writer, String tableName) throws Exception {
        writer.write("-- ----------------------------\n");
        writer.write("-- Table structure for `" + tableName + "`\n");
        writer.write("-- ----------------------------\n");
        writer.write("DROP TABLE IF EXISTS `" + tableName + "`;\n");

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SHOW CREATE TABLE `" + tableName + "`")) {
            if (rs.next()) {
                writer.write(rs.getString(2) + ";\n\n");
            }
        }

        writer.write("-- ----------------------------\n");
        writer.write("-- Dumping data for table `" + tableName + "`\n");
        writer.write("-- ----------------------------\n");

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM `" + tableName + "`")) {

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            while (rs.next()) {
                StringBuilder insert = new StringBuilder("INSERT INTO `" + tableName + "` VALUES (");
                for (int i = 1; i <= columnCount; i++) {
                    Object value = rs.getObject(i);
                    if (value == null) {
                        insert.append("NULL");
                    } else if (value instanceof String || value instanceof java.sql.Date
                            || value instanceof java.sql.Timestamp || value instanceof byte[]) {
                        if (value instanceof byte[]) {
                            insert.append("0x").append(bytesToHex((byte[]) value));
                        } else {
                            insert.append("'").append(escapeString(value.toString())).append("'");
                        }
                    } else if (value instanceof Number) {
                        insert.append(value);
                    } else {
                        insert.append("'").append(escapeString(value.toString())).append("'");
                    }

                    if (i < columnCount) {
                        insert.append(", ");
                    }
                }
                insert.append(");\n");
                writer.write(insert.toString());
            }
        }
        writer.write("\n");
    }

    private String escapeString(String str) {
        if (str == null) return "";
        return str.replace("\\", "\\\\")
                  .replace("'", "\\'")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t")
                  .replace("\0", "\\0");
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private String buildJdbcUrl(String host, String port, String database) {
        StringBuilder url = new StringBuilder("jdbc:mysql://");
        url.append(host).append(":").append(port != null ? port : "3306");
        if (database != null && !database.isEmpty()) {
            url.append("/").append(database);
        }
        url.append("?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=GMT%2B8&allowPublicKeyRetrieval=true");
        return url.toString();
    }

    @Override
    public File executeIncrementalBackup(Map<String, String> connConfig, String backupPath, String targetName) throws Exception {
        String host = connConfig.get("host");
        String port = connConfig.get("port");
        String username = connConfig.get("username");
        String password = connConfig.get("password");
        String database = connConfig.get("database");
        String compressEnabled = connConfig.getOrDefault("compressEnabled", "1");
        
        // 获取binlog位置作为增量备份基准点
        String jdbcUrl = buildJdbcUrl(host, port, database);
        String binlogFile = null;
        long binlogPosition = 0;
        boolean hasBinlogPrivilege = false;
        
        try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SHOW MASTER STATUS")) {
            if (rs.next()) {
                binlogFile = rs.getString("File");
                binlogPosition = rs.getLong("Position");
                hasBinlogPrivilege = true;
            }
        } catch (SQLException e) {
            // 权限不足或binlog未开启，记录警告但不中断备份
            System.out.println("[WARN] 无法获取binlog位置，可能原因：1)用户无REPLICATION CLIENT权限 2)binlog未开启。将使用全量备份代替。错误：" + e.getMessage());
        }
        
        // 如果没有binlog权限，回退到全量备份
        if (!hasBinlogPrivilege) {
            System.out.println("[INFO] 增量备份需要binlog权限，自动回退到全量备份模式");
            return executeFullBackup(connConfig, backupPath, targetName);
        }
        
        // 生成增量备份文件名
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String timestamp = sdf.format(new Date());
        String backupFileName = String.format("mysql_%s_incremental_%s.sql",
                database != null ? database : "all", timestamp);
        File backupFile = new File(backupPath, backupFileName);
        backupFile.getParentFile().mkdirs();
        
        // 使用JDBC方式备份，使用UTF-8编码
        try (Connection conn = DriverManager.getConnection(jdbcUrl, username, password);
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(backupFile), "UTF-8"))) {

            writer.write("-- MySQL Incremental Database Backup\n");
            writer.write("-- Generated: " + new Date() + "\n");
            writer.write("-- Database: " + database + "\n");
            writer.write("-- Binlog File: " + binlogFile + "\n");
            writer.write("-- Binlog Position: " + binlogPosition + "\n");
            writer.write("-- Note: 增量备份基于binlog位置，恢复时需要配合binlog使用\n");
            writer.write("-- Charset: UTF-8\n\n");
            writer.write("SET FOREIGN_KEY_CHECKS=0;\n\n");
            writer.write("SET NAMES utf8mb4;\n\n");
            
            // 解析目标表
            List<String> targetTables = parseTargetTables(targetName, database);
            List<String> tablesToBackup = new ArrayList<>();
            
            if (targetTables != null && !targetTables.isEmpty()) {
                tablesToBackup = targetTables;
            } else {
                // 获取所有表
                String tableSql = database != null
                    ? "SELECT TABLE_NAME FROM information_schema.TABLES WHERE TABLE_SCHEMA = ? AND TABLE_TYPE = 'BASE TABLE'"
                    : "SHOW TABLES";

                try (PreparedStatement pstmt = conn.prepareStatement(tableSql)) {
                    if (database != null) {
                        pstmt.setString(1, database);
                    }

                    try (ResultSet rs = pstmt.executeQuery()) {
                        while (rs.next()) {
                            tablesToBackup.add(rs.getString(1));
                        }
                    }
                }
            }
            
            // 增量备份：导出表结构和所有数据（简化实现）
            // 实际增量备份应该基于上次备份的binlog位置，这里记录当前位置供参考
            for (String tableName : tablesToBackup) {
                dumpTable(conn, writer, tableName);
            }

            writer.write("SET FOREIGN_KEY_CHECKS=1;\n");
            writer.write("\n-- Incremental backup reference: " + binlogFile + ":" + binlogPosition + "\n");
            writer.write("-- To restore incremental changes, use: mysqlbinlog --start-position=" + binlogPosition + " " + binlogFile + " | mysql ...\n");
        }

        // 根据配置决定是否压缩
        if ("1".equals(compressEnabled) || "true".equalsIgnoreCase(compressEnabled)) {
            File compressedFile = compressFile(backupFile);
            backupFile.delete();
            return compressedFile;
        } else {
            return backupFile;
        }
    }
    
    /**
     * 只导出表数据（用于增量备份）
     */
    private void dumpTableDataOnly(Connection conn, BufferedWriter writer, String tableName) throws Exception {
        writer.write("-- ----------------------------\n");
        writer.write("-- Incremental data for table `" + tableName + "`\n");
        writer.write("-- ----------------------------\n");

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM `" + tableName + "`")) {

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            int rowCount = 0;
            while (rs.next()) {
                rowCount++;
                StringBuilder insert = new StringBuilder("INSERT INTO `" + tableName + "` VALUES (");
                for (int i = 1; i <= columnCount; i++) {
                    Object value = rs.getObject(i);
                    if (value == null) {
                        insert.append("NULL");
                    } else if (value instanceof String || value instanceof java.sql.Date
                            || value instanceof java.sql.Timestamp || value instanceof byte[]) {
                        if (value instanceof byte[]) {
                            insert.append("0x").append(bytesToHex((byte[]) value));
                        } else {
                            insert.append("'").append(escapeString(value.toString())).append("'");
                        }
                    } else if (value instanceof Number) {
                        insert.append(value);
                    } else {
                        insert.append("'").append(escapeString(value.toString())).append("'");
                    }

                    if (i < columnCount) {
                        insert.append(", ");
                    }
                }
                insert.append(");\n");
                writer.write(insert.toString());
            }
            
            if (rowCount == 0) {
                writer.write("-- No data in table\n");
            }
        }
        writer.write("\n");
    }

    @Override
    public boolean verifyBackup(File backupFile, Map<String, String> connConfig) {
        if (!backupFile.exists() || backupFile.length() == 0) {
            return false;
        }

        try {
            if (backupFile.getName().endsWith(".gz")) {
                try (InputStream is = new FileInputStream(backupFile);
                     java.util.zip.GZIPInputStream gis = new java.util.zip.GZIPInputStream(is);
                     BufferedReader reader = new BufferedReader(new InputStreamReader(gis))) {
                    String line;
                    int count = 0;
                    while ((line = reader.readLine()) != null && count < 10) {
                        if (line.contains("MySQL") || line.contains("INSERT") || line.contains("DROP")) {
                            return true;
                        }
                        count++;
                    }
                }
            } else {
                try (BufferedReader reader = new BufferedReader(new FileReader(backupFile))) {
                    String line;
                    int count = 0;
                    while ((line = reader.readLine()) != null && count < 10) {
                        if (line.contains("MySQL") || line.contains("INSERT") || line.contains("DROP")) {
                            return true;
                        }
                        count++;
                    }
                }
            }
        } catch (Exception e) {
            return false;
        }

        return backupFile.length() > 0;
    }

    @Override
    public boolean restoreBackup(File backupFile, Map<String, String> connConfig, String targetName) throws Exception {
        String host = connConfig.get("host");
        String port = connConfig.get("port");
        String username = connConfig.get("username");
        String password = connConfig.get("password");
        String database = connConfig.get("database");

        String jdbcUrl = buildJdbcUrl(host, port, database);

        InputStream inputStream;
        if (backupFile.getName().endsWith(".gz")) {
            inputStream = new java.util.zip.GZIPInputStream(new FileInputStream(backupFile));
        } else {
            inputStream = new FileInputStream(backupFile);
        }

        try (Connection conn = DriverManager.getConnection(jdbcUrl, username,password);
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
             Statement stmt = conn.createStatement()) {

            StringBuilder sql = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("--")) {
                    continue;
                }
                sql.append(line).append(" ");
                if (line.endsWith(";")) {
                    stmt.execute(sql.toString());
                    sql.setLength(0);
                }
            }
        }

        return true;
    }

    @Override
    public String calculateMd5(File backupFile) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            try (InputStream is = Files.newInputStream(backupFile.toPath())) {
                byte[] buffer = new byte[8192];
                int len;
                while ((len = is.read(buffer)) != -1) {
                    md.update(buffer, 0, len);
                }
            }
            BigInteger bi = new BigInteger(1, md.digest());
            return String.format("%032x", bi);
        } catch (Exception e) {
            return null;
        }
    }

    private File compressFile(File sourceFile) throws IOException {
        File compressedFile = new File(sourceFile.getAbsolutePath() + ".gz");

        try (FileInputStream fis = new FileInputStream(sourceFile);
             FileOutputStream fos = new FileOutputStream(compressedFile);
             GZIPOutputStream gzos = new GZIPOutputStream(fos)) {
            byte[] buffer = new byte[8192];
            int len;
            while ((len = fis.read(buffer)) != -1) {
                gzos.write(buffer, 0, len);
            }
        }

        return compressedFile;
    }
}
