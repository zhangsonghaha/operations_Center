package com.ruoyi.system.service.impl;

import java.util.List;
import com.ruoyi.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.ruoyi.system.mapper.SysMessageMapper;
import com.ruoyi.system.domain.SysMessage;
import com.ruoyi.system.service.ISysMessageService;

/**
 * 站内信Service业务层处理
 * 
 * @author ruoyi
 * @date 2024-05-20
 */
@Service
public class SysMessageServiceImpl implements ISysMessageService 
{
    @Autowired
    private SysMessageMapper sysMessageMapper;

    /**
     * 查询站内信
     * 
     * @param messageId 站内信ID
     * @return 站内信
     */
    @Override
    public SysMessage selectSysMessageById(Long messageId)
    {
        return sysMessageMapper.selectSysMessageById(messageId);
    }

    /**
     * 查询站内信列表
     * 
     * @param sysMessage 站内信
     * @return 站内信
     */
    @Override
    public List<SysMessage> selectSysMessageList(SysMessage sysMessage)
    {
        return sysMessageMapper.selectSysMessageList(sysMessage);
    }

    /**
     * 新增站内信
     * 
     * @param sysMessage 站内信
     * @return 结果
     */
    @Override
    public int insertSysMessage(SysMessage sysMessage)
    {
        sysMessage.setCreateTime(DateUtils.getNowDate());
        return sysMessageMapper.insertSysMessage(sysMessage);
    }

    /**
     * 修改站内信
     * 
     * @param sysMessage 站内信
     * @return 结果
     */
    @Override
    public int updateSysMessage(SysMessage sysMessage)
    {
        sysMessage.setUpdateTime(DateUtils.getNowDate());
        return sysMessageMapper.updateSysMessage(sysMessage);
    }

    /**
     * 批量删除站内信
     * 
     * @param messageIds 需要删除的站内信ID
     * @return 结果
     */
    @Override
    public int deleteSysMessageByIds(Long[] messageIds)
    {
        return sysMessageMapper.deleteSysMessageByIds(messageIds);
    }

    /**
     * 删除站内信信息
     * 
     * @param messageId 站内信ID
     * @return 结果
     */
    @Override
    public int deleteSysMessageById(Long messageId)
    {
        return sysMessageMapper.deleteSysMessageById(messageId);
    }

    /**
     * 获取未读消息数量
     *
     * @param userId 用户ID
     * @return 结果
     */
    @Override
    public int selectUnreadCount(Long userId)
    {
        return sysMessageMapper.selectUnreadCount(userId);
    }
}
