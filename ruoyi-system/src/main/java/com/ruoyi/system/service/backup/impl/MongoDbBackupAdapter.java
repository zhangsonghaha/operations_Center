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
 * MongoDB 备份适配器
 *
 * @author ruoyi
 */
@Component
public class MongoDbBackupAdapter implements IDbBackupAdapter {

    @Override
    public String getDbType() {
        return "mongodb";
    }

    @Override
    public File executeFullBackup(Map<String, String> connConfig, String backupPath, String targetName) throws Exception {
        String host = connConfig.get("host");
        String port = connConfig.get("port");
        String username = connConfig.get("username");
        String password = connConfig.get("password");
        String database = connConfig.get("database");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String timestamp = sdf.format(new Date());
        String backupDirName = String.format("mongodb_%s_full_%s",
                database != null ? database : "all", timestamp);
        File backupDir = new File(backupPath, backupDirName);

        backupDir.mkdirs();

        ProcessBuilder pb = new ProcessBuilder();
        pb.redirectErrorStream(true);

        String mongoUri = buildMongoUri(host, port, username, password, database);

        if (targetName != null && !targetName.isEmpty()) {
            pb.command("mongodump", "--uri", mongoUri, "--collection", targetName,
                    "--out", backupDir.getAbsolutePath());
        } else {
            pb.command("mongodump", "--uri", mongoUri,
                    "--out", backupDir.getAbsolutePath());
        }

        Process process = pb.start();
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            deleteDirectory(backupDir);
            throw new RuntimeException("MongoDB备份失败，退出码: " + exitCode);
        }

        File tarFile = createTarArchive(backupDir, backupPath);
        deleteDirectory(backupDir);

        return tarFile;
    }

    @Override
    public File executeIncrementalBackup(Map<String, String> connConfig, String backupPath, String targetName) throws Exception {
        String host = connConfig.get("host");
        String port = connConfig.get("port");
        String username = connConfig.get("username");
        String password = connConfig.get("password");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String timestamp = sdf.format(new Date());
        String backupDirName = String.format("mongodb_%s_incremental_%s",
                targetName != null ? targetName : "all", timestamp);
        File backupDir = new File(backupPath, backupDirName);

        backupDir.mkdirs();

        ProcessBuilder pb = new ProcessBuilder();
        pb.redirectErrorStream(true);

        String mongoUri = buildMongoUri(host, port, username, password, connConfig.get("database"));

        pb.command("mongodump", "--uri", mongoUri, "--oplog",
                "--out", backupDir.getAbsolutePath());

        Process process = pb.start();
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            deleteDirectory(backupDir);
            throw new RuntimeException("MongoDB增量备份失败，退出码: " + exitCode);
        }

        File tarFile = createTarArchive(backupDir, backupPath);
        deleteDirectory(backupDir);

        return tarFile;
    }

    @Override
    public boolean verifyBackup(File backupFile, Map<String, String> connConfig) {
        if (!backupFile.exists() || backupFile.length() == 0) {
            return false;
        }

        if (backupFile.getName().endsWith(".tar.gz") || backupFile.getName().endsWith(".tgz")) {
            return verifyTarGz(backupFile);
        }

        return backupFile.exists();
    }

    @Override
    public boolean restoreBackup(File backupFile, Map<String, String> connConfig, String targetName) throws Exception {
        String host = connConfig.get("host");
        String port = connConfig.get("port");
        String username = connConfig.get("username");
        String password = connConfig.get("password");
        String database = connConfig.get("database");

        String mongoUri = buildMongoUri(host, port, username, password, database);

        ProcessBuilder pb = new ProcessBuilder();
        pb.redirectErrorStream(true);

        File tempDir = new File(backupFile.getParent(), "restore_temp");
        tempDir.mkdirs();

        try {
            extractTarArchive(backupFile, tempDir);

            if (targetName != null && !targetName.isEmpty()) {
                pb.command("mongorestore", "--uri", mongoUri, "--collection", targetName,
                        "--nsInclude", database + "." + targetName, tempDir.getAbsolutePath());
            } else {
                pb.command("mongorestore", "--uri", mongoUri, tempDir.getAbsolutePath());
            }

            Process process = pb.start();
            int exitCode = process.waitFor();

            return exitCode == 0;
        } finally {
            deleteDirectory(tempDir);
        }
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

    private String buildMongoUri(String host, String port, String username, String password, String database) {
        StringBuilder uri = new StringBuilder("mongodb://");

        if (username != null && !username.isEmpty()) {
            uri.append(username);
            if (password != null && !password.isEmpty()) {
                uri.append(":").append(password);
            }
            uri.append("@");
        }

        uri.append(host);
        if (port != null && !port.isEmpty()) {
            uri.append(":").append(port);
        }

        if (database != null && !database.isEmpty()) {
            uri.append("/").append(database);
        }

        uri.append("?authSource=admin");

        return uri.toString();
    }

    private File createTarArchive(File sourceDir, String outputPath) throws Exception {
        String tarFileName = sourceDir.getName() + ".tar.gz";
        File tarFile = new File(outputPath, tarFileName);

        ProcessBuilder pb = new ProcessBuilder();
        pb.command("tar", "-czf", tarFile.getAbsolutePath(), "-C", sourceDir.getParent(), sourceDir.getName());
        pb.redirectErrorStream(true);

        Process process = pb.start();
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new RuntimeException("创建tar.gz归档失败，退出码: " + exitCode);
        }

        return tarFile;
    }

    private void extractTarArchive(File tarFile, File outputDir) throws Exception {
        ProcessBuilder pb = new ProcessBuilder();
        pb.command("tar", "-xzf", tarFile.getAbsolutePath(), "-C", outputDir.getParent());
        pb.redirectErrorStream(true);

        Process process = pb.start();
        int exitCode = process.waitFor();

        if (exitCode != 0) {
            throw new RuntimeException("解压tar.gz归档失败，退出码: " + exitCode);
        }
    }

    private boolean verifyTarGz(File file) {
        try {
            ProcessBuilder pb = new ProcessBuilder();
            pb.command("tar", "-tzf", file.getAbsolutePath());
            pb.redirectErrorStream(true);

            Process process = pb.start();
            int exitCode = process.waitFor();

            return exitCode == 0;
        } catch (Exception e) {
            return false;
        }
    }

    private void deleteDirectory(File dir) {
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteDirectory(file);
                }
            }
        }
        dir.delete();
    }
}
