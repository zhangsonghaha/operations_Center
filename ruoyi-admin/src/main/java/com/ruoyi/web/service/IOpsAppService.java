package com.ruoyi.web.service;

import java.util.List;
import com.ruoyi.web.domain.OpsApp;

/**
 * 应用注册Service接口
 * 
 * @author ruoyi
 */
public interface IOpsAppService 
{
    /**
     * 查询应用注册
     * 
     * @param appId 应用注册ID
     * @return 应用注册
     */
    public OpsApp selectOpsAppById(Long appId);

    /**
     * 查询应用注册列表
     * 
     * @param opsApp 应用注册
     * @return 应用注册集合
     */
    public List<OpsApp> selectOpsAppList(OpsApp opsApp);

    /**
     * 新增应用注册
     * 
     * @param opsApp 应用注册
     * @return 结果
     */
    public int insertOpsApp(OpsApp opsApp);

    /**
     * 修改应用注册
     * 
     * @param opsApp 应用注册
     * @return 结果
     */
    public int updateOpsApp(OpsApp opsApp);

    /**
     * 批量删除应用注册
     * 
     * @param appIds 需要删除的应用注册ID
     * @return 结果
     */
    public int deleteOpsAppByIds(Long[] appIds);

    /**
     * 删除应用注册信息
     * 
     * @param appId 应用注册ID
     * @return 结果
     */
    public int deleteOpsAppById(Long appId);

    /**
     * 校验应用名称是否唯一
     * 
     * @param opsApp 应用信息
     * @return 结果
     */
    public boolean checkAppNameUnique(OpsApp opsApp);
}
