package com.example.liyuan.dto;

import lombok.Data;

@Data
public class HotSingerDTO {
    private Long singerId;      // 歌手ID
    private String singerName;  // 歌手昵称
    private String singerAvatar; // 歌手头像
    private Integer orderCount; // 被点歌次数
}