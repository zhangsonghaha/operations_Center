package com.ruoyi.web.mapper;

import java.util.List;
import com.ruoyi.web.domain.OpsServer;

/**
 * 服务器资产Mapper接口
 */
public interface OpsServerMapper 
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
     * 删除服务器资产
     */
    public int deleteOpsServerByServerId(Long serverId);

    /**
     * 批量删除服务器资产
     */
    public int deleteOpsServerByServerIds(Long[] serverIds);
}
