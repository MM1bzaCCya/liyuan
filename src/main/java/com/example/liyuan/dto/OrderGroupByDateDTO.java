package com.example.liyuan.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class OrderGroupByDateDTO {
    private LocalDate date;               // 日期
    private BigDecimal dailyIncome;       // 当日总收入
    private Integer orderCount;          // 当日订单数量
    private List<OrderDetailDTO> orders; // 当日订单列表
}