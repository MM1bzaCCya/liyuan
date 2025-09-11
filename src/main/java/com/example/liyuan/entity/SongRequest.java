package com.example.liyuan.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SongRequest {
    private Long id;
    private Long userId;
    private String songName;
    private String originalSinger;
    private Integer status; // 0-待抢, 1-已抢, 2-已完成, 3-已取消
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // 关联字段（非数据库字段）
    private String requesterName;
    private String requesterAvatar;
}