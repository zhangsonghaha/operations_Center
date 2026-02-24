package com.ruoyi.system.service.impl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.SysDbConn;
import com.ruoyi.system.domain.SysDbLog;
import com.ruoyi.system.mapper.SysDbConnMapper;
import com.ruoyi.system.mapper.SysDbLogMapper;
import com.ruoyi.system.service.IDbExecuteService;

import com.ruoyi.common.core.redis.RedisCache;
import java.util.concurrent.TimeUnit;

/**
 * 数据库执行Service业务层处理
 * 
 * @author ruoyi
 */
@Service
public class DbExecuteServiceImpl implements IDbExecuteService 
{
    @Autowired
    private SysDbConnMapper sysDbConnMapper;

    @Autowired
    private SysDbLogMapper sysDbLogMapper;
    
    @Autowired
    private RedisCache redisCache;

    /**
     * 获取数据库连接
     */
    private Connection getConnection(Long connId) throws Exception
    {
        SysDbConn connInfo = sysDbConnMapper.selectSysDbConnByConnId(connId);
        if (connInfo == null)
        {
            throw new Exception("数据库连接不存在");
        }
        
        if ("1".equals(connInfo.getStatus()))
        {
            throw new Exception("数据库连接已停用");
        }
        
        String url = "jdbc:mysql://" + connInfo.getHost() + ":" + connInfo.getPort() + "/" + connInfo.getDbName() 
                   + "?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=GMT%2B8";
        
        // 加载驱动
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(url, connInfo.getUsername(), connInfo.getPassword());
    }

    /**
     * 记录日志
     */
    private void recordLog(Long connId, String sql, long startTime, Exception e)
    {
        long endTime = System.currentTimeMillis();
        SysDbLog log = new SysDbLog();
        log.setConnId(connId);
        log.setSqlContent(StringUtils.substring(sql, 0, 2000)); // 截取过长的SQL
        log.setCostTime(endTime - startTime);
        log.setCreateTime(DateUtils.getNowDate());
        try {
            log.setCreateBy(SecurityUtils.getUsername());
        } catch (Exception ex) {
            log.setCreateBy("system");
        }

        if (e != null)
        {
            log.setStatus("1");
            log.setErrorMsg(StringUtils.substring(e.getMessage(), 0, 2000));
        }
        else
        {
            log.setStatus("0");
        }
        sysDbLogMapper.insertSysDbLog(log);
    }

    @Override
    public List<String> getTableList(Long connId)
    {
        List<String> tableList = new ArrayList<>();
        try (Connection conn = getConnection(connId))
        {
            String catalog = conn.getCatalog();
            ResultSet rs = conn.getMetaData().getTables(catalog, null, "%", new String[] { "TABLE", "VIEW" });
            while (rs.next())
            {
                tableList.add(rs.getString("TABLE_NAME"));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new RuntimeException("获取表列表失败: " + e.getMessage());
        }
        return tableList;
    }

    @Override
    public Map<String, Object> getDatabaseSchema(Long connId)
    {
        String cacheKey = "db_schema:" + connId;
        Map<String, Object> cachedSchema = redisCache.getCacheObject(cacheKey);
        if (cachedSchema != null) {
            return cachedSchema;
        }

        Map<String, Object> schema = new HashMap<>();
        List<Map<String, Object>> tables = new ArrayList<>();
        List<Map<String, Object>> views = new ArrayList<>();
        List<Map<String, Object>> procedures = new ArrayList<>();
        List<Map<String, Object>> functions = new ArrayList<>();
        
        try (Connection conn = getConnection(connId))
        {
            String catalog = conn.getCatalog();
            String dbName = conn.getCatalog();
            java.sql.DatabaseMetaData dbMeta = conn.getMetaData();
            
            schema.put("dbName", dbName);
            schema.put("dbType", dbMeta.getDatabaseProductName());
            schema.put("dbVersion", dbMeta.getDatabaseProductVersion());
            
            // 1. 获取所有表
            try (ResultSet rs = dbMeta.getTables(catalog, null, "%", new String[] { "TABLE" }))
            {
                while (rs.next())
                {
                    Map<String, Object> tableInfo = new HashMap<>();
                    tableInfo.put("name", rs.getString("TABLE_NAME"));
                    tableInfo.put("type", "TABLE");
                    tableInfo.put("remarks", rs.getString("REMARKS"));
                    tables.add(tableInfo);
                }
            }
            
            // 2. 获取所有视图
            try (ResultSet rs = dbMeta.getTables(catalog, null, "%", new String[] { "VIEW" }))
            {
                while (rs.next())
                {
                    Map<String, Object> viewInfo = new HashMap<>();
                    viewInfo.put("name", rs.getString("TABLE_NAME"));
                    viewInfo.put("type", "VIEW");
                    viewInfo.put("remarks", rs.getString("REMARKS"));
                    views.add(viewInfo);
                }
            }
            
            // 3. 获取存储过程
            try (ResultSet rsProc = dbMeta.getProcedures(catalog, null, "%"))
            {
                while (rsProc.next())
                {
                    Map<String, Object> procInfo = new HashMap<>();
                    procInfo.put("name", rsProc.getString("PROCEDURE_NAME"));
                    procInfo.put("type", "PROCEDURE");
                    procInfo.put("remarks", rsProc.getString("REMARKS"));
                    procedures.add(procInfo);
                }
            }
            
            // 4. 获取函数
            try 
            {
                try (ResultSet rsFunc = dbMeta.getFunctions(catalog, null, "%"))
                {
                    while (rsFunc.next())
                    {
                        Map<String, Object> funcInfo = new HashMap<>();
                        funcInfo.put("name", rsFunc.getString("FUNCTION_NAME"));
                        funcInfo.put("type", "FUNCTION");
                        funcInfo.put("remarks", rsFunc.getString("REMARKS"));
                        functions.add(funcInfo);
                    }
                }
            }
            catch (Throwable e) 
            {
                // 忽略不支持的情况
            }
            
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new RuntimeException("获取数据库结构失败: " + e.getMessage());
        }
        
        schema.put("tables", tables);
        schema.put("views", views);
        schema.put("procedures", procedures);
        schema.put("functions", functions);
        
        // 缓存，设置过期时间为 1 小时
        redisCache.setCacheObject(cacheKey, schema, 1, TimeUnit.HOURS);
        
        return schema;
    }

    @Override
    public Map<String, Object> getTableStructure(Long connId, String tableName)
    {
        Map<String, Object> structure = new HashMap<>();
        List<Map<String, Object>> columns = new ArrayList<>();
        List<Map<String, Object>> indexes = new ArrayList<>();
        List<String> primaryKeys = new ArrayList<>();
        
        try (Connection conn = getConnection(connId))
        {
            String catalog = conn.getCatalog();
            java.sql.DatabaseMetaData dbMeta = conn.getMetaData();
            
            // 1. 获取列信息
            try (ResultSet rsCols = dbMeta.getColumns(catalog, null, tableName, "%"))
            {
                while (rsCols.next())
                {
                    Map<String, Object> col = new HashMap<>();
                    col.put("name", rsCols.getString("COLUMN_NAME"));
                    col.put("type", rsCols.getString("TYPE_NAME"));
                    col.put("size", rsCols.getInt("COLUMN_SIZE"));
                    col.put("nullable", rsCols.getInt("NULLABLE") == 1);
                    col.put("defaultValue", rsCols.getString("COLUMN_DEF"));
                    col.put("remarks", rsCols.getString("REMARKS"));
                    col.put("position", rsCols.getInt("ORDINAL_POSITION"));
                    columns.add(col);
                }
            }
            
            // 2. 获取主键
            try (ResultSet rsPk = dbMeta.getPrimaryKeys(catalog, null, tableName))
            {
                while (rsPk.next())
                {
                    primaryKeys.add(rsPk.getString("COLUMN_NAME"));
                }
            }
            
            // 3. 获取索引
            try (ResultSet rsIdx = dbMeta.getIndexInfo(catalog, null, tableName, false, false))
            {
                Map<String, Map<String, Object>> indexMap = new HashMap<>();
                
                while (rsIdx.next())
                {
                    String idxName = rsIdx.getString("INDEX_NAME");
                    // 跳过主键索引
                    if (idxName == null || "PRIMARY".equals(idxName)) continue;
                    
                    if (!indexMap.containsKey(idxName))
                    {
                        Map<String, Object> idx = new HashMap<>();
                        idx.put("name", idxName);
                        idx.put("unique", !rsIdx.getBoolean("NON_UNIQUE"));
                        idx.put("columns", new ArrayList<String>());
                        idx.put("type", getIndexTypeName(rsIdx.getShort("TYPE")));
                        indexMap.put(idxName, idx);
                    }
                    
                    @SuppressWarnings("unchecked")
                    List<String> idxCols = (List<String>) indexMap.get(idxName).get("columns");
                    idxCols.add(rsIdx.getString("COLUMN_NAME"));
                }
                
                indexes.addAll(indexMap.values());
            }
            
            // 4. 获取表注释（MySQL特有）
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT TABLE_COMMENT FROM information_schema.TABLES WHERE TABLE_SCHEMA = '" + catalog + "' AND TABLE_NAME = '" + tableName + "'"))
            {
                if (rs.next())
                {
                    structure.put("tableComment", rs.getString("TABLE_COMMENT"));
                }
            }
            catch (Exception e)
            {
                // 忽略错误，某些数据库可能不支持
            }
            
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new RuntimeException("获取表结构失败: " + e.getMessage());
        }
        
        structure.put("tableName", tableName);
        structure.put("columns", columns);
        structure.put("primaryKeys", primaryKeys);
        structure.put("indexes", indexes);
        
        return structure;
    }
    
    private String getIndexTypeName(short type)
    {
        switch (type)
        {
            case 0: return "Statistic";
            case 1: return "Clustered";
            case 2: return "Hashed";
            case 3: return "Other";
            default: return "Unknown";
        }
    }

    @Override
    public Map<String, Object> getProcedureDefinition(Long connId, String procName)
    {
        Map<String, Object> definition = new HashMap<>();
        
        try (Connection conn = getConnection(connId))
        {
            String dbName = conn.getCatalog();
            
            // MySQL获取存储过程定义
            String sql = "SHOW CREATE PROCEDURE " + dbName + "." + procName;
            
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql))
            {
                if (rs.next())
                {
                    definition.put("name", procName);
                    definition.put("sqlMode", rs.getString("sql_mode"));
                    definition.put("characterSetClient", rs.getString("character_set_client"));
                    definition.put("collationConnection", rs.getString("collation_connection"));
                    definition.put("databaseCollation", rs.getString("Database Collation"));
                    definition.put("definition", rs.getString("Create Procedure"));
                }
                else
                {
                    throw new RuntimeException("存储过程不存在: " + procName);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new RuntimeException("获取存储过程定义失败: " + e.getMessage());
        }
        
        return definition;
    }

    @Override
    public Map<String, Object> getFunctionDefinition(Long connId, String funcName)
    {
        Map<String, Object> definition = new HashMap<>();
        
        try (Connection conn = getConnection(connId))
        {
            String dbName = conn.getCatalog();
            
            // MySQL获取函数定义
            String sql = "SHOW CREATE FUNCTION " + dbName + "." + funcName;
            
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql))
            {
                if (rs.next())
                {
                    definition.put("name", funcName);
                    definition.put("sqlMode", rs.getString("sql_mode"));
                    definition.put("characterSetClient", rs.getString("character_set_client"));
                    definition.put("collationConnection", rs.getString("collation_connection"));
                    definition.put("databaseCollation", rs.getString("Database Collation"));
                    definition.put("definition", rs.getString("Create Function"));
                }
                else
                {
                    throw new RuntimeException("函数不存在: " + funcName);
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new RuntimeException("获取函数定义失败: " + e.getMessage());
        }
        
        return definition;
    }

    @Override
    public int updateProcedure(Long connId, String procName, String definition)
    {
        long startTime = System.currentTimeMillis();
        Exception exception = null;
        int rows = 0;

        try (Connection conn = getConnection(connId);
             Statement stmt = conn.createStatement())
        {
            String dbName = conn.getCatalog();
            
            // MySQL需要先DROP再CREATE
            String dropSql = "DROP PROCEDURE IF EXISTS " + dbName + "." + procName;
            stmt.executeUpdate(dropSql);
            
            // 创建新存储过程
            stmt.executeUpdate(definition);
            rows = 1;
        }
        catch (Exception e)
        {
            exception = e;
            throw new RuntimeException("更新存储过程失败: " + e.getMessage());
        }
        finally
        {
            recordLog(connId, "UPDATE PROCEDURE: " + procName, startTime, exception);
        }
        
        return rows;
    }

    @Override
    public int updateFunction(Long connId, String funcName, String definition)
    {
        long startTime = System.currentTimeMillis();
        Exception exception = null;
        int rows = 0;

        try (Connection conn = getConnection(connId);
             Statement stmt = conn.createStatement())
        {
            String dbName = conn.getCatalog();
            
            // MySQL需要先DROP再CREATE
            String dropSql = "DROP FUNCTION IF EXISTS " + dbName + "." + funcName;
            stmt.executeUpdate(dropSql);
            
            // 创建新函数
            stmt.executeUpdate(definition);
            rows = 1;
        }
        catch (Exception e)
        {
            exception = e;
            throw new RuntimeException("更新函数失败: " + e.getMessage());
        }
        finally
        {
            recordLog(connId, "UPDATE FUNCTION: " + funcName, startTime, exception);
        }
        
        return rows;
    }

    @Override
    public Map<String, Object> getDatabaseMetadata(Long connId)
    {
        String cacheKey = "db_meta:" + connId;
        Map<String, Object> cachedMetadata = redisCache.getCacheObject(cacheKey);
        if (cachedMetadata != null) {
            return cachedMetadata;
        }

        Map<String, Object> metadata = new HashMap<>();
        List<Map<String, Object>> tables = new ArrayList<>();
        List<String> functions = new ArrayList<>();
        
        try (Connection conn = getConnection(connId))
        {
            String catalog = conn.getCatalog();
            java.sql.DatabaseMetaData dbMeta = conn.getMetaData();
            
            // 1. 获取所有表
            Map<String, Map<String, Object>> tableMap = new HashMap<>();
            try (ResultSet rs = dbMeta.getTables(catalog, null, "%", new String[] { "TABLE", "VIEW" }))
            {
                while (rs.next())
                {
                    String tableName = rs.getString("TABLE_NAME");
                    Map<String, Object> tableInfo = new HashMap<>();
                    tableInfo.put("name", tableName);
                    tableInfo.put("type", rs.getString("TABLE_TYPE"));
                    tableInfo.put("columns", new ArrayList<Map<String, String>>());
                    
                    tables.add(tableInfo);
                    tableMap.put(tableName, tableInfo);
                }
            }
            
            // 2. 批量获取所有列
            try (ResultSet rsCols = dbMeta.getColumns(catalog, null, "%", "%"))
            {
                while (rsCols.next())
                {
                    String tableName = rsCols.getString("TABLE_NAME");
                    Map<String, Object> tableInfo = tableMap.get(tableName);
                    if (tableInfo != null)
                    {
                        @SuppressWarnings("unchecked")
                        List<Map<String, String>> columns = (List<Map<String, String>>) tableInfo.get("columns");
                        Map<String, String> col = new HashMap<>();
                        col.put("name", rsCols.getString("COLUMN_NAME"));
                        col.put("type", rsCols.getString("TYPE_NAME"));
                        // col.put("remarks", rsCols.getString("REMARKS"));
                        columns.add(col);
                    }
                }
            }
            
            // 3. 获取存储过程
            try (ResultSet rsProc = dbMeta.getProcedures(catalog, null, "%"))
            {
                while (rsProc.next())
                {
                    functions.add(rsProc.getString("PROCEDURE_NAME"));
                }
            }
            
            // 4. 获取函数 (JDBC 4.0+)
            try 
            {
                try (ResultSet rsFunc = dbMeta.getFunctions(catalog, null, "%"))
                {
                    while (rsFunc.next())
                    {
                        functions.add(rsFunc.getString("FUNCTION_NAME"));
                    }
                }
            }
            catch (Throwable e) 
            {
                // 忽略不支持的情况
            }
            
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new RuntimeException("获取元数据失败: " + e.getMessage());
        }
        
        metadata.put("tables", tables);
        metadata.put("functions", functions);
        
        // 缓存元数据，设置过期时间为 1 小时
        redisCache.setCacheObject(cacheKey, metadata, 1, TimeUnit.HOURS);
        
        return metadata;
    }

    @Override
    public List<Map<String, Object>> executeSelect(Long connId, String sql)
    {
        long startTime = System.currentTimeMillis();
        List<Map<String, Object>> resultList = new ArrayList<>();
        Exception exception = null;

        try (Connection conn = getConnection(connId);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql))
        {
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            while (rs.next())
            {
                Map<String, Object> rowMap = new HashMap<>();
                for (int i = 1; i <= columnCount; i++)
                {
                    rowMap.put(metaData.getColumnLabel(i), rs.getObject(i));
                }
                resultList.add(rowMap);
            }
        }
        catch (Exception e)
        {
            exception = e;
            throw new RuntimeException("SQL执行失败: " + e.getMessage());
        }
        finally
        {
            recordLog(connId, sql, startTime, exception);
        }
        return resultList;
    }

    @Override
    public int executeUpdate(Long connId, String sql)
    {
        long startTime = System.currentTimeMillis();
        int rows = 0;
        Exception exception = null;

        try (Connection conn = getConnection(connId);
             Statement stmt = conn.createStatement())
        {
            rows = stmt.executeUpdate(sql);
        }
        catch (Exception e)
        {
            exception = e;
            throw new RuntimeException("SQL执行失败: " + e.getMessage());
        }
        finally
        {
            recordLog(connId, sql, startTime, exception);
        }
        return rows;
    }
}
