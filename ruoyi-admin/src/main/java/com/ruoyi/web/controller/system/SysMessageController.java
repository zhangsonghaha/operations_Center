package com.ruoyi.web.controller.system;

import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.system.domain.SysMessage;
import com.ruoyi.system.service.ISysMessageService;

/**
 * 站内信Controller
 * 
 * @author ruoyi
 * @date 2024-05-20
 */
@RestController
@RequestMapping("/system/message")
public class SysMessageController extends BaseController
{
    @Autowired
    private ISysMessageService sysMessageService;

    /**
     * 查询站内信列表
     */
    @PreAuthorize("@ss.hasPermi('system:message:list')")
    @GetMapping("/list")
    public TableDataInfo list(SysMessage sysMessage)
    {
        startPage();
        // 如果是普通用户，只能查询自己的消息
        if (!getLoginUser().getUser().isAdmin()) {
            // 这里可以根据业务逻辑调整，比如收件箱只查 receiver_id = userId
            // 目前简化为由前端传参控制，或者在此处强制设置
            if (sysMessage.getReceiverId() == null && sysMessage.getSenderId() == null) {
                 sysMessage.setReceiverId(getUserId());
            }
        }
        List<SysMessage> list = sysMessageService.selectSysMessageList(sysMessage);
        return getDataTable(list);
    }

    /**
     * 获取未读数量
     */
    @GetMapping("/unreadCount")
    public AjaxResult unreadCount()
    {
        return AjaxResult.success(sysMessageService.selectUnreadCount(getUserId()));
    }

    /**
     * 导出站内信列表
     */
    @PreAuthorize("@ss.hasPermi('system:message:export')")
    @Log(title = "站内信", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, SysMessage sysMessage)
    {
        List<SysMessage> list = sysMessageService.selectSysMessageList(sysMessage);
        ExcelUtil<SysMessage> util = new ExcelUtil<SysMessage>(SysMessage.class);
        util.exportExcel(response, list, "站内信数据");
    }

    /**
     * 获取站内信详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:message:query')")
    @GetMapping(value = "/{messageId}")
    public AjaxResult getInfo(@PathVariable("messageId") Long messageId)
    {
        SysMessage msg = sysMessageService.selectSysMessageById(messageId);
        // 如果是接收者阅读，且当前未读，则标记为已读
        if (msg.getReceiverId().equals(getUserId()) && "0".equals(msg.getReadStatus())) {
            msg.setReadStatus("1");
            msg.setReadTime(new Date());
            sysMessageService.updateSysMessage(msg);
        }
        return AjaxResult.success(msg);
    }

    /**
     * 新增站内信
     */
    @PreAuthorize("@ss.hasPermi('system:message:add')")
    @Log(title = "站内信", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody SysMessage sysMessage)
    {
        sysMessage.setSenderId(getUserId());
        sysMessage.setCreateBy(getUsername());
        
        // 批量发送
        if (sysMessage.getReceiverIds() != null && !sysMessage.getReceiverIds().isEmpty()) {
            int successCount = 0;
            for (Long receiverId : sysMessage.getReceiverIds()) {
                SysMessage msg = new SysMessage();
                // 复制属性
                msg.setSenderId(sysMessage.getSenderId());
                msg.setReceiverId(receiverId);
                msg.setTitle(sysMessage.getTitle());
                msg.setContent(sysMessage.getContent());
                msg.setMessageType(sysMessage.getMessageType());
                msg.setBusinessId(sysMessage.getBusinessId());
                msg.setCreateBy(sysMessage.getCreateBy());
                msg.setAttachment(sysMessage.getAttachment());
                sysMessageService.insertSysMessage(msg);
                successCount++;
            }
            return successCount > 0 ? AjaxResult.success() : AjaxResult.error();
        }
        
        return toAjax(sysMessageService.insertSysMessage(sysMessage));
    }

    /**
     * 修改站内信
     */
    @PreAuthorize("@ss.hasPermi('system:message:edit')")
    @Log(title = "站内信", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody SysMessage sysMessage)
    {
        sysMessage.setUpdateBy(getUsername());
        return toAjax(sysMessageService.updateSysMessage(sysMessage));
    }

    /**
     * 删除站内信
     */
    @PreAuthorize("@ss.hasPermi('system:message:remove')")
    @Log(title = "站内信", businessType = BusinessType.DELETE)
	@DeleteMapping("/{messageIds}")
    public AjaxResult remove(@PathVariable Long[] messageIds)
    {
        return toAjax(sysMessageService.deleteSysMessageByIds(messageIds));
    }
}
