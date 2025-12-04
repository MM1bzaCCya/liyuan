package com.example.liyuan.service;

import com.example.liyuan.dto.HotSongDTO;
import com.example.liyuan.dto.HotSingerDTO;
import java.util.List;

public interface SongStatisticService {
    /**
     * 获取热门歌曲列表（按点歌次数排序）
     * @return 热门歌曲列表
     */
    List<HotSongDTO> getHotSongs();

    /**
     * 获取热门歌手列表（按被点歌次数排序）
     * @return 热门歌手列表
     */
    List<HotSingerDTO> getHotSingers();
}