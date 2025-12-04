package com.example.liyuan.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PersonalPlaylist {
    private Long id;
    private Long userId;
    private String songName;
    private String artist;
    private LocalDateTime addTime;
    private Boolean isFavorite;
}
