package com.ruoyi.system.service.backup.impl;

import java.io.*;
import java.math.BigInteger;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import org.springframework.stereotype.Component;

import com.ruoyi.system.service.backup.IDbBackupAdapter;

/**
 * PostgreSQL 备份适配器
 * 
 * @author ruoyi
 */
@Component
public class PostgreSqlBackupAdapter implements IDbBackupAdapter {

    @Override
    public String getDbType() {
        return "postgresql";
    }

    @Override
    public File executeFullBackup(Map<String, String> connConfig, String backupPath, String targetName) throws Exception {
        String host = connConfig.get("host");
        String port = connConfig.get("port");
        String username = connConfig.get("username");
        String password = connConfig.get("password");
        String database = connConfig.get("database");

        // 设置环境变量
        ProcessBuilder pb = new ProcessBuilder();
        pb.environment().put("PGPASSWORD", password);
        pb.redirectErrorStream(true);

        // 生成备份文件名
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String timestamp = sdf.format(new Date());
        String backupFileName;

        if (targetName != null && !targetName.isEmpty()) {
            // 指定数据库备份
            backupFileName = String.format("postgresql_%s_full_%s.sql", targetName, timestamp);
        } else {
            // 全实例备份
            backupFileName = String.format("postgresql_%s_full_%s.sql", 
                    database != null ? database : "all", timestamp);
        }

        File backupFile = new File(backupPath, backupFileName);
        backupFile.getParentFile().mkdirs();

        // 构建pg_dump命令
        if (targetName != null && !targetName.isEmpty()) {
            pb.command("pg_dump", "-h", host, "-p", port, "-U", username, 
                    "-d", targetName, "-F", "p", "-v");
        } else {
            // 使用pg_dumpall备份所有数据库
            pb.command("pg_dumpall", "-h", host, "-p", port, "-U", username, "-v");
        }

        // 执行备份
        Process process = pb.start();

        try (InputStream is = process.getInputStream();
                FileOutputStream fos = new FileOutputStream(backupFile);
                BufferedOutputStream bos = new BufferedOutputStream(fos)) {
            byte[] buffer = new byte[8192];
            int len;
            while ((len = is.read(buffer)) != -1) {
                bos.write(buffer, 0, len);
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            backupFile.delete();
            throw new RuntimeException("PostgreSQL备份失败，退出码: " + exitCode);
        }

        // 压缩备份文件
        File compressedFile = compressFile(backupFile);
        backupFile.delete();

        return compressedFile;
    }

    @Override
    public File executeIncrementalBackup(Map<String, String> connConfig, String backupPath, String targetName) throws Exception {
        // PostgreSQL使用WAL日志进行增量备份，使用pg_basebackup工具
        String host = connConfig.get("host");
        String port = connConfig.get("port");
        String username = connConfig.get("username");
        String password = connConfig.get("password");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String timestamp = sdf.format(new Date());
        String backupDirName = String.format("postgresql_%s_base_%s",
                targetName != null ? targetName : "all", timestamp);
        File backupDir = new File(backupPath, backupDirName);

        ProcessBuilder pb = new ProcessBuilder();
        pb.environment().put("PGPASSWORD", password);
        pb.redirectErrorStream(true);
        pb.command("pg_basebackup", "-h", host, "-p", port, "-U", username,
                "-D", backupDir.getAbsolutePath(), "-Fp", "-Xs", "-P");

        Process process = pb.start();

        try (InputStream is = process.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            deleteDirectory(backupDir);
            throw new RuntimeException("PostgreSQL基础备份失败，请确保已配置WAL归档，退出码: " + exitCode);
        }

        // 打包为tar.gz
        File tarFile = new File(backupPath, backupDirName + ".tar.gz");
        packToTarGz(backupDir, tarFile);
        deleteDirectory(backupDir);

        return tarFile;
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
                        if (line.contains("PostgreSQL database dump") || line.contains("pg_dump")) {
                            return true;
                        }
                        count++;
                    }
                }
            } else if (backupFile.getName().endsWith(".tar.gz")) {
                // 对于tar.gz文件，检查文件大小和完整性
                return backupFile.length() > 1024;
            }
        } catch (Exception e) {
            return false;
        }

        return false;
    }

    @Override
    public boolean restoreBackup(File backupFile, Map<String, String> connConfig, String targetName) throws Exception {
        String host = connConfig.get("host");
        String port = connConfig.get("port");
        String username = connConfig.get("username");
        String password = connConfig.get("password");

        ProcessBuilder pb = new ProcessBuilder();
        pb.environment().put("PGPASSWORD", password);
        pb.redirectErrorStream(true);
        pb.command("psql", "-h", host, "-p", port, "-U", username, "-f", "-");

        Process process = pb.start();

        try (OutputStream os = process.getOutputStream()) {
            if (backupFile.getName().endsWith(".gz")) {
                try (InputStream is = new FileInputStream(backupFile);
                        java.util.zip.GZIPInputStream gis = new java.util.zip.GZIPInputStream(is)) {
                    byte[] buffer = new byte[8192];
                    int len;
                    while ((len = gis.read(buffer)) != -1) {
                        os.write(buffer, 0, len);
                    }
                }
            } else {
                try (FileInputStream fis = new FileInputStream(backupFile)) {
                    byte[] buffer = new byte[8192];
                    int len;
                    while ((len = fis.read(buffer)) != -1) {
                        os.write(buffer, 0, len);
                    }
                }
            }
        }

        int exitCode = process.waitFor();
        return exitCode == 0;
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

    private void packToTarGz(File sourceDir, File tarGzFile) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(tarGzFile);
                GZIPOutputStream gzos = new GZIPOutputStream(fos);
                java.util.zip.ZipOutputStream zos = new java.util.zip.ZipOutputStream(gzos)) {
            // 简化为zip格式
            packDir(sourceDir, sourceDir.getName(), zos);
        }
    }

    private void packDir(File dir, String name, java.util.zip.ZipOutputStream zos) throws IOException {
        File[] files = dir.listFiles();
        if (files == null) return;

        for (File file : files) {
            String entryName = name + "/" + file.getName();
            if (file.isDirectory()) {
                packDir(file, entryName, zos);
            } else {
                try (FileInputStream fis = new FileInputStream(file)) {
                    java.util.zip.ZipEntry entry = new java.util.zip.ZipEntry(entryName);
                    zos.putNextEntry(entry);
                    byte[] buffer = new byte[8192];
                    int len;
                    while ((len = fis.read(buffer)) != -1) {
                        zos.write(buffer, 0, len);
                    }
                    zos.closeEntry();
                }
            }
        }
    }

    private void deleteDirectory(File dir) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        dir.delete();
    }
}
