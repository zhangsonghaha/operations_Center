package com.ruoyi.system.service;

import java.util.List;
import java.util.Map;

/**
 * 数据库执行Service接口
 * 
 * @author ruoyi
 */
public interface IDbExecuteService 
{
    /**
     * 获取数据库表列表
     * 
     * @param connId 连接ID
     * @return 表名列表
     */
    public List<String> getTableList(Long connId);

    /**
     * 获取数据库元数据（表、列、函数等）
     * 
     * @param connId 连接ID
     * @return 元数据对象
     */
    public Map<String, Object> getDatabaseMetadata(Long connId);

    /**
     * 获取数据库结构（数据库名称、表、视图、存储过程、函数等）
     * 
     * @param connId 连接ID
     * @return 数据库结构对象
     */
    public Map<String, Object> getDatabaseSchema(Long connId);

    /**
     * 获取表结构详情（列、主键、索引等）
     * 
     * @param connId 连接ID
     * @param tableName 表名
     * @return 表结构详情
     */
    public Map<String, Object> getTableStructure(Long connId, String tableName);

    /**
     * 获取存储过程定义
     * 
     * @param connId 连接ID
     * @param procName 存储过程名
     * @return 存储过程定义
     */
    public Map<String, Object> getProcedureDefinition(Long connId, String procName);

    /**
     * 获取函数定义
     * 
     * @param connId 连接ID
     * @param funcName 函数名
     * @return 函数定义
     */
    public Map<String, Object> getFunctionDefinition(Long connId, String funcName);

    /**
     * 更新存储过程
     * 
     * @param connId 连接ID
     * @param procName 存储过程名
     * @param definition 完整定义SQL
     * @return 影响行数
     */
    public int updateProcedure(Long connId, String procName, String definition);

    /**
     * 更新函数
     * 
     * @param connId 连接ID
     * @param funcName 函数名
     * @param definition 完整定义SQL
     * @return 影响行数
     */
    public int updateFunction(Long connId, String funcName, String definition);

    /**
     * 执行查询SQL
     * 
     * @param connId 连接ID
     * @param sql SQL语句
     * @return 结果集
     */
    public List<Map<String, Object>> executeSelect(Long connId, String sql);

    /**
     * 执行增删改SQL
     * 
     * @param connId 连接ID
     * @param sql SQL语句
     * @return 影响行数
     */
    public int executeUpdate(Long connId, String sql);
}
