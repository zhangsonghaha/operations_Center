package com.ruoyi.common.utils;

import com.alibaba.fastjson2.JSONObject;
import com.ruoyi.common.utils.http.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * WeChat Utility Class
 */
@Component
public class WeChatUtils {

    private static final Logger log = LoggerFactory.getLogger(WeChatUtils.class);

    @Value("${wechat.appId}")
    private String appId;

    @Value("${wechat.secret}")
    private String secret;

    /**
     * Get OpenID using login code
     * 
     * @param code The code from wx.login()
     * @return openid
     */
    public String getOpenId(String code) {
        // Mock implementation for demo environment
        if (code.startsWith("mock_code_")) {
            return "mock_openid_" + code.substring(10);
        }

        // WeChat API URL
        String urlStr = "https://api.weixin.qq.com/sns/jscode2session?appid=" + appId + 
                        "&secret=" + secret + 
                        "&js_code=" + code + 
                        "&grant_type=authorization_code";
        
        try {
            // Use RuoYi's existing HttpUtils
            String result = HttpUtils.sendGet(urlStr);
            JSONObject jsonObject = JSONObject.parseObject(result);
            
            if (jsonObject.containsKey("errcode") && jsonObject.getIntValue("errcode") != 0) {
                log.error("Failed to get OpenID: {}", result);
                return null;
            }
            
            return jsonObject.getString("openid");
        } catch (Exception e) {
            log.error("WeChat API call exception", e);
            return null;
        }
    }
}
