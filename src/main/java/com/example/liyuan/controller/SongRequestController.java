package com.example.liyuan.controller;

import com.example.liyuan.entity.SongRequest;
import com.example.liyuan.entity.User;
import com.example.liyuan.service.SongRequestService;
import com.example.liyuan.service.UserService;
import com.example.liyuan.util.JwtUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/songRequests")
public class SongRequestController {

    private static final Logger logger = LoggerFactory.getLogger(SongRequestController.class);

    @Autowired
    private SongRequestService songRequestService;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping
    public ResponseEntity<Map<String, Object>> createSongRequest(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, String> request) {

        try {
            // 从token中获取用户ID
            Long userId = getUserIdFromToken(token);
            logger.info("Creating song request for user ID: {}", userId);

            // 验证用户是否存在
            User user = userService.getUserById(userId);
            if (user == null) {
                logger.error("User not found with ID: {}", userId);
                return ResponseEntity.badRequest().body(Map.of(
                        "code", 400,
                        "msg", "用户不存在"
                ));
            }

            String songName = request.get("songName");
            String originalSinger = request.get("originalSinger");

            if (songName == null || originalSinger == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "code", 400,
                        "msg", "歌曲名称和原唱歌手不能为空"
                ));
            }

            boolean result = songRequestService.createRequest(userId, songName, originalSinger);
            if (result) {
                return ResponseEntity.ok(Map.of(
                        "code", 200,
                        "msg", "点歌成功"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                        "code", 400,
                        "msg", "点歌失败"
                ));
            }
        } catch (Exception e) {
            logger.error("点歌失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                    "code", 400,
                    "msg", "点歌失败: " + e.getMessage()
            ));
        }
    }
    @GetMapping("/available")
    public ResponseEntity<List<SongRequest>> getAvailableRequests() {
        try {
            logger.info("获取待抢歌曲列表");
            List<SongRequest> requests = songRequestService.getAvailableRequests();
            logger.info("找到 {} 首待抢歌曲", requests.size());
            return ResponseEntity.ok(requests);
        } catch (Exception e) {
            logger.error("获取待抢歌曲失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(null);
        }
    }


    @GetMapping("/user")
    public ResponseEntity<List<SongRequest>> getUserRequests(@RequestHeader("Authorization") String token) {
        try {
            Long userId = getUserIdFromToken(token);
            List<SongRequest> requests = songRequestService.getUserRequests(userId);
            return ResponseEntity.ok(requests);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    // 从token中获取用户ID的方法
    private Long getUserIdFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("无效的认证信息");
        }

        String token = authHeader.substring(7);
        if (!jwtUtils.validateToken(token)) {
            throw new RuntimeException("Token验证失败");
        }

        return jwtUtils.getUserIdFromToken(token);
    }
}