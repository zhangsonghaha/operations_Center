package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.SysDbConn;

/**
 * 数据库连接配置Mapper接口
 * 
 * @author ruoyi
 */
public interface SysDbConnMapper 
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
     * 删除数据库连接配置
     * 
     * @param connId 数据库连接配置主键
     * @return 结果
     */
    public int deleteSysDbConnByConnId(Long connId);

    /**
     * 批量删除数据库连接配置
     * 
     * @param connIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteSysDbConnByConnIds(Long[] connIds);
}
