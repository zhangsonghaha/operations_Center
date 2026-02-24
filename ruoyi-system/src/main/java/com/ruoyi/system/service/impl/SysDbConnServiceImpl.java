package com.ruoyi.system.service.impl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.mapper.SysDbConnMapper;
import com.ruoyi.system.domain.SysDbConn;
import com.ruoyi.system.service.ISysDbConnService;

/**
 * 数据库连接配置Service业务层处理
 * 
 * @author ruoyi
 */
@Service
public class SysDbConnServiceImpl implements ISysDbConnService 
{
    @Autowired
    private SysDbConnMapper sysDbConnMapper;

    /**
     * 查询数据库连接配置
     * 
     * @param connId 数据库连接配置主键
     * @return 数据库连接配置
     */
    @Override
    public SysDbConn selectSysDbConnByConnId(Long connId)
    {
        return sysDbConnMapper.selectSysDbConnByConnId(connId);
    }

    /**
     * 查询数据库连接配置列表
     * 
     * @param sysDbConn 数据库连接配置
     * @return 数据库连接配置
     */
    @Override
    public List<SysDbConn> selectSysDbConnList(SysDbConn sysDbConn)
    {
        return sysDbConnMapper.selectSysDbConnList(sysDbConn);
    }

    /**
     * 新增数据库连接配置
     * 
     * @param sysDbConn 数据库连接配置
     * @return 结果
     */
    @Override
    public int insertSysDbConn(SysDbConn sysDbConn)
    {
        sysDbConn.setCreateTime(DateUtils.getNowDate());
        return sysDbConnMapper.insertSysDbConn(sysDbConn);
    }

    /**
     * 修改数据库连接配置
     * 
     * @param sysDbConn 数据库连接配置
     * @return 结果
     */
    @Override
    public int updateSysDbConn(SysDbConn sysDbConn)
    {
        sysDbConn.setUpdateTime(DateUtils.getNowDate());
        return sysDbConnMapper.updateSysDbConn(sysDbConn);
    }

    /**
     * 批量删除数据库连接配置
     * 
     * @param connIds 需要删除的数据库连接配置主键
     * @return 结果
     */
    @Override
    public int deleteSysDbConnByConnIds(Long[] connIds)
    {
        return sysDbConnMapper.deleteSysDbConnByConnIds(connIds);
    }

    /**
     * 删除数据库连接配置信息
     * 
     * @param connId 数据库连接配置主键
     * @return 结果
     */
    @Override
    public int deleteSysDbConnByConnId(Long connId)
    {
        return sysDbConnMapper.deleteSysDbConnByConnId(connId);
    }

    /**
     * 测试数据库连接
     * 
     * @param sysDbConn 数据库连接配置
     * @return 错误信息，如果成功返回null
     */
    @Override
    public String testConnection(SysDbConn sysDbConn) {
        // 如果是本地连接，尝试直接连接，不加数据库名进行测试（因为可能数据库名填错了，但连接是通的）
        // 或者保留原逻辑，但捕获Unknown database异常并提示更友好的信息
        String url = "jdbc:mysql://" + sysDbConn.getHost() + ":" + sysDbConn.getPort() + "/" + sysDbConn.getDbName() 
                   + "?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=GMT%2B8";
        try {
            // 加载驱动，这里只演示MySQL，实际可能需要动态加载不同驱动
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (Connection conn = DriverManager.getConnection(url, sysDbConn.getUsername(), sysDbConn.getPassword())) {
                return null;
            }
        } catch (Exception e) {
            String msg = e.getMessage();
            if (msg != null && msg.contains("Unknown database")) {
                 return "数据库 '" + sysDbConn.getDbName() + "' 不存在，请检查数据库名称是否正确";
            }
            return msg;
        }
    }
}
