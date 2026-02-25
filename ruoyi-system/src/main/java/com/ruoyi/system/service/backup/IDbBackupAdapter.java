package com.ruoyi.system.service.backup;

import java.io.File;
import java.util.Map;

/**
 * 数据库备份适配器接口
 * 
 * @author ruoyi
 */
public interface IDbBackupAdapter 
{
    /**
     * 获取适配器支持的数据库类型
     * @return 数据库类型
     */
    String getDbType();

    /**
     * 执行全量备份
     * @param connConfig 连接配置
     * @param backupPath 备份路径
     * @param targetName 备份目标（数据库名或表名）
     * @return 备份文件
     * @throws Exception 备份异常
     */
    File executeFullBackup(Map<String, String> connConfig, String backupPath, String targetName) throws Exception;

    /**
     * 执行增量备份
     * @param connConfig 连接配置
     * @param backupPath 备份路径
     * @param targetName 备份目标
     * @return 备份文件
     * @throws Exception 备份异常
     */
    File executeIncrementalBackup(Map<String, String> connConfig, String backupPath, String targetName) throws Exception;

    /**
     * 验证备份文件
     * @param backupFile 备份文件
     * @param connConfig 连接配置
     * @return 验证结果
     */
    boolean verifyBackup(File backupFile, Map<String, String> connConfig);

    /**
     * 恢复备份
     * @param backupFile 备份文件
     * @param connConfig 连接配置
     * @param targetName 恢复目标
     * @return 恢复结果
     * @throws Exception 恢复异常
     */
    boolean restoreBackup(File backupFile, Map<String, String> connConfig, String targetName) throws Exception;

    /**
     * 获取备份文件MD5
     * @param backupFile 备份文件
     * @return MD5值
     */
    String calculateMd5(File backupFile);
}
