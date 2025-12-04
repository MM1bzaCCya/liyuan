package com.example.liyuan.service.impl;

import com.example.liyuan.dto.HotSongDTO;
import com.example.liyuan.dto.HotSingerDTO;
import com.example.liyuan.mapper.OrderMapper;
import com.example.liyuan.service.SongStatisticService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SongStatisticServiceImpl implements SongStatisticService {

    @Autowired
    private OrderMapper orderMapper;

    @Override
    public List<HotSongDTO> getHotSongs() {
        List<HotSongDTO> hotSongs = orderMapper.findHotSongs();
        return hotSongs;
    }

    @Override
    public List<HotSingerDTO> getHotSingers() {
        List<HotSingerDTO> hotSingers = orderMapper.findHotSingers();
        return hotSingers;
    }
}