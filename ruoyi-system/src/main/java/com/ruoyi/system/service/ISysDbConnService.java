package com.ruoyi.system.service;

import java.util.List;
import com.ruoyi.system.domain.SysDbConn;

/**
 * 数据库连接配置Service接口
 * 
 * @author ruoyi
 */
public interface ISysDbConnService 
{
    /**
     * 查询数据库连接配置
     * 
     * @param connId 数据库连接配置主键
     * @return 数据库连接配置
     */
    public SysDbConn selectSysDbConnByConnId(Long connId);

    /**
     * 查询数据库连接配置列表
     * 
     * @param sysDbConn 数据库连接配置
     * @return 数据库连接配置集合
     */
    public List<SysDbConn> selectSysDbConnList(SysDbConn sysDbConn);

    /**
     * 新增数据库连接配置
     * 
     * @param sysDbConn 数据库连接配置
     * @return 结果
     */
    public int insertSysDbConn(SysDbConn sysDbConn);

    /**
     * 修改数据库连接配置
     * 
     * @param sysDbConn 数据库连接配置
     * @return 结果
     */
    public int updateSysDbConn(SysDbConn sysDbConn);

    /**
     * 批量删除数据库连接配置
     * 
     * @param connIds 需要删除的数据库连接配置主键集合
     * @return 结果
     */
    public int deleteSysDbConnByConnIds(Long[] connIds);

    /**
     * 删除数据库连接配置信息
     * 
     * @param connId 数据库连接配置主键
     * @return 结果
     */
    public int deleteSysDbConnByConnId(Long connId);

    /**
     * 测试数据库连接
     * 
     * @param sysDbConn 数据库连接配置
     * @return 错误信息，如果成功返回null
     */
    public String testConnection(SysDbConn sysDbConn);
}
