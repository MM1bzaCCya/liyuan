package com.example.liyuan.service.impl;

import com.example.liyuan.entity.SongGrab;
import com.example.liyuan.entity.User;
import com.example.liyuan.exception.CustomException;
import com.example.liyuan.mapper.SongGrabMapper;
import com.example.liyuan.mapper.UserMapper;
import com.example.liyuan.service.SongGrabService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SongGrabServiceImpl implements SongGrabService {

    @Autowired
    private SongGrabMapper songGrabMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional
    public boolean grabSong(Long requestId, Long singerId) {
        return this.grabSong(requestId, singerId, null, null);
    }

    /**
     * 新增：带音频信息的抢歌方法
     * @param requestId 请求ID
     * @param singerId 歌手ID
     * @param audioLocalPath 音频本地路径
     * @param audioDuration 音频时长（秒）
     * @return 是否成功
     */
    @Transactional
    public boolean grabSong(Long requestId, Long singerId, String audioLocalPath, Integer audioDuration) {
        // 检查歌手是否存在
        User singer = userMapper.selectById(singerId);
        if (singer == null) {
            throw new CustomException(400, "歌手用户不存在");
        }

        // 检查是否已经抢过
        if (songGrabMapper.countByRequestAndSinger(requestId, singerId) > 0) {
            throw new CustomException(400, "您已经抢过这首歌了");
        }

        SongGrab grab = new SongGrab();
        grab.setRequestId(requestId);
        grab.setSingerId(singerId);
        grab.setStatus(0); // 待确认
        grab.setAudioLocalPath(audioLocalPath);
        grab.setAudioDuration(audioDuration);
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
    @Transactional
    public boolean updateGrabStatus(Long grabId, Integer status) {
        SongGrab grab = songGrabMapper.selectById(grabId);
        if (grab != null) {
            grab.setStatus(status);
            grab.setUpdateTime(LocalDateTime.now());
            return songGrabMapper.updateStatus(grab) > 0;
        }
        return false;
    }

    /**
     * 新增：更新音频信息
     * @param grabId 抢歌ID
     * @param audioLocalPath 音频本地路径
     * @param audioDuration 音频时长（秒）
     * @return 是否成功
     */
    @Transactional
    public boolean updateAudioInfo(Long grabId, String audioLocalPath, Integer audioDuration) {
        SongGrab grab = songGrabMapper.selectById(grabId);
        if (grab != null) {
            grab.setAudioLocalPath(audioLocalPath);
            grab.setAudioDuration(audioDuration);
            grab.setUpdateTime(LocalDateTime.now());
            return songGrabMapper.updateAudioInfo(grab) > 0;
        }
        return false;
    }

    @Override
    public SongGrab getGrabById(Long grabId) {
        return songGrabMapper.selectById(grabId);
    }
}