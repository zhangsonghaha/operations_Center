package com.ruoyi.system.mapper;

import java.util.List;
import com.ruoyi.system.domain.SysMessage;

/**
 * 站内信Mapper接口
 * 
 * @author ruoyi
 * @date 2024-05-20
 */
public interface SysMessageMapper 
{
    /**
     * 查询站内信
     * 
     * @param messageId 站内信ID
     * @return 站内信
     */
    public SysMessage selectSysMessageById(Long messageId);

    /**
     * 查询站内信列表
     * 
     * @param sysMessage 站内信
     * @return 站内信集合
     */
    public List<SysMessage> selectSysMessageList(SysMessage sysMessage);

    /**
     * 新增站内信
     * 
     * @param sysMessage 站内信
     * @return 结果
     */
    public int insertSysMessage(SysMessage sysMessage);

    /**
     * 修改站内信
     * 
     * @param sysMessage 站内信
     * @return 结果
     */
    public int updateSysMessage(SysMessage sysMessage);

    /**
     * 删除站内信
     * 
     * @param messageId 站内信ID
     * @return 结果
     */
    public int deleteSysMessageById(Long messageId);

    /**
     * 批量删除站内信
     * 
     * @param messageIds 需要删除的数据ID
     * @return 结果
     */
    public int deleteSysMessageByIds(Long[] messageIds);

    /**
     * 获取未读消息数量
     *
     * @param userId 用户ID
     * @return 结果
     */
    public int selectUnreadCount(Long userId);
}
