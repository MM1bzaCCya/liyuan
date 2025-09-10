package com.example.liyuan.dto;

import com.example.liyuan.entity.User;
import lombok.Data;

@Data
public class LoginResponse {
    private String token;
    private User user;
}