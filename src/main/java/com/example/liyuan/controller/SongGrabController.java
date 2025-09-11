package com.example.liyuan.controller;

import com.example.liyuan.entity.SongGrab;
import com.example.liyuan.service.SongGrabService;
import com.example.liyuan.service.UserService;
import com.example.liyuan.util.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/songGrabs")
public class SongGrabController {

    @Autowired
    private SongGrabService songGrabService;
    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping
    public ResponseEntity<Map<String, Object>> grabSong(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, Long> request) {

        Long singerId = getUserIdFromToken(token.replace("Bearer ", ""));
        Long requestId = request.get("requestId");

        if (requestId == null) {
            return ResponseEntity.badRequest().body(Map.of("code", 400, "msg", "请求ID不能为空"));
        }

        boolean result = songGrabService.grabSong(requestId, singerId);
        if (result) {
            return ResponseEntity.ok(Map.of("code", 200, "msg", "抢歌成功"));
        } else {
            return ResponseEntity.badRequest().body(Map.of("code", 400, "msg", "抢歌失败，可能已经抢过"));
        }
    }

    @GetMapping("/request/{requestId}")
    public ResponseEntity<List<SongGrab>> getGrabsByRequest(@PathVariable Long requestId) {
        List<SongGrab> grabs = songGrabService.getGrabsByRequest(requestId);
        return ResponseEntity.ok(grabs);
    }

    @GetMapping("/singer")
    public ResponseEntity<List<SongGrab>> getGrabsBySinger(@RequestHeader("Authorization") String token) {
        Long singerId = getUserIdFromToken(token.replace("Bearer ", ""));
        List<SongGrab> grabs = songGrabService.getGrabsBySinger(singerId);
        return ResponseEntity.ok(grabs);
    }

    private Long getUserIdFromToken(String token) {
        return jwtUtils.getUserIdFromToken(token);  // 使用 JwtUtils 提供的方法解析 token
    }
}