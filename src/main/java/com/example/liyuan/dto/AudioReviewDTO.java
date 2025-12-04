package com.example.liyuan.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class AudioReviewDTO {
    // 音频信息
    private Long audioId;
    private String audioName;
    private String audioLocalPath;
    private Integer audioDuration;
    private LocalDateTime createdAt;

    // 歌手信息
    private Long singerId;
    private String singerNickname;
    private String singerCustomNickname;
    private String singerAvatar;
    private Integer singerUserType;
    private Integer singerPoints;

    // 前端显示用
    private String displaySingerName;

    public String getDisplaySingerName() {
        if (singerCustomNickname != null && !singerCustomNickname.isEmpty()) {
            return singerCustomNickname;
        }
        return singerNickname;
    }
}