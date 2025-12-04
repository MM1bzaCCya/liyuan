package com.example.liyuan.controller;

import com.example.liyuan.entity.User;
import com.example.liyuan.entity.UserAudio;
import com.example.liyuan.entity.PersonalPlaylist;
import com.example.liyuan.service.ProfileService;
import com.example.liyuan.util.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/profile")
public class ProfileController {

    @Autowired
    private ProfileService profileService;

    @Autowired
    private JwtUtils jwtUtils;

    /**
     * 获取用户个人信息
     */
    @GetMapping("/info")
    public ResponseEntity<?> getUserInfo(HttpServletRequest request) {
        try {
            Long userId = jwtUtils.getUserIdFromRequest(request);
            if (userId == null) {
                return ResponseEntity.status(401).body(createErrorResponse("未授权访问"));
            }

            User user = profileService.getUserProfile(userId);
            if (user == null) {
                return ResponseEntity.status(404).body(createErrorResponse("用户不存在"));
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", createUserInfoResponse(user));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(createErrorResponse("服务器内部错误"));
        }
    }

    /**
     * 更新用户个人信息
     */
    @PutMapping("/info")
    public ResponseEntity<?> updateUserInfo(
            @RequestBody Map<String, String> requestData,
            HttpServletRequest request) {
        try {
            Long userId = jwtUtils.getUserIdFromRequest(request);
            if (userId == null) {
                return ResponseEntity.status(401).body(createErrorResponse("未授权访问"));
            }

            String customNickname = requestData.get("customNickname");
            String bio = requestData.get("bio");

            if (customNickname == null && bio == null) {
                return ResponseEntity.badRequest().body(createErrorResponse("没有要更新的数据"));
            }

            boolean success = profileService.updateUserProfile(userId, customNickname, bio);

            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ? "更新成功" : "更新失败");

            if (success) {
                // 返回更新后的用户信息
                User user = profileService.getUserProfile(userId);
                response.put("data", createUserInfoResponse(user));
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(createErrorResponse("服务器内部错误"));
        }
    }

    /**
     * 获取用户录音列表
     */
    @GetMapping("/audios")
    public ResponseEntity<?> getUserAudios(HttpServletRequest request) {
        try {
            Long userId = jwtUtils.getUserIdFromRequest(request);
            if (userId == null) {
                return ResponseEntity.status(401).body(createErrorResponse("未授权访问"));
            }

            List<UserAudio> audios = profileService.getUserAudios(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", audios);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(createErrorResponse("服务器内部错误"));
        }
    }

    /**
     * 添加录音记录
     */
    @PostMapping("/audio")
    public ResponseEntity<?> addUserAudio(
            @RequestBody Map<String, Object> requestData,
            HttpServletRequest request) {
        try {
            Long userId = jwtUtils.getUserIdFromRequest(request);
            if (userId == null) {
                return ResponseEntity.status(401).body(createErrorResponse("未授权访问"));
            }

            String audioName = (String) requestData.get("audioName");
            String audioLocalPath = (String) requestData.get("audioLocalPath");
            Integer duration = (Integer) requestData.get("duration");

            if (audioName == null || audioName.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("录音名称不能为空"));
            }

            if (audioLocalPath == null || audioLocalPath.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("录音路径不能为空"));
            }

            boolean success = profileService.addUserAudio(
                    userId, audioName, audioLocalPath, duration);

            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ? "添加成功" : "添加失败");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(createErrorResponse("服务器内部错误"));
        }
    }

    /**
     * 删除录音记录
     */
    @DeleteMapping("/audio/{audioId}")
    public ResponseEntity<?> deleteUserAudio(
            @PathVariable Long audioId,
            HttpServletRequest request) {
        try {
            Long userId = jwtUtils.getUserIdFromRequest(request);
            if (userId == null) {
                return ResponseEntity.status(401).body(createErrorResponse("未授权访问"));
            }

            boolean success = profileService.deleteUserAudio(userId, audioId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ? "删除成功" : "删除失败");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(createErrorResponse("服务器内部错误"));
        }
    }

    /**
     * 获取个人歌单
     */
    @GetMapping("/playlist")
    public ResponseEntity<?> getPersonalPlaylist(HttpServletRequest request) {
        try {
            Long userId = jwtUtils.getUserIdFromRequest(request);
            if (userId == null) {
                return ResponseEntity.status(401).body(createErrorResponse("未授权访问"));
            }

            List<PersonalPlaylist> playlist = profileService.getPersonalPlaylist(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", playlist);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(createErrorResponse("服务器内部错误"));
        }
    }

    /**
     * 添加歌曲到个人歌单
     */
    @PostMapping("/playlist")
    public ResponseEntity<?> addToPlaylist(
            @RequestBody Map<String, Object> requestData,
            HttpServletRequest request) {
        try {
            Long userId = jwtUtils.getUserIdFromRequest(request);
            if (userId == null) {
                return ResponseEntity.status(401).body(createErrorResponse("未授权访问"));
            }

            String songName = (String) requestData.get("songName");
            String artist = (String) requestData.get("artist");
            Boolean isFavorite = (Boolean) requestData.get("isFavorite");

            if (songName == null || songName.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("歌曲名称不能为空"));
            }

            boolean success = profileService.addToPlaylist(
                    userId, songName, artist, isFavorite);

            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ? "添加成功" : "添加失败");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(createErrorResponse("服务器内部错误"));
        }
    }

    /**
     * 从个人歌单删除歌曲
     */
    @DeleteMapping("/playlist/{songId}")
    public ResponseEntity<?> removeFromPlaylist(
            @PathVariable Long songId,
            HttpServletRequest request) {
        try {
            Long userId = jwtUtils.getUserIdFromRequest(request);
            if (userId == null) {
                return ResponseEntity.status(401).body(createErrorResponse("未授权访问"));
            }

            boolean success = profileService.removeFromPlaylist(userId, songId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ? "删除成功" : "删除失败");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(createErrorResponse("服务器内部错误"));
        }
    }

    /**
     * 创建用户信息响应
     */
    private Map<String, Object> createUserInfoResponse(User user) {
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("wechatNickname", user.getNickname());
        userInfo.put("customNickname", user.getCustomNickname());
        userInfo.put("avatar", user.getAvatar());
        userInfo.put("bio", user.getBio());
        userInfo.put("gender", user.getGender());
        userInfo.put("userType", user.getUserType());
        userInfo.put("points", user.getPoints());
        return userInfo;
    }

    /**
     * 创建错误响应
     */
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("message", message);
        return errorResponse;
    }
    /**
     * 获取他人个人信息（公开信息）
     * 注意：这个方法不需要登录验证，或者只验证token但不要求是本人
     */
    @GetMapping("/public/{userId}")
    public ResponseEntity<?> getOtherUserInfo(
            @PathVariable Long userId,
            HttpServletRequest request) {
        try {
            // 可选：验证token，但不强制要求是本人
            // Long currentUserId = jwtUtils.getUserIdFromRequest(request);

            User user = profileService.getUserProfile(userId);
            if (user == null) {
                return ResponseEntity.status(404).body(createErrorResponse("用户不存在"));
            }

            // 只返回公开信息，隐藏敏感信息
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", createPublicUserInfoResponse(user));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(createErrorResponse("服务器内部错误"));
        }
    }

    /**
     * 获取他人的录音列表
     */
    @GetMapping("/public/{userId}/audios")
    public ResponseEntity<?> getOtherUserAudios(@PathVariable Long userId) {
        try {
            // 检查用户是否存在
            User user = profileService.getUserProfile(userId);
            if (user == null) {
                return ResponseEntity.status(404).body(createErrorResponse("用户不存在"));
            }

            List<UserAudio> audios = profileService.getUserAudios(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", audios);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(createErrorResponse("服务器内部错误"));
        }
    }

    /**
     * 获取他人的个人歌单
     */
    @GetMapping("/public/{userId}/playlist")
    public ResponseEntity<?> getOtherUserPlaylist(@PathVariable Long userId) {
        try {
            // 检查用户是否存在
            User user = profileService.getUserProfile(userId);
            if (user == null) {
                return ResponseEntity.status(404).body(createErrorResponse("用户不存在"));
            }

            List<PersonalPlaylist> playlist = profileService.getPersonalPlaylist(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", playlist);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(createErrorResponse("服务器内部错误"));
        }
    }

    /**
     * 创建公开用户信息响应（隐藏敏感信息）
     */
    private Map<String, Object> createPublicUserInfoResponse(User user) {
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("nickname", user.getNickname()); // 微信昵称
        userInfo.put("customNickname", user.getCustomNickname()); // 自定义昵称
        userInfo.put("avatar", user.getAvatar()); // 头像
        userInfo.put("bio", user.getBio()); // 个人简介
        userInfo.put("gender", user.getGender()); // 性别
        userInfo.put("userType", user.getUserType()); // 用户类型
        // 隐藏敏感信息：openid, unionid, country, province, city, createTime, updateTime等
        return userInfo;
    }
}