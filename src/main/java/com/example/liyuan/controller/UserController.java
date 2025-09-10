package com.example.liyuan.controller;

import com.example.liyuan.entity.User;
import com.example.liyuan.service.UserService;
import com.example.liyuan.util.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    private static final String PASSWORD = "MM1bzaCC";  // 固定密码
    @Autowired
    private JwtUtils jwtUtils;

    // 获取用户信息
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        if (user != null) {
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // 更新用户信息
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateUser(@PathVariable Long id, @RequestBody User user) {
        user.setId(id);
        boolean result = userService.updateUser(user);
        if (result) {
            return ResponseEntity.ok(Map.of("code", 200, "msg", "用户信息更新成功"));
        } else {
            return ResponseEntity.badRequest().body(Map.of("code", 400, "msg", "用户信息更新失败"));
        }
    }




    private Long getUserIdFromToken(String token) {
        // 使用 JwtUtils 来解析 token 获取 userId
        return jwtUtils.getUserIdFromToken(token.replace("Bearer ", ""));  // 调用 JwtUtils 中的方法
    }

    @PostMapping("/becomeSinger")
    public ResponseEntity<Map<String, Object>> becomeSinger(@RequestHeader("Authorization") String token,
                                                            @RequestBody Map<String, String> request) {
        try {
            // 从token中获取用户ID
            Long userId = getUserIdFromToken(token);  // 使用新的 getUserIdFromToken 方法

            String password = request.get("password");
            String correctPassword = "MM1bzaCC"; // 正确密码

            if (!correctPassword.equals(password)) {
                return ResponseEntity.badRequest().body(Map.of(
                        "code", 400,
                        "msg", "认证密码错误"
                ));
            }

            // 更新用户身份为歌手
            boolean result = userService.updateUserType(userId, 1); // 1表示歌手身份

            if (result) {
                return ResponseEntity.ok(Map.of(
                        "code", 200,
                        "msg", "成功成为认证歌手"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                        "code", 400,
                        "msg", "身份更新失败"
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "code", 400,
                    "msg", "申请失败: " + e.getMessage()
            ));
        }
    }
}