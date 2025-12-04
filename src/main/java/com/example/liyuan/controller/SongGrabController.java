package com.example.liyuan.controller;

import com.example.liyuan.entity.SongGrab;
import com.example.liyuan.service.SongGrabService;
import com.example.liyuan.util.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/songGrabs")
public class SongGrabController {

    @Autowired
    private SongGrabService songGrabService;

    @Autowired
    private JwtUtils jwtUtils;

    /**
     * 抢歌接口（支持带音频信息）
     * 请求体：{"requestId": 123, "audioLocalPath": "path/to/audio.mp3", "audioDuration": 15}
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> grabSong(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, Object> requestBody) {

        Long singerId = getUserIdFromToken(token.replace("Bearer ", ""));

        // 提取请求参数
        Long requestId = null;
        String audioLocalPath = null;
        Integer audioDuration = null;

        try {
            if (requestBody.get("requestId") != null) {
                requestId = Long.valueOf(requestBody.get("requestId").toString());
            }

            if (requestBody.get("audioLocalPath") != null) {
                audioLocalPath = requestBody.get("audioLocalPath").toString();
            }

            if (requestBody.get("audioDuration") != null) {
                audioDuration = Integer.valueOf(requestBody.get("audioDuration").toString());
            }
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(createResponse(400, "请求参数格式错误"));
        }

        if (requestId == null) {
            return ResponseEntity.badRequest().body(createResponse(400, "请求ID不能为空"));
        }

        boolean result;

        // 根据是否有音频信息调用不同的方法
        if (audioLocalPath != null && audioDuration != null) {
            // 带音频信息的抢歌
            result = songGrabService.grabSong(requestId, singerId, audioLocalPath, audioDuration);
        } else {
            // 不带音频信息的抢歌（保持向后兼容）
            result = songGrabService.grabSong(requestId, singerId);
        }

        if (result) {
            return ResponseEntity.ok(createResponse(200, "抢歌成功"));
        } else {
            return ResponseEntity.badRequest().body(createResponse(400, "抢歌失败，可能已经抢过或歌手不存在"));
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

    /**
     * 更新抢歌记录的音频信息
     * 用于已抢歌但未录音的情况
     */
    @PutMapping("/{grabId}/audio")
    public ResponseEntity<Map<String, Object>> updateAudioInfo(
            @RequestHeader("Authorization") String token,
            @PathVariable Long grabId,
            @RequestBody Map<String, Object> audioInfo) {

        Long singerId = getUserIdFromToken(token.replace("Bearer ", ""));

        // 验证该抢歌记录是否属于当前用户
        SongGrab grab = songGrabService.getGrabById(grabId);
        if (grab == null) {
            return ResponseEntity.badRequest().body(createResponse(400, "抢歌记录不存在"));
        }

        if (!grab.getSingerId().equals(singerId)) {
            return ResponseEntity.badRequest().body(createResponse(403, "无权修改此抢歌记录"));
        }

        String audioLocalPath = null;
        Integer audioDuration = null;

        try {
            if (audioInfo.get("audioLocalPath") != null) {
                audioLocalPath = audioInfo.get("audioLocalPath").toString();
            }

            if (audioInfo.get("audioDuration") != null) {
                audioDuration = Integer.valueOf(audioInfo.get("audioDuration").toString());
            }
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(createResponse(400, "音频信息格式错误"));
        }

        if (audioLocalPath == null || audioDuration == null) {
            return ResponseEntity.badRequest().body(createResponse(400, "音频信息不能为空"));
        }

        boolean result = songGrabService.updateAudioInfo(grabId, audioLocalPath, audioDuration);
        if (result) {
            return ResponseEntity.ok(createResponse(200, "音频信息更新成功"));
        } else {
            return ResponseEntity.badRequest().body(createResponse(400, "音频信息更新失败"));
        }
    }

    /**
     * 获取特定抢歌记录的详情
     */
    @GetMapping("/{grabId}")
    public ResponseEntity<SongGrab> getGrabDetail(
            @RequestHeader("Authorization") String token,
            @PathVariable Long grabId) {

        SongGrab grab = songGrabService.getGrabById(grabId);
        if (grab == null) {
            return ResponseEntity.notFound().build();
        }

        // 验证权限：只有相关用户（点歌者或抢歌者）可以查看
        Long userId = getUserIdFromToken(token.replace("Bearer ", ""));

        // TODO: 根据业务需求添加更详细的权限验证
        // 例如，检查用户是否是点歌者或抢歌者

        return ResponseEntity.ok(grab);
    }

    private Long getUserIdFromToken(String token) {
        return jwtUtils.getUserIdFromToken(token);
    }

    private Map<String, Object> createResponse(int code, String msg) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", code);
        response.put("msg", msg);
        return response;
    }
}