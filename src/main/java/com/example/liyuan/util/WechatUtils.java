package com.example.liyuan.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
public class WechatUtils {

    // 添加 logger 实例
    private static final Logger logger = LoggerFactory.getLogger(WechatUtils.class);

    @Value("${wechat.appid}")
    private String appid;

    @Value("${wechat.secret}")
    private String secret;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 使用 code 获取微信会话信息 (openid 和 session_key)
     * @param code 微信小程序前端传来的 code
     * @return 包含微信会话信息的 Map
     */
    public Map<String, Object> getWechatSession(String code) {
        String url = "https://api.weixin.qq.com/sns/jscode2session?appid=" + appid +
                "&secret=" + secret + "&js_code=" + code + "&grant_type=authorization_code";

        // 记录请求（隐藏敏感信息）
        logger.info("调用微信API: {}", url.replace(secret, "***"));

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            logger.info("微信API响应: {}", response.getBody());

            Map<String, Object> result = objectMapper.readValue(response.getBody(), Map.class);

            // 检查微信接口返回的错误
            if (result.containsKey("errcode")) {
                Integer errcode = (Integer) result.get("errcode");
                String errmsg = (String) result.get("errmsg");
                logger.error("微信接口错误: {} - {}", errcode, errmsg);
                throw new RuntimeException("微信接口错误: " + errcode + " - " + errmsg);
            }

            return result;
        } catch (Exception e) {
            logger.error("获取微信会话失败: {}", e.getMessage(), e);
            throw new RuntimeException("获取微信会话失败: " + e.getMessage(), e);
        }
    }

    /**
     * 从微信会话中提取 openid
     * @param code 微信小程序前端传来的 code
     * @return openid
     */
    public String getOpenid(String code) {
        Map<String, Object> session = getWechatSession(code);
        return (String) session.get("openid");
    }

    /**
     * 从微信会话中提取 session_key
     * @param code 微信小程序前端传来的 code
     * @return session_key
     */
    public String getSessionKey(String code) {
        Map<String, Object> session = getWechatSession(code);
        return (String) session.get("session_key");
    }

    /**
     * 从微信会话中提取 unionid (如果有的话)
     * @param code 微信小程序前端传来的 code
     * @return unionid 或 null
     */
    public String getUnionid(String code) {
        Map<String, Object> session = getWechatSession(code);
        return (String) session.get("unionid");
    }

    /**
     * 验证微信会话是否有效
     * @param code 微信小程序前端传来的 code
     * @return 是否有效
     */
    public boolean validateWechatSession(String code) {
        try {
            Map<String, Object> session = getWechatSession(code);
            return session.containsKey("openid") && session.containsKey("session_key");
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取微信接口调用凭证 (access_token)
     * 注意: 这个接口有调用频率限制，应该缓存结果
     * @return access_token
     */
    public String getAccessToken() {
        String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=" +
                appid + "&secret=" + secret;

        // 记录请求（隐藏敏感信息）
        logger.info("调用微信API获取access_token: {}", url.replace(secret, "***"));

        try {
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            logger.info("微信API响应: {}", response.getBody());

            Map<String, Object> result = objectMapper.readValue(response.getBody(), Map.class);

            if (result.containsKey("errcode")) {
                Integer errcode = (Integer) result.get("errcode");
                String errmsg = (String) result.get("errmsg");
                logger.error("获取access_token失败: {} - {}", errcode, errmsg);
                throw new RuntimeException("获取access_token失败: " + errcode + " - " + errmsg);
            }

            return (String) result.get("access_token");
        } catch (Exception e) {
            logger.error("获取access_token失败: {}", e.getMessage(), e);
            throw new RuntimeException("获取access_token失败: " + e.getMessage(), e);
        }
    }

    /**
     * 获取小程序码 (限制性接口，需要已发布的小程序)
     * @param accessToken 接口调用凭证
     * @param path 小程序页面路径
     * @param width 二维码宽度
     * @return 小程序码的字节数组
     */
    public byte[] getWxacode(String accessToken, String path, int width) {
        String url = "https://api.weixin.qq.com/wxa/getwxacode?access_token=" + accessToken;

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("path", path);
        requestBody.put("width", width);
        requestBody.put("is_hyaline", true);
        requestBody.put("auto_color", false);

        logger.info("调用微信API获取小程序码: path={}, width={}", path, width);

        try {
            ResponseEntity<byte[]> response = restTemplate.postForEntity(url, requestBody, byte[].class);
            logger.info("获取小程序码成功");
            return response.getBody();
        } catch (Exception e) {
            logger.error("获取小程序码失败: {}", e.getMessage(), e);
            throw new RuntimeException("获取小程序码失败: " + e.getMessage(), e);
        }
    }
}