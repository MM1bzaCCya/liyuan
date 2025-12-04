package com.example.liyuan.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtils {

//Jwt 签名密钥，从配置文件中读取
    @Value("${jwt.secret}")
    private String secret;

//设置token过期时间
    @Value("${jwt.expiration}")
    private Long expiration;
//生成Token
    public String generateToken(Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
//建立一个哈希表，将用户id存入claims中
        return Jwts.builder()
                .setClaims(claims) //设置自定义数据
                .setIssuedAt(new Date()) //设置发放时间
                .setExpiration(new Date(System.currentTimeMillis() + expiration)) //设置过期时间
                .signWith(SignatureAlgorithm.HS512, getSecretKey()) //使用还512算法签名
                .compact(); //生成最终token字符串
    }
//从Token中提起用户ID
    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(getSecretKey())//设置签名密钥
                .parseClaimsJws(token)//解析token
                .getBody();//获取payload部分
        return Long.parseLong(claims.get("userId").toString());
    }
//验证Token有效性
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(getSecretKey()).parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private byte[] getSecretKey() {
        // 使用 Java 8+ 的 Base64 类替代 DatatypeConverter
        return Base64.getEncoder().encode(secret.getBytes());
        //将字符串进行Base64编码，以符合JWT规范
    }
    public Long getUserIdFromRequest(HttpServletRequest request) {
        // 从请求头中获取Authorization
        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }

        // 提取Token（去掉"Bearer "前缀）
        String token = authHeader.substring(7);

        try {
            // 验证Token并获取用户ID
            if (validateToken(token)) {
                return getUserIdFromToken(token);
            }
        } catch (Exception e) {
            // Token无效或过期
            return null;
        }

        return null;
    }
}