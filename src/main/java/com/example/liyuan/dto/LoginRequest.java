package com.example.liyuan.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String phone;
    private String code;
}