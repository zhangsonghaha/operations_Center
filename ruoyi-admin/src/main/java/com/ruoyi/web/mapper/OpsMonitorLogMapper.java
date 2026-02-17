package com.ruoyi.web.mapper;

import java.util.List;
import com.ruoyi.web.domain.OpsMonitorLog;
import org.apache.ibatis.annotations.Param;

/**
 * 服务器监控日志Mapper接口
 */
public interface OpsMonitorLogMapper 
{
    /**
     * 查询服务器监控日志
     * 
     * @param id 服务器监控日志主键
     * @return 服务器监控日志
     */
    public OpsMonitorLog selectOpsMonitorLogById(Long id);

    /**
     * 查询服务器监控日志列表
     * 
     * @param opsMonitorLog 服务器监控日志
     * @return 服务器监控日志集合
     */
    public List<OpsMonitorLog> selectOpsMonitorLogList(OpsMonitorLog opsMonitorLog);

    /**
     * 新增服务器监控日志
     * 
     * @param opsMonitorLog 服务器监控日志
     * @return 结果
     */
    public int insertOpsMonitorLog(OpsMonitorLog opsMonitorLog);

    /**
     * 修改服务器监控日志
     * 
     * @param opsMonitorLog 服务器监控日志
     * @return 结果
     */
    public int updateOpsMonitorLog(OpsMonitorLog opsMonitorLog);

    /**
     * 删除服务器监控日志
     * 
     * @param id 服务器监控日志主键
     * @return 结果
     */
    public int deleteOpsMonitorLogById(Long id);

    /**
     * 批量删除服务器监控日志
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteOpsMonitorLogByIds(Long[] ids);

    /**
     * 清理过期日志
     * @param days 保留天数
     */
    public int cleanExpiredLogs(@Param("days") int days);
}
