package com.ruoyi.web.service;

import java.util.Map;

/**
 * 运维仪表盘Service接口
 */
public interface IOpsDashboardService 
{
    /**
     * 获取仪表盘聚合数据
     * 
     * @return 结果
     */
    public Map<String, Object> getDashboardData();
}
