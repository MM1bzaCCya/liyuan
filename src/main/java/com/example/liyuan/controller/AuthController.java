package com.example.liyuan.controller;

import com.example.liyuan.dto.LoginResponse;
import com.example.liyuan.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> wechatLogin(@RequestBody Map<String, Object> request) {
        logger.info("收到微信登录请求: {}", request);

        String code = (String) request.get("code");
        Map<String, Object> userInfo = (Map<String, Object>) request.get("userInfo");

        if (code == null || code.isEmpty()) {
            logger.error("微信登录失败: code参数为空");
            return ResponseEntity.badRequest().body(Map.of(
                    "code", 400,
                    "msg", "code参数不能为空"
            ));
        }

        if (userInfo == null) {
            logger.error("微信登录失败: userInfo参数为空");
            return ResponseEntity.badRequest().body(Map.of(
                    "code", 400,
                    "msg", "userInfo参数不能为空"
            ));
        }

        try {
            LoginResponse response = authService.loginWithWechat(code, userInfo);
            logger.info("微信登录成功: {}", response.getUser().getNickname());

            // 确保返回的数据结构包含 token 和 userInfo
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("token", response.getToken());
            responseData.put("userInfo", response.getUser()); // 确保字段名正确

            return ResponseEntity.ok(Map.of(
                    "code", 200,
                    "msg", "登录成功",
                    "data", responseData
            ));
        } catch (Exception e) {
            logger.error("微信登录失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                    "code", 400,
                    "msg", "登录失败: " + e.getMessage()
            ));
        }
    }
}