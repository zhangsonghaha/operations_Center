package com.ruoyi.system.domain;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 数据库连接配置对象 sys_db_conn
 * 
 * @author ruoyi
 */
public class SysDbConn extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 连接ID */
    private Long connId;

    /** 连接名称 */
    @Excel(name = "连接名称")
    private String connName;

    /** 数据库类型 */
    @Excel(name = "数据库类型")
    private String dbType;

    /** 主机地址 */
    @Excel(name = "主机地址")
    private String host;

    /** 端口 */
    @Excel(name = "端口")
    private String port;

    /** 账号 */
    @Excel(name = "账号")
    private String username;

    /** 密码 */
    private String password;

    /** 数据库名 */
    @Excel(name = "数据库名")
    private String dbName;

    /** 状态（0正常 1停用） */
    @Excel(name = "状态", readConverterExp = "0=正常,1=停用")
    private String status;

    public void setConnId(Long connId) 
    {
        this.connId = connId;
    }

    public Long getConnId() 
    {
        return connId;
    }
    public void setConnName(String connName) 
    {
        this.connName = connName;
    }

    @NotBlank(message = "连接名称不能为空")
    @Size(min = 0, max = 100, message = "连接名称不能超过100个字符")
    public String getConnName() 
    {
        return connName;
    }
    public void setDbType(String dbType) 
    {
        this.dbType = dbType;
    }

    public String getDbType() 
    {
        return dbType;
    }
    public void setHost(String host) 
    {
        this.host = host;
    }

    @NotBlank(message = "主机地址不能为空")
    public String getHost() 
    {
        return host;
    }
    public void setPort(String port) 
    {
        this.port = port;
    }

    @NotBlank(message = "端口不能为空")
    public String getPort() 
    {
        return port;
    }
    public void setUsername(String username) 
    {
        this.username = username;
    }

    @NotBlank(message = "账号不能为空")
    public String getUsername() 
    {
        return username;
    }
    public void setPassword(String password) 
    {
        this.password = password;
    }

    @NotBlank(message = "密码不能为空")
    public String getPassword() 
    {
        return password;
    }
    public void setDbName(String dbName) 
    {
        this.dbName = dbName;
    }

    @NotBlank(message = "数据库名不能为空")
    public String getDbName() 
    {
        return dbName;
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
            .append("connId", getConnId())
            .append("connName", getConnName())
            .append("dbType", getDbType())
            .append("host", getHost())
            .append("port", getPort())
            .append("username", getUsername())
            .append("password", getPassword())
            .append("dbName", getDbName())
            .append("status", getStatus())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .append("remark", getRemark())
            .toString();
    }
}
