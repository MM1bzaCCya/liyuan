package com.example.liyuan.service.impl;

import com.example.liyuan.dto.LoginResponse;
import com.example.liyuan.entity.User;
import com.example.liyuan.mapper.UserMapper;
import com.example.liyuan.service.AuthService;
import com.example.liyuan.util.JwtUtils;
import com.example.liyuan.util.WechatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class AuthServiceImpl implements AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);
    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private WechatUtils wechatUtils;

    @Override
    @Transactional
    public LoginResponse loginWithWechat(String code, Map<String, Object> userInfo) {
        // 使用code获取微信openid和session_key
        Map<String, Object> wechatSession = wechatUtils.getWechatSession(code);

        if (wechatSession == null || wechatSession.get("openid") == null) {
            logger.error("微信登录失败，无法获取用户信息");
            throw new RuntimeException("微信登录失败，无法获取用户信息");
        }

        String openid = (String) wechatSession.get("openid");
        String sessionKey = (String) wechatSession.get("session_key");
        String unionid = (String) wechatSession.get("unionid");

        logger.info("获取到微信用户信息: openid={}, unionid={}", openid, unionid);

        // 查找或创建用户
        User user = userMapper.selectByWechatOpenid(openid);
        logger.info("数据库查询结果: {}", user);

        if (user == null) {
            logger.info("创建新用户");
            user = new User();
            user.setWechatOpenid(openid);
            user.setWechatUnionid(unionid);
            user.setNickname((String) userInfo.get("nickName"));
            user.setAvatar((String) userInfo.get("avatarUrl"));
            user.setGender((Integer) userInfo.get("gender"));
            user.setCountry((String) userInfo.get("country"));
            user.setProvince((String) userInfo.get("province"));
            user.setCity((String) userInfo.get("city"));
            user.setUserType(0); // 普通用户
            user.setPoints(0);
            user.setCreateTime(LocalDateTime.now());
            user.setUpdateTime(LocalDateTime.now());

            int result = userMapper.insert(user);
            logger.info("插入用户结果: {}", result);
        } else {
            logger.info("更新现有用户");
            // 更新用户信息
            user.setNickname((String) userInfo.get("nickName"));
            user.setAvatar((String) userInfo.get("avatarUrl"));
            user.setGender((Integer) userInfo.get("gender"));
            user.setCountry((String) userInfo.get("country"));
            user.setProvince((String) userInfo.get("province"));
            user.setCity((String) userInfo.get("city"));
            user.setUpdateTime(LocalDateTime.now());

            int result = userMapper.update(user);
            logger.info("更新用户结果: {}", result);
        }

        // 生成JWT token
        String token = jwtUtils.generateToken(user.getId());
        logger.info("生成JWT token: {}", token);

        // 返回登录响应
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUser(user);

        return response;
    }
}