package com.ruoyi.web.service;

import java.util.List;
import com.ruoyi.web.domain.OpsServer;

/**
 * 服务器资产Service接口
 */
public interface IOpsServerService 
{
    /**
     * 查询服务器资产
     */
    public OpsServer selectOpsServerByServerId(Long serverId);

    /**
     * 查询服务器资产列表
     */
    public List<OpsServer> selectOpsServerList(OpsServer opsServer);

    /**
     * 新增服务器资产
     */
    public int insertOpsServer(OpsServer opsServer);

    /**
     * 修改服务器资产
     */
    public int updateOpsServer(OpsServer opsServer);

    /**
     * 批量删除服务器资产
     */
    public int deleteOpsServerByServerIds(Long[] serverIds);

    /**
     * 删除服务器资产信息
     */
    public int deleteOpsServerByServerId(Long serverId);

    /**
     * 检测服务器连接状态
     * 
     * @param serverId 服务器ID
     * @return 结果消息
     */
    public String checkConnection(Long serverId);
}
