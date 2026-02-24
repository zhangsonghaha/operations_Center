package com.ruoyi.system.domain.monitor;

import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 数据库监控规则对象 sys_db_monitor_rule
 * 
 * @author ruoyi
 */
public class DbMonitorRule extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 规则ID */
    private Long ruleId;

    /** 连接ID(0代表全局) */
    private Long connId;

    /** 规则类型(slow_query/connection/table_space) */
    private String ruleType;

    /** 指标名称 */
    private String metricName;

    /** 条件(GT, LT, EQ) */
    private String condition;

    /** 阈值 */
    private Double threshold;

    /** 状态（0正常 1停用） */
    private String enabled;

    public void setRuleId(Long ruleId) 
    {
        this.ruleId = ruleId;
    }

    public Long getRuleId() 
    {
        return ruleId;
    }

    public void setConnId(Long connId) 
    {
        this.connId = connId;
    }

    public Long getConnId() 
    {
        return connId;
    }

    public void setRuleType(String ruleType) 
    {
        this.ruleType = ruleType;
    }

    public String getRuleType() 
    {
        return ruleType;
    }

    public void setMetricName(String metricName) 
    {
        this.metricName = metricName;
    }

    public String getMetricName() 
    {
        return metricName;
    }

    public void setCondition(String condition) 
    {
        this.condition = condition;
    }

    public String getCondition() 
    {
        return condition;
    }

    public void setThreshold(Double threshold) 
    {
        this.threshold = threshold;
    }

    public Double getThreshold() 
    {
        return threshold;
    }

    public void setEnabled(String enabled) 
    {
        this.enabled = enabled;
    }

    public String getEnabled() 
    {
        return enabled;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("ruleId", getRuleId())
            .append("connId", getConnId())
            .append("ruleType", getRuleType())
            .append("metricName", getMetricName())
            .append("condition", getCondition())
            .append("threshold", getThreshold())
            .append("enabled", getEnabled())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
