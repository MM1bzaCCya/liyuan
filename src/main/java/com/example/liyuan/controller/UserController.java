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
    @PostMapping("/depositPoints")
    public ResponseEntity<Map<String, Object>> depositPoints(@RequestHeader("Authorization") String token,
                                                             @RequestBody Map<String, Integer> request) {
        try {
            // 从token中获取用户ID
            Long userId = getUserIdFromToken(token.replace("Bearer ", ""));

            Integer points = request.get("points");

            if (points == null || points <= 0) {
                return ResponseEntity.badRequest().body(Map.of(
                        "code", 400,
                        "msg", "积分数量必须为正整数"
                ));
            }

            // 检查积分档位是否合法（10, 20, 50, 100, 200, 300）
            int[] validPoints = {10, 20, 50, 100, 200, 300};
            boolean isValid = false;
            for (int validPoint : validPoints) {
                if (points == validPoint) {
                    isValid = true;
                    break;
                }
            }

            if (!isValid) {
                return ResponseEntity.badRequest().body(Map.of(
                        "code", 400,
                        "msg", "请选择正确的积分档位"
                ));
            }

            // 获取当前用户
            User user = userService.getUserById(userId);
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "code", 400,
                        "msg", "用户不存在"
                ));
            }

            // 增加用户积分
            boolean result = userService.addUserPoints(userId, points);

            if (result) {
                // 返回更新后的用户信息
                User updatedUser = userService.getUserById(userId);
                return ResponseEntity.ok(Map.of(
                        "code", 200,
                        "msg", "积分充值成功",
                        "data", Map.of(
                                "userId", userId,
                                "points", updatedUser.getPoints(),
                                "depositAmount", points
                        )
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                        "code", 400,
                        "msg", "积分充值失败"
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "code", 400,
                    "msg", "充值失败: " + e.getMessage()
            ));
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