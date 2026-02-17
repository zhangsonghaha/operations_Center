package com.ruoyi.web.domain.vo;

/**
 * 远程文件信息VO
 */
public class RemoteFileVo
{
    private String name;
    private long size;
    private String permission;
    private String modTime;
    private boolean isDir;
    
    // Getters and Setters
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public long getSize() {
        return size;
    }
    public void setSize(long size) {
        this.size = size;
    }
    public String getPermission() {
        return permission;
    }
    public void setPermission(String permission) {
        this.permission = permission;
    }
    public String getModTime() {
        return modTime;
    }
    public void setModTime(String modTime) {
        this.modTime = modTime;
    }
    public boolean isDir() {
        return isDir;
    }
    public void setDir(boolean isDir) {
        this.isDir = isDir;
    }
}
