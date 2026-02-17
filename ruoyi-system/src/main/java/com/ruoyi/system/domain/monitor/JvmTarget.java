package com.ruoyi.system.domain.monitor;

import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * JMX 监控目标对象 sys_jvm_target
 * 
 * @author ruoyi
 */
public class JvmTarget extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 目标ID */
    private Long targetId;

    /** 应用名称 */
    private String name;

    /** 主机地址 */
    private String host;

    /** JMX端口 */
    private Integer port;

    /** JMX用户名 */
    private String username;

    /** JMX密码 */
    private String password;

    /** 状态（0正常 1停用） */
    private String enabled;

    public void setTargetId(Long targetId) 
    {
        this.targetId = targetId;
    }

    public Long getTargetId() 
    {
        return targetId;
    }
    public void setName(String name) 
    {
        this.name = name;
    }

    public String getName() 
    {
        return name;
    }
    public void setHost(String host) 
    {
        this.host = host;
    }

    public String getHost() 
    {
        return host;
    }
    public void setPort(Integer port) 
    {
        this.port = port;
    }

    public Integer getPort() 
    {
        return port;
    }
    public void setUsername(String username) 
    {
        this.username = username;
    }

    public String getUsername() 
    {
        return username;
    }
    public void setPassword(String password) 
    {
        this.password = password;
    }

    public String getPassword() 
    {
        return password;
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
            .append("targetId", getTargetId())
            .append("name", getName())
            .append("host", getHost())
            .append("port", getPort())
            .append("username", getUsername())
            .append("password", getPassword())
            .append("enabled", getEnabled())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
