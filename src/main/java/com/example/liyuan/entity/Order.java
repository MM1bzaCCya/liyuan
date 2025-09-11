package com.example.liyuan.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Order {
    private Long id;
    private Long requestId;
    private Long grabId;
    private Long userId;
    private Long singerId;
    private String songName;
    private String originalSinger;
    private Integer status; // 0-待演唱, 1-演唱中, 2-已完成, 3-已取消
    private Integer queueNumber;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime singTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime completeTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    // 关联字段（非数据库字段）
    private String requesterName;
    private String requesterAvatar;
    private String singerName;
    private String singerAvatar;
}