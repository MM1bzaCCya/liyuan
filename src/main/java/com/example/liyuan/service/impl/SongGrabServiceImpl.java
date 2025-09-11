package com.example.liyuan.service.impl;

import com.example.liyuan.entity.SongGrab;
import com.example.liyuan.entity.User;
import com.example.liyuan.exception.CustomException;
import com.example.liyuan.mapper.SongGrabMapper;
import com.example.liyuan.service.SongGrabService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.liyuan.mapper.UserMapper;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class SongGrabServiceImpl implements SongGrabService {

    @Autowired
    private SongGrabMapper songGrabMapper;
    @Autowired
    private UserMapper UserMapper;

    @Override
    public boolean grabSong(Long requestId, Long singerId) {
        // 检查歌手是否存在
        User singer = UserMapper.selectById(singerId);
        if (singer == null) {
            throw new CustomException(400, "歌手用户不存在");
        }

        // 检查是否已经抢过
        if (songGrabMapper.countByRequestAndSinger(requestId, singerId) > 0) {
            return false; // 已经抢过
        }

        SongGrab grab = new SongGrab();
        grab.setRequestId(requestId);
        grab.setSingerId(singerId);
        grab.setStatus(0); // 待确认
        grab.setCreateTime(LocalDateTime.now());
        grab.setUpdateTime(LocalDateTime.now());
        return songGrabMapper.insert(grab) > 0;
    }

    @Override
    public List<SongGrab> getGrabsByRequest(Long requestId) {
        return songGrabMapper.selectByRequestIdWithInfo(requestId);
    }

    @Override
    public List<SongGrab> getGrabsBySinger(Long singerId) {
        return songGrabMapper.selectBySingerIdWithInfo(singerId);
    }

    @Override
    public boolean updateGrabStatus(Long grabId, Integer status) {
        SongGrab grab = songGrabMapper.selectById(grabId);
        if (grab != null) {
            grab.setStatus(status);
            grab.setUpdateTime(LocalDateTime.now());
            return songGrabMapper.updateStatus(grab) > 0;
        }
        return false;
    }

    @Override
    public SongGrab getGrabById(Long grabId) {
        return songGrabMapper.selectById(grabId);
    }
}