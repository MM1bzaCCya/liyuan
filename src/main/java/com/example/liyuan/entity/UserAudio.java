package com.example.liyuan.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class UserAudio {
    private Long id;
    private Long userId;
    private String audioName;
    private String audioLocalPath;
    private Integer audioDuration;
    private LocalDateTime createdAt;
}