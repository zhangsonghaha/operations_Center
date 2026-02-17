package com.ruoyi.system.mapper.monitor;

import java.util.List;
import com.ruoyi.system.domain.monitor.JvmMetric;
import org.apache.ibatis.annotations.Param;

/**
 * JVM 监控指标 Mapper 接口
 * 
 * @author ruoyi
 */
public interface JvmMetricMapper 
{
    /**
     * 查询监控指标
     * 
     * @param metricId 监控指标ID
     * @return 监控指标
     */
    public JvmMetric selectJvmMetricById(Long metricId);

    /**
     * 查询监控指标列表
     * 
     * @param jvmMetric 监控指标
     * @return 监控指标集合
     */
    public List<JvmMetric> selectJvmMetricList(JvmMetric jvmMetric);

    /**
     * 新增监控指标
     * 
     * @param jvmMetric 监控指标
     * @return 结果
     */
    public int insertJvmMetric(JvmMetric jvmMetric);

    /**
     * 批量新增监控指标
     *
     * @param jvmMetricList 监控指标列表
     * @return 结果
     */
    public int batchInsertJvmMetric(List<JvmMetric> jvmMetricList);

    /**
     * 删除过期指标数据
     *
     * @param days 保留天数
     * @return 结果
     */
    public int deleteExpiredMetrics(@Param("days") int days);
}
