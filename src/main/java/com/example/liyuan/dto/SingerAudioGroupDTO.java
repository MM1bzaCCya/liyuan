package com.example.liyuan.dto;

import lombok.Data;

import java.util.List;

@Data
public class SingerAudioGroupDTO {
    // 歌手信息
    private SingerInfoDTO singer;

    // 音频列表
    private List<AudioInfoDTO> audios;

    // 音频数量
    private Integer audioCount;
}

@Data
class SingerInfoDTO {
    private Long id;
    private String name;
    private String avatar;
    private Integer userType;
    private Integer points;
}

@Data
class AudioInfoDTO {
    private Long id;
    private String name;
    private String path;
    private Integer duration;
    private String createdAt;
    private Long singerId;
    private String singerName;
    private String singerAvatar;
    private Boolean isPlaying;
}