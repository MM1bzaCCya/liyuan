package com.example.liyuan.controller;

import com.example.liyuan.dto.HotSingerDTO;
import com.example.liyuan.dto.HotSongDTO;
import com.example.liyuan.service.SongStatisticService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/hot")
public class SongStatisticController {

    private static final Logger logger = LoggerFactory.getLogger(SongStatisticController.class);

    @Autowired
    private SongStatisticService songStatisticService;

    /**
     * 获取热门歌曲列表
     */
    @GetMapping("/hot-songs")
    public ResponseEntity<?> getHotSongs() {
        logger.info("接收 /hot/hot-songs 请求");

        try {
            List<HotSongDTO> hotSongs = songStatisticService.getHotSongs();
            logger.info("获取到 {} 首热门歌曲", hotSongs.size());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", hotSongs);
            response.put("message", "获取热门歌曲成功");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("获取热门歌曲失败", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "获取热门歌曲失败: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
    /**
     * 获取热门歌手列表
     */
    @GetMapping("/hot-singers")
    public ResponseEntity<?> getHotSingers() {
        logger.info("接收 /hot/hot-singers 请求");

        try {
            List<HotSingerDTO> hotSingers = songStatisticService.getHotSingers();
            logger.info("获取到 {} 位热门歌手", hotSingers.size());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", hotSingers);
            response.put("message", "获取热门歌手成功");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("获取热门歌手失败", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "获取热门歌手失败: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}