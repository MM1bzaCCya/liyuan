package com.example.liyuan.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.example.liyuan.mapper")
public class MyBatisConfig {
    // MyBatis 配置
}