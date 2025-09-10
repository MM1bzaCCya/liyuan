package com.example.liyuan.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class User {
    private Long id;
    private String wechatOpenid;     // 微信openid
    private String wechatUnionid;    // 微信unionid
    private String nickname;         // 微信昵称
    private String avatar;           // 微信头像
    private Integer gender;          // 性别：0-未知，1-男，2-女
    private String country;          // 国家
    private String province;         // 省份
    private String city;             // 城市
    private Integer userType = 0;    // 用户类型：0-普通用户, 1-歌手, 2-老板
    private Integer points = 0;      // 用户积分
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}