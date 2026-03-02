package com.ruoyi.web.mapper;

import com.ruoyi.web.domain.OpsDeployLog;
import java.util.List;

/**
 * 部署日志Mapper接口
 * 
 * @author ruoyi
 */
public interface OpsDeployLogMapper {
    
    /**
     * 查询部署日志
     */
    public OpsDeployLog selectOpsDeployLogById(Long logId);
    
    /**
     * 查询部署日志列表
     */
    public List<OpsDeployLog> selectOpsDeployLogList(OpsDeployLog opsDeployLog);
    
    /**
     * 新增部署日志
     */
    public int insertOpsDeployLog(OpsDeployLog opsDeployLog);
    
    /**
     * 修改部署日志
     */
    public int updateOpsDeployLog(OpsDeployLog opsDeployLog);
    
    /**
     * 追加日志内容（使用数据库 CONCAT 避免读取整个日志）
     * 
     * @param logId 日志ID
     * @param content 要追加的内容
     * @param updateTime 更新时间
     * @return 影响行数
     */
    public int appendLogContent(@org.apache.ibatis.annotations.Param("logId") Long logId, 
                                @org.apache.ibatis.annotations.Param("content") String content, 
                                @org.apache.ibatis.annotations.Param("updateTime") java.util.Date updateTime);
    
    /**
     * 删除部署日志
     */
    public int deleteOpsDeployLogById(Long logId);
    
    /**
     * 批量删除部署日志
     */
    public int deleteOpsDeployLogByIds(Long[] logIds);
}
