package com.ruoyi.system.domain.monitor;

import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * JVM 告警规则对象 sys_jvm_alert_rule
 * 
 * @author ruoyi
 */
public class JvmAlertRule extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 规则ID */
    private Long ruleId;

    /** 目标ID */
    private Long targetId;

    /** 指标名称 */
    private String metricName;

    /** 条件 */
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
    public void setTargetId(Long targetId) 
    {
        this.targetId = targetId;
    }

    public Long getTargetId() 
    {
        return targetId;
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
            .append("targetId", getTargetId())
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
