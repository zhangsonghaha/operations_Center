package com.ruoyi.system.service.backup.impl;

import java.io.*;
import java.math.BigInteger;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.ruoyi.system.service.backup.IDbBackupAdapter;

/**
 * Redis 备份适配器
 *
 * @author ruoyi
 */
@Component
public class RedisBackupAdapter implements IDbBackupAdapter {

    @Override
    public String getDbType() {
        return "redis";
    }

    @Override
    public File executeFullBackup(Map<String, String> connConfig, String backupPath, String targetName) throws Exception {
        String host = connConfig.get("host");
        String port = connConfig.get("port");
        String password = connConfig.get("password");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String timestamp = sdf.format(new Date());
        String backupFileName = String.format("redis_full_%s.rdb", timestamp);
        File backupFile = new File(backupPath, backupFileName);

        backupFile.getParentFile().mkdirs();

        ProcessBuilder pb = new ProcessBuilder();
        pb.redirectErrorStream(true);

        String redisCli = buildRedisCliCommand(host, port, password, "BGSAVE");

        pb.command("sh", "-c", redisCli);

        Process process = pb.start();
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new RuntimeException("Redis BGSAVE命令执行失败，退出码: " + exitCode);
        }

        String rdbPath = findRdbFile();
        if (rdbPath != null) {
            File rdbFile = new File(rdbPath);
            if (rdbFile.exists()) {
                Files.copy(rdbFile.toPath(), backupFile.toPath());
            }
        }

        File compressedFile = compressFile(backupFile);
        backupFile.delete();

        return compressedFile;
    }

    @Override
    public File executeIncrementalBackup(Map<String, String> connConfig, String backupPath, String targetName) throws Exception {
        String host = connConfig.get("host");
        String port = connConfig.get("port");
        String password = connConfig.get("password");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String timestamp = sdf.format(new Date());
        String backupFileName = String.format("redis_aof_%s.aof", timestamp);
        File backupFile = new File(backupPath, backupFileName);

        backupFile.getParentFile().mkdirs();

        ProcessBuilder pb = new ProcessBuilder();
        pb.redirectErrorStream(true);

        String redisCli = buildRedisCliCommand(host, port, password, "LASTSAVE");

        pb.command("sh", "-c", redisCli + " > " + backupFile.getAbsolutePath());

        Process process = pb.start();
        process.waitFor();

        if (backupFile.length() == 0) {
            throw new RuntimeException("Redis增量备份失败，无法获取AOF文件");
        }

        return backupFile;
    }

    @Override
    public boolean verifyBackup(File backupFile, Map<String, String> connConfig) {
        if (!backupFile.exists() || backupFile.length() == 0) {
            return false;
        }

        String fileName = backupFile.getName().toLowerCase();

        if (fileName.endsWith(".rdb") || fileName.endsWith(".rdb.gz")) {
            return verifyRdbFile(backupFile);
        }

        if (fileName.endsWith(".aof") || fileName.endsWith(".aof.gz")) {
            return verifyAofFile(backupFile);
        }

        return backupFile.exists();
    }

    @Override
    public boolean restoreBackup(File backupFile, Map<String, String> connConfig, String targetName) throws Exception {
        String host = connConfig.get("host");
        String port = connConfig.get("port");
        String password = connConfig.get("password");

        File tempFile = backupFile;

        if (backupFile.getName().endsWith(".gz")) {
            tempFile = gunzipFile(backupFile);
        }

        ProcessBuilder pb = new ProcessBuilder();
        pb.redirectErrorStream(true);

        String redisCli = buildRedisCliCommand(host, port, password, null);

        if (tempFile.getName().endsWith(".rdb")) {
            pb.command("sh", "-c", "cp " + tempFile.getAbsolutePath() + " /var/lib/redis/dump.rdb");
        } else {
            pb.command(redisCli, "DEBUG", "LOAD", tempFile.getAbsolutePath());
        }

        Process process = pb.start();
        int exitCode = process.waitFor();

        if (tempFile != backupFile) {
            tempFile.delete();
        }

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

    private String buildRedisCliCommand(String host, String port, String password, String command) {
        StringBuilder cmd = new StringBuilder("redis-cli");

        if (host != null && !host.isEmpty()) {
            cmd.append(" -h ").append(host);
        }

        if (port != null && !port.isEmpty()) {
            cmd.append(" -p ").append(port);
        }

        if (password != null && !password.isEmpty()) {
            cmd.append(" -a ").append(password);
        }

        if (command != null && !command.isEmpty()) {
            cmd.append(" ").append(command);
        }

        return cmd.toString();
    }

    private String findRdbFile() {
        String[] possiblePaths = {
            "/var/lib/redis/dump.rdb",
            "/etc/redis/dump.rdb",
            "/data/redis/dump.rdb",
            System.getProperty("user.home") + "/dump.rdb"
        };

        for (String path : possiblePaths) {
            File file = new File(path);
            if (file.exists()) {
                return path;
            }
        }

        return null;
    }

    private boolean verifyRdbFile(File file) {
        try {
            if (file.getName().endsWith(".gz")) {
                try (InputStream is = new FileInputStream(file);
                     java.util.zip.GZIPInputStream gis = new java.util.zip.GZIPInputStream(is)) {
                    byte[] header = new byte[5];
                    return gis.read(header) == 5 && new String(header).startsWith("REDIS");
                }
            } else {
                try (FileInputStream fis = new FileInputStream(file)) {
                    byte[] header = new byte[5];
                    return fis.read(header) == 5 && new String(header).startsWith("REDIS");
                }
            }
        } catch (Exception e) {
            return false;
        }
    }

    private boolean verifyAofFile(File file) {
        try {
            BufferedReader reader;
            if (file.getName().endsWith(".gz")) {
                reader = new BufferedReader(new InputStreamReader(
                    new java.util.zip.GZIPInputStream(new FileInputStream(file))));
            } else {
                reader = new BufferedReader(new FileReader(file));
            }

            String firstLine = reader.readLine();
            reader.close();

            return firstLine != null && (firstLine.startsWith("*") || firstLine.contains("REDIS"));
        } catch (Exception e) {
            return false;
        }
    }

    private File compressFile(File sourceFile) throws IOException {
        File compressedFile = new File(sourceFile.getAbsolutePath() + ".gz");

        try (FileInputStream fis = new FileInputStream(sourceFile);
             FileOutputStream fos = new FileOutputStream(compressedFile);
             java.util.zip.GZIPOutputStream gzos = new java.util.zip.GZIPOutputStream(fos)) {
            byte[] buffer = new byte[8192];
            int len;
            while ((len = fis.read(buffer)) != -1) {
                gzos.write(buffer, 0, len);
            }
        }

        return compressedFile;
    }

    private File gunzipFile(File gzipFile) throws IOException {
        String outputPath = gzipFile.getAbsolutePath().replaceAll("\\.gz$", "");
        File outputFile = new File(outputPath);

        try (FileInputStream fis = new FileInputStream(gzipFile);
             java.util.zip.GZIPInputStream gis = new java.util.zip.GZIPInputStream(fis);
             FileOutputStream fos = new FileOutputStream(outputFile)) {
            byte[] buffer = new byte[8192];
            int len;
            while ((len = gis.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
            }
        }

        return outputFile;
    }
}
