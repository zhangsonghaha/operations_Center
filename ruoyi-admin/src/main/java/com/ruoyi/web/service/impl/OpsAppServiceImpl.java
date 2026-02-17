package com.ruoyi.web.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.constant.UserConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.web.mapper.OpsAppMapper;
import com.ruoyi.web.domain.OpsApp;
import com.ruoyi.web.service.IOpsAppService;

/**
 * 应用注册Service业务层处理
 * 
 * @author ruoyi
 */
@Service
public class OpsAppServiceImpl implements IOpsAppService 
{
    @Autowired
    private OpsAppMapper opsAppMapper;

    /**
     * 查询应用注册
     * 
     * @param appId 应用注册ID
     * @return 应用注册
     */
    @Override
    public OpsApp selectOpsAppById(Long appId)
    {
        return opsAppMapper.selectOpsAppById(appId);
    }

    /**
     * 查询应用注册列表
     * 
     * @param opsApp 应用注册
     * @return 应用注册
     */
    @Override
    public List<OpsApp> selectOpsAppList(OpsApp opsApp)
    {
        return opsAppMapper.selectOpsAppList(opsApp);
    }

    /**
     * 新增应用注册
     * 
     * @param opsApp 应用注册
     * @return 结果
     */
    @Override
    public int insertOpsApp(OpsApp opsApp)
    {
        opsApp.setCreateTime(DateUtils.getNowDate());
        return opsAppMapper.insertOpsApp(opsApp);
    }

    /**
     * 修改应用注册
     * 
     * @param opsApp 应用注册
     * @return 结果
     */
    @Override
    public int updateOpsApp(OpsApp opsApp)
    {
        opsApp.setUpdateTime(DateUtils.getNowDate());
        return opsAppMapper.updateOpsApp(opsApp);
    }

    /**
     * 批量删除应用注册
     * 
     * @param appIds 需要删除的应用注册ID
     * @return 结果
     */
    @Override
    public int deleteOpsAppByIds(Long[] appIds)
    {
        return opsAppMapper.deleteOpsAppByIds(appIds);
    }

    /**
     * 删除应用注册信息
     * 
     * @param appId 应用注册ID
     * @return 结果
     */
    @Override
    public int deleteOpsAppById(Long appId)
    {
        return opsAppMapper.deleteOpsAppById(appId);
    }

    /**
     * 校验应用名称是否唯一
     * 
     * @param opsApp 应用信息
     * @return 结果
     */
    @Override
    public boolean checkAppNameUnique(OpsApp opsApp)
    {
        Long appId = StringUtils.isNull(opsApp.getAppId()) ? -1L : opsApp.getAppId();
        OpsApp info = opsAppMapper.checkAppNameUnique(opsApp.getAppName());
        if (StringUtils.isNotNull(info) && info.getAppId().longValue() != appId.longValue())
        {
            return UserConstants.NOT_UNIQUE;
        }
        return UserConstants.UNIQUE;
    }
}
