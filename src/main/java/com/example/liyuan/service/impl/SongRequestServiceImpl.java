package com.example.liyuan.service.impl;

import com.example.liyuan.entity.SongRequest;
import com.example.liyuan.mapper.SongRequestMapper;
import com.example.liyuan.service.SongRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SongRequestServiceImpl implements SongRequestService {

    @Autowired
    private SongRequestMapper songRequestMapper;

    @Override
    public boolean createRequest(Long userId, String songName, String originalSinger) {
        SongRequest request = new SongRequest();
        request.setUserId(userId); // 使用 userId 参数
        request.setSongName(songName);
        request.setOriginalSinger(originalSinger);
        request.setStatus(0); // 待抢
        request.setCreateTime(LocalDateTime.now());
        request.setUpdateTime(LocalDateTime.now());
        return songRequestMapper.insert(request) > 0;
    }

    @Override
    public List<SongRequest> getAvailableRequests() {
        return songRequestMapper.selectAvailableRequestsWithUserInfo();
    }

    @Override
    public List<SongRequest> getUserRequests(Long userId) {
        return songRequestMapper.selectByUserId(userId);
    }

    @Override
    public boolean updateRequestStatus(Long requestId, Integer status) {
        SongRequest request = songRequestMapper.selectById(requestId);
        if (request != null) {
            request.setStatus(status);
            request.setUpdateTime(LocalDateTime.now());
            return songRequestMapper.updateStatus(request) > 0;
        }
        return false;
    }

    @Override
    public SongRequest getRequestById(Long requestId) {
        return songRequestMapper.selectById(requestId);
    }
}