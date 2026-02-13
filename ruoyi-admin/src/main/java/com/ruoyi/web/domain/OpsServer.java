package com.ruoyi.web.domain;

import com.ruoyi.common.core.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 服务器资产对象 t_ops_server
 */
public class OpsServer extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 服务器ID */
    private Long serverId;

    /** 服务器名称 */
    private String serverName;

    /** 公网IP */
    private String publicIp;

    /** 内网IP */
    private String privateIp;

    /** SSH端口 */
    private Integer serverPort;

    /** 账号 */
    private String username;

    /** 密码 */
    private String password;

    /** 认证方式（0密码 1密钥） */
    private String authType;

    /** SSH密钥 */
    private String privateKey;

    /** 所属机房 */
    private String dataCenter;

    /** 所属分组 */
    private Long groupId;

    /** 状态（0正常 1停用） */
    private String status;

    public void setServerId(Long serverId) 
    {
        this.serverId = serverId;
    }

    public Long getServerId() 
    {
        return serverId;
    }
    public void setServerName(String serverName) 
    {
        this.serverName = serverName;
    }

    public String getServerName() 
    {
        return serverName;
    }
    public void setPublicIp(String publicIp) 
    {
        this.publicIp = publicIp;
    }

    public String getPublicIp() 
    {
        return publicIp;
    }
    public void setPrivateIp(String privateIp) 
    {
        this.privateIp = privateIp;
    }

    public String getPrivateIp() 
    {
        return privateIp;
    }
    public void setServerPort(Integer serverPort) 
    {
        this.serverPort = serverPort;
    }

    public Integer getServerPort() 
    {
        return serverPort;
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
    public void setAuthType(String authType) 
    {
        this.authType = authType;
    }

    public String getAuthType() 
    {
        return authType;
    }
    public void setPrivateKey(String privateKey) 
    {
        this.privateKey = privateKey;
    }

    public String getPrivateKey() 
    {
        return privateKey;
    }
    public void setDataCenter(String dataCenter) 
    {
        this.dataCenter = dataCenter;
    }

    public String getDataCenter() 
    {
        return dataCenter;
    }
    public void setGroupId(Long groupId) 
    {
        this.groupId = groupId;
    }

    public Long getGroupId() 
    {
        return groupId;
    }
    public void setStatus(String status) 
    {
        this.status = status;
    }

    public String getStatus() 
    {
        return status;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("serverId", getServerId())
            .append("serverName", getServerName())
            .append("publicIp", getPublicIp())
            .append("privateIp", getPrivateIp())
            .append("serverPort", getServerPort())
            .append("username", getUsername())
            .append("password", getPassword())
            .append("authType", getAuthType())
            .append("privateKey", getPrivateKey())
            .append("dataCenter", getDataCenter())
            .append("groupId", getGroupId())
            .append("status", getStatus())
            .append("remark", getRemark())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
