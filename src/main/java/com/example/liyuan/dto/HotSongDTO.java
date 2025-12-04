package com.example.liyuan.dto;

import lombok.Data;

@Data
public class HotSongDTO {
    private String songName;      // 歌曲名称
    private String artist;        // 原唱歌手
    private Integer orderCount;   // 点歌次数（这是统计出来的）
}