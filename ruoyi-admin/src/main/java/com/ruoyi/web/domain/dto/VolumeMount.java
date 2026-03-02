package com.ruoyi.web.domain.dto;

import java.io.Serializable;

/**
 * Docker卷挂载配置
 * 
 * @author ruoyi
 */
public class VolumeMount implements Serializable
{
    private static final long serialVersionUID = 1L;

    /** 主机路径 */
    private String hostPath;

    /** 容器路径 */
    private String containerPath;

    /** 挂载模式(rw-读写, ro-只读) */
    private String mode;

    public VolumeMount() {
    }

    public VolumeMount(String hostPath, String containerPath, String mode) {
        this.hostPath = hostPath;
        this.containerPath = containerPath;
        this.mode = mode;
    }

    public String getHostPath() {
        return hostPath;
    }

    public void setHostPath(String hostPath) {
        this.hostPath = hostPath;
    }

    public String getContainerPath() {
        return containerPath;
    }

    public void setContainerPath(String containerPath) {
        this.containerPath = containerPath;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    @Override
    public String toString() {
        return "VolumeMount{" +
                "hostPath='" + hostPath + '\'' +
                ", containerPath='" + containerPath + '\'' +
                ", mode='" + mode + '\'' +
                '}';
    }
}
