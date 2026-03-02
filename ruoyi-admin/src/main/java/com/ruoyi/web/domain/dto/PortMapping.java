package com.ruoyi.web.domain.dto;

import java.io.Serializable;

/**
 * Docker端口映射配置
 * 
 * @author ruoyi
 */
public class PortMapping implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 主机端口 */
    private Integer hostPort;

    /** 容器端口 */
    private Integer containerPort;

    /** 协议(tcp/udp) */
    private String protocol;

    public PortMapping() {
    }

    public PortMapping(Integer hostPort, Integer containerPort, String protocol) {
        this.hostPort = hostPort;
        this.containerPort = containerPort;
        this.protocol = protocol;
    }

    public Integer getHostPort() {
        return hostPort;
    }

    public void setHostPort(Integer hostPort) {
        this.hostPort = hostPort;
    }

    public Integer getContainerPort() {
        return containerPort;
    }

    public void setContainerPort(Integer containerPort) {
        this.containerPort = containerPort;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    @Override
    public String toString() {
        return "PortMapping{" +
                "hostPort=" + hostPort +
                ", containerPort=" + containerPort +
                ", protocol='" + protocol + '\'' +
                '}';
    }
}
