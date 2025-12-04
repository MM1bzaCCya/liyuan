package com.example.liyuan.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderDetailDTO {
    private Long id;
    private String orderNo;              // 订单编号
    private Long userId;                 // 点歌人ID
    private String userNickname;         // 点歌人昵称
    private String userAvatar;           // 点歌人头像
    private Long singerId;               // 歌手ID
    private String singerNickname;       // 歌手昵称
    private String singerAvatar;         // 歌手头像
    private String songName;             // 歌曲名称
    private String originalSinger;       // 原唱歌手
    private BigDecimal amount;           // 金额
    private Integer points;              // 积分（如果是积分支付）
    private String paymentType;          // 支付方式
    private Integer status;              // 订单状态
    private String statusText;           // 状态文本
    private Integer queueNumber;         // 排队号

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime completeTime;  // 完成时间

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;    // 创建时间
}