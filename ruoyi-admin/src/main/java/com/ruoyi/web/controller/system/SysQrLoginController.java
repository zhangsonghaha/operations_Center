package com.ruoyi.web.controller.system;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.framework.web.service.SysQrLoginService;
import com.ruoyi.framework.web.service.SysQrLoginService.QrStatusResult;

/**
 * 扫码登录控制器
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/login/qr")
public class SysQrLoginController
{
    @Autowired
    private SysQrLoginService qrLoginService;

    /**
     * 获取登录二维码UUID
     */
    @GetMapping("/code")
    public AjaxResult getQrCode()
    {
        String uuid = qrLoginService.createQrCode();
        return AjaxResult.success().put("uuid", uuid);
    }

    /**
     * 检查二维码状态
     */
    @GetMapping("/check")
    public AjaxResult checkQrCodeStatus(@RequestParam("uuid") String uuid)
    {
        QrStatusResult result = qrLoginService.checkQrCodeStatus(uuid);
        return AjaxResult.success()
                .put("status", result.getStatus())
                .put("token", result.getToken());
    }
    
    // --- 以下接口仅用于模拟手机端操作 (实际应由手机App调用，且需要鉴权) ---

    /**
     * 模拟扫描二维码
     */
    @PostMapping("/scan")
    public AjaxResult scanQrCode(@RequestBody QrLoginBody body)
    {
        qrLoginService.scanQrCode(body.getUuid());
        return AjaxResult.success();
    }

    /**
     * 模拟确认登录 (实际场景中，username即为手机端的openId)
     */
    @PostMapping("/confirm")
    public AjaxResult confirmQrCode(@RequestBody QrLoginBody body)
    {
        // 这里暂时复用 username 字段作为 openId / thirdPartyId
        // 模拟端应该传递一个唯一的 openId
        // 如果未传 username，随机生成一个 (仅供测试未绑定场景)
        String openId = body.getUsername();
        if (openId == null || openId.isEmpty()) {
            openId = "TEST_OPENID_" + System.currentTimeMillis();
        }
        
        qrLoginService.confirmQrCode(body.getUuid(), openId);
        return AjaxResult.success().put("openId", openId); // 返回 openId 方便前端测试
    }
    
    /**
     * 绑定现有账号
     */
    @PostMapping("/bind")
    public AjaxResult bindUser(@RequestBody QrBindBody body)
    {
        qrLoginService.bindUser(body.getUuid(), body.getUsername(), body.getPassword());
        return AjaxResult.success("绑定成功");
    }
    
    /**
     * 微信扫码登录 (接收 Code)
     */
    @PostMapping("/wechat")
    public AjaxResult wechatLogin(@RequestBody WechatLoginBody body)
    {
        qrLoginService.wechatLogin(body.getUuid(), body.getCode());
        return AjaxResult.success();
    }

    public static class WechatLoginBody {
        private String uuid;
        private String code;
        
        public String getUuid() { return uuid; }
        public void setUuid(String uuid) { this.uuid = uuid; }
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
    }

    public static class QrLoginBody {
        private String uuid;
        private String username; // used as openId for mock

        public String getUuid() {
            return uuid;
        }

        public void setUuid(String uuid) {
            this.uuid = uuid;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }
    }
    
    public static class QrBindBody {
        private String uuid;
        private String username;
        private String password;
        
        public String getUuid() { return uuid; }
        public void setUuid(String uuid) { this.uuid = uuid; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}
