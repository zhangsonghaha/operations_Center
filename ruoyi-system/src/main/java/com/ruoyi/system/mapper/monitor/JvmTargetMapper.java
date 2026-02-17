package com.ruoyi.system.mapper.monitor;

import java.util.List;
import com.ruoyi.system.domain.monitor.JvmTarget;

/**
 * JMX 监控目标 Mapper 接口
 * 
 * @author ruoyi
 */
public interface JvmTargetMapper 
{
    /**
     * 查询监控目标
     * 
     * @param targetId 监控目标ID
     * @return 监控目标
     */
    public JvmTarget selectJvmTargetById(Long targetId);

    /**
     * 查询监控目标列表
     * 
     * @param jvmTarget 监控目标
     * @return 监控目标集合
     */
    public List<JvmTarget> selectJvmTargetList(JvmTarget jvmTarget);

    /**
     * 新增监控目标
     * 
     * @param jvmTarget 监控目标
     * @return 结果
     */
    public int insertJvmTarget(JvmTarget jvmTarget);

    /**
     * 修改监控目标
     * 
     * @param jvmTarget 监控目标
     * @return 结果
     */
    public int updateJvmTarget(JvmTarget jvmTarget);

    /**
     * 删除监控目标
     * 
     * @param targetId 监控目标ID
     * @return 结果
     */
    public int deleteJvmTargetById(Long targetId);

    /**
     * 批量删除监控目标
     * 
     * @param targetIds 需要删除的数据ID
     * @return 结果
     */
    public int deleteJvmTargetByIds(Long[] targetIds);
}
