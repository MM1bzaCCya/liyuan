package com.example.liyuan.service;

import com.example.liyuan.dto.LoginResponse;

import java.util.Map;

public interface AuthService {
    LoginResponse loginWithWechat(String code, Map<String, Object> userInfo);
}