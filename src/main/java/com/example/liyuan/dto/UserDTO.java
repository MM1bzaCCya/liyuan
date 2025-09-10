package com.example.liyuan.dto;

import lombok.Data;

@Data
public class UserDTO {
    private Long id;
    private String phone;
    private String nickname;
    private String avatar;
    private Integer userType;
    private Integer points;
}