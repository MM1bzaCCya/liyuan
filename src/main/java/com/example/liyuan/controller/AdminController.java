package com.example.liyuan.controller;

import com.example.liyuan.dto.AudioReviewDTO;
import com.example.liyuan.entity.User;
import com.example.liyuan.service.AdminService;
import com.example.liyuan.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private UserService userService;

    /**
     * 获取用户统计信息
     */
    @GetMapping("/users/stats")
    public ResponseEntity<Map<String, Object>> getUserStats() {
        try {
            Map<String, Object> stats = adminService.getUserStats();

            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("msg", "success");
            response.put("data", stats);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("msg", "获取统计信息失败: " + e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 获取所有用户列表（支持搜索和筛选）
     */
    @GetMapping("/users")
    public ResponseEntity<Map<String, Object>> getAllUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "15") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String userTypeStr) {  // 改为 String 类型

        try {
            Integer userType = null;
            if (userTypeStr != null && !"null".equals(userTypeStr) && !userTypeStr.isEmpty()) {
                try {
                    userType = Integer.parseInt(userTypeStr);
                } catch (NumberFormatException e) {
                    // 如果无法解析为整数，保持为 null
                    userType = null;
                }
            }

            Map<String, Object> result = adminService.getUsers(page, size, keyword, userType);

            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("msg", "success");
            response.put("data", result);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("msg", "获取用户列表失败: " + e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(500).body(response);
        }
    }
    /**
     * 修改用户身份
     */
    @PutMapping("/users/{userId}/role")
    public ResponseEntity<Map<String, Object>> updateUserRole(
            @PathVariable Long userId,
            @RequestBody Map<String, Object> requestData) {

        try {
            Integer userType = (Integer) requestData.get("userType");
            if (userType == null || userType < 0 || userType > 2) {
                Map<String, Object> response = new HashMap<>();
                response.put("code", 400);
                response.put("msg", "无效的用户类型");
                response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }

            boolean success = adminService.updateUserRole(userId, userType);

            Map<String, Object> response = new HashMap<>();
            if (success) {
                response.put("code", 200);
                response.put("msg", "修改成功");
            } else {
                response.put("code", 400);
                response.put("msg", "修改失败，用户不存在");
            }
            response.put("data", null);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("msg", "修改用户身份失败: " + e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 获取用户详情
     */
    @GetMapping("/users/{userId}")
    public ResponseEntity<Map<String, Object>> getUserDetail(@PathVariable Long userId) {
        try {
            User user = userService.getUserById(userId);

            Map<String, Object> response = new HashMap<>();
            if (user != null) {
                // 简化返回数据
                Map<String, Object> userData = new HashMap<>();
                userData.put("id", user.getId());
                userData.put("nickname", user.getNickname());
                userData.put("customNickname", user.getCustomNickname());
                userData.put("avatar", user.getAvatar());
                userData.put("gender", user.getGender());
                userData.put("userType", user.getUserType());
                userData.put("points", user.getPoints());
                userData.put("bio", user.getBio());
                userData.put("createTime", user.getCreateTime());

                response.put("code", 200);
                response.put("msg", "success");
                response.put("data", userData);
            } else {
                response.put("code", 404);
                response.put("msg", "用户不存在");
                response.put("data", null);
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("msg", "获取用户详情失败: " + e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 批量修改用户身份
     */
    @PutMapping("/users/batch-role")
    public ResponseEntity<Map<String, Object>> batchUpdateUserRole(
            @RequestBody Map<String, Object> requestData) {

        try {
            @SuppressWarnings("unchecked")
            java.util.List<Long> userIds = (java.util.List<Long>) requestData.get("userIds");
            Integer userType = (Integer) requestData.get("userType");

            if (userIds == null || userIds.isEmpty() || userType == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("code", 400);
                response.put("msg", "请求参数不完整");
                response.put("data", null);
                return ResponseEntity.badRequest().body(response);
            }

            int successCount = adminService.batchUpdateUserRole(userIds, userType);

            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("msg", "批量修改成功，共更新 " + successCount + " 个用户");
            response.put("data", null);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("msg", "批量修改失败: " + e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(500).body(response);
        }
    }
    @GetMapping("/audios")
    public ResponseEntity<Map<String, Object>> getAllAudios(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword) {

        try {
            Map<String, Object> result = adminService.getSingerAudios(page, size, keyword);

            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("msg", "success");
            response.put("data", result);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("msg", "获取语音列表失败: " + e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 删除语音
     */
    @DeleteMapping("/audios/{audioId}")
    public ResponseEntity<Map<String, Object>> deleteAudio(@PathVariable Long audioId) {
        try {
            boolean success = adminService.deleteAudio(audioId);

            Map<String, Object> response = new HashMap<>();
            if (success) {
                response.put("code", 200);
                response.put("msg", "删除成功");
            } else {
                response.put("code", 400);
                response.put("msg", "删除失败，语音不存在");
            }
            response.put("data", null);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("msg", "删除语音失败: " + e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * 获取语音统计信息
     */
    @GetMapping("/audios/stats")
    public ResponseEntity<Map<String, Object>> getAudioStats() {
        try {
            Map<String, Object> stats = adminService.getAudioStats();

            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("msg", "success");
            response.put("data", stats);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("msg", "获取统计信息失败: " + e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(500).body(response);
        }
    }
    @GetMapping("/orders/stats")
    public ResponseEntity<Map<String, Object>> getOrderStats() {
        try {
            Map<String, Object> stats = adminService.getOrderStats();
            return ResponseEntity.ok(Map.of(
                    "code", 200,
                    "msg", "成功",
                    "data", stats
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "code", 400,
                    "msg", "获取订单统计失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 获取订单列表（按日期分组）
     */
    @GetMapping("/orders")
    public ResponseEntity<Map<String, Object>> getOrders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        try {
            Map<String, Object> result = adminService.getOrders(
                    page, size, startDate, endDate, keyword, status);
            return ResponseEntity.ok(Map.of(
                    "code", 200,
                    "msg", "成功",
                    "data", result
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "code", 400,
                    "msg", "获取订单列表失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 修改订单状态
     */
    @PutMapping("/orders/{id}/status")
    public ResponseEntity<Map<String, Object>> updateOrderStatus(
            @PathVariable Long id,
            @RequestBody Map<String, Integer> request) {
        try {
            Integer status = request.get("status");
            if (status == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "code", 400,
                        "msg", "状态不能为空"
                ));
            }

            boolean success = adminService.updateOrderStatus(id, status);
            if (success) {
                return ResponseEntity.ok(Map.of(
                        "code", 200,
                        "msg", "订单状态更新成功"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                        "code", 400,
                        "msg", "订单状态更新失败"
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "code", 400,
                    "msg", "订单状态更新失败: " + e.getMessage()
            ));
        }
    }

}