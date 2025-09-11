package com.example.liyuan.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SongGrab {
    private Long id;
    private Long requestId;
    private Long singerId;
    private Integer status; // 0-待确认, 1-已确认, 2-已拒绝
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // 关联字段（非数据库字段）
    private String singerName;
    private String singerAvatar;
    private String songName;
    private String originalSinger;
}