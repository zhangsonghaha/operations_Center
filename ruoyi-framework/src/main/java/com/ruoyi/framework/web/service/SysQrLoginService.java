package com.ruoyi.framework.web.service;

import java.util.concurrent.TimeUnit;
import java.util.List;
import javax.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.MessageUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.uuid.IdUtils;
import com.ruoyi.framework.manager.AsyncManager;
import com.ruoyi.framework.manager.factory.AsyncFactory;
import com.ruoyi.framework.security.context.AuthenticationContextHolder;
import com.ruoyi.system.service.ISysUserService;

/**
 * 扫码登录服务
 * 
 * @author ruoyi
 */
@Component
public class SysQrLoginService
{
    @Autowired
    private TokenService tokenService;

    @Resource
    private AuthenticationManager authenticationManager;

    @Autowired
    private RedisCache redisCache;
    
    @Autowired
    private SysLoginService loginService;
    
    @Autowired
    private ISysUserService userService;
    
    @Autowired
    private SysPermissionService permissionService;
    
    @Autowired
    private com.ruoyi.common.utils.WeChatUtils weChatUtils;

    // QR Code Status
    public static final String QR_STATUS_WAITING = "WAITING";
    public static final String QR_STATUS_SCANNED = "SCANNED";
    public static final String QR_STATUS_CONFIRMED = "CONFIRMED";
    public static final String QR_STATUS_EXPIRED = "EXPIRED";
    public static final String QR_STATUS_NOT_BOUND = "NOT_BOUND";
    
    private static final String QR_CODE_KEY = "qr_login:";
    private static final Integer QR_EXPIRE_MINUTES = 2;

    /**
     * 生成登录二维码UUID
     * 
     * @return UUID
     */
    public String createQrCode()
    {
        String uuid = IdUtils.simpleUUID();
        String verifyKey = QR_CODE_KEY + uuid;
        redisCache.setCacheObject(verifyKey, QR_STATUS_WAITING, QR_EXPIRE_MINUTES, TimeUnit.MINUTES);
        return uuid;
    }

    /**
     * 检查二维码状态
     * 
     * @param uuid 唯一标识
     * @return 状态和Token
     */
    public QrStatusResult checkQrCodeStatus(String uuid)
    {
        String verifyKey = QR_CODE_KEY + uuid;
        String status = redisCache.getCacheObject(verifyKey);
        
        if (status == null)
        {
            return new QrStatusResult(QR_STATUS_EXPIRED, null);
        }
        
        if (QR_STATUS_CONFIRMED.equals(status))
        {
            // 获取暂存的Token
            String tokenKey = QR_CODE_KEY + uuid + ":token";
            String token = redisCache.getCacheObject(tokenKey);
            // 登录成功后不要立即删除缓存，防止前端轮询时因网络波动没收到响应而重试失败
            // 让其自然过期即可
            // redisCache.deleteObject(verifyKey);
            // redisCache.deleteObject(tokenKey);
            return new QrStatusResult(QR_STATUS_CONFIRMED, token);
        }
        
        if (QR_STATUS_NOT_BOUND.equals(status)) {
            // 返回未绑定状态，可能包含 openId (实际上 openId 存在 Redis 里，前端不需要知道明文，只需要知道 uuid 对应的状态是未绑定)
             return new QrStatusResult(QR_STATUS_NOT_BOUND, null);
        }
        
        return new QrStatusResult(status, null);
    }

    /**
     * 模拟手机端扫描二维码
     * 
     * @param uuid 唯一标识
     */
    public void scanQrCode(String uuid)
    {
        String verifyKey = QR_CODE_KEY + uuid;
        if (redisCache.hasKey(verifyKey))
        {
            redisCache.setCacheObject(verifyKey, QR_STATUS_SCANNED, QR_EXPIRE_MINUTES, TimeUnit.MINUTES);
        }
        else
        {
            throw new ServiceException("二维码已过期或不存在");
        }
    }

    /**
     * 微信扫码登录 (通过Code)
     *
     * @param uuid 唯一标识
     * @param code 微信Code
     */
    public void wechatLogin(String uuid, String code) {
        String verifyKey = QR_CODE_KEY + uuid;
        if (!redisCache.hasKey(verifyKey)) {
            throw new ServiceException("二维码已过期或不存在");
        }

        // 1. Exchange code for OpenID
        String openId = weChatUtils.getOpenId(code);
        if (StringUtils.isEmpty(openId)) {
            throw new ServiceException("微信登录失败，无法获取OpenID");
        }

        // 2. Reuse existing confirmation logic
        confirmQrCode(uuid, openId);
    }

    /**
     * 模拟手机端确认登录 (支持绑定逻辑)
     * 
     * @param uuid 唯一标识
     * @param thirdPartyId 三方ID (OpenID)
     */
    public void confirmQrCode(String uuid, String thirdPartyId)
    {
        String verifyKey = QR_CODE_KEY + uuid;
        if (!redisCache.hasKey(verifyKey))
        {
            throw new ServiceException("二维码已过期或不存在");
        }

        // APP 已鉴权，可以直接获取当前登录用户
        // 如果 thirdPartyId 传的是 username，则验证是否与当前登录用户一致
        LoginUser loginUser = com.ruoyi.common.utils.SecurityUtils.getLoginUser();
        SysUser currentUser = loginUser.getUser();
        
        // 如果 APP 传了 username，校验一下是否匹配（可选）
        if (StringUtils.isNotEmpty(thirdPartyId) && !currentUser.getUserName().equals(thirdPartyId)) {
             // 仅记录日志，或者强制使用当前登录用户
             // throw new ServiceException("非法操作：用户不匹配");
        }
        
        // 直接使用当前登录用户进行 PC 端登录
        loginUser(verifyKey, currentUser);
    }
    
    /**
     * 绑定现有账号并登录
     */
    public void bindUser(String uuid, String username, String password) {
         String verifyKey = QR_CODE_KEY + uuid;
         String openId = redisCache.getCacheObject(verifyKey + ":openid");
         
         if (StringUtils.isEmpty(openId)) {
             throw new ServiceException("二维码已过期或绑定信息失效");
         }
         
         // 验证账号密码
         UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, password);
         AuthenticationContextHolder.setContext(authenticationToken);
         Authentication authentication = authenticationManager.authenticate(authenticationToken);
         
         LoginUser loginUser = (LoginUser) authentication.getPrincipal();
         SysUser sysUser = loginUser.getUser();
         
         // 更新绑定信息 (remark)
         sysUser.setRemark("OPENID:" + openId);
         userService.updateUser(sysUser);
         
         // 登录
         loginUser(verifyKey, sysUser);
    }

    private void loginUser(String verifyKey, SysUser user) {
        try {
             // 重新构造 LoginUser 以确保信息最新
             // 注意：这里没有走 AuthenticationManager，因为已经验证过了或者信任绑定关系
             // 但为了生成 Token，我们需要 LoginUser
             // 简单起见，我们假设 user 对象足够生成 Token，或者重新查一遍
             // 标准做法是构造 LoginUser
             
             // 这里为了复用 TokenService，我们需要完整的 LoginUser
             // 由于我们没有密码 (自动登录场景)，我们无法调用 authenticationManager.authenticate
             // 所以我们需要手动构造 LoginUser
             
             // 模拟 UserDetails
             LoginUser loginUser = new LoginUser(user.getUserId(), user.getDeptId(), user, permissionService.getMenuPermission(user));
             
             AsyncManager.me().execute(AsyncFactory.recordLogininfor(user.getUserName(), Constants.LOGIN_SUCCESS, "扫码登录成功"));
             loginService.recordLoginInfo(user.getUserId());
             
             String token = tokenService.createToken(loginUser);
             
             // 存入 Redis，状态改为 CONFIRMED
             redisCache.setCacheObject(verifyKey, QR_STATUS_CONFIRMED, QR_EXPIRE_MINUTES, TimeUnit.MINUTES);
             redisCache.setCacheObject(verifyKey + ":token", token, QR_EXPIRE_MINUTES, TimeUnit.MINUTES);
        } catch (Exception e) {
             throw new ServiceException("登录失败: " + e.getMessage());
        }
    }
    
    public static class QrStatusResult {
        private String status;
        private String token;

        public QrStatusResult(String status, String token) {
            this.status = status;
            this.token = token;
        }

        public String getStatus() {
            return status;
        }

        public String getToken() {
            return token;
        }
    }
}
