package com.example.liyuan.service;

import com.example.liyuan.entity.SongRequest;

import java.util.List;

public interface SongRequestService {
    boolean createRequest(Long userId, String songName, String originalSinger);
    List<SongRequest> getAvailableRequests();
    List<SongRequest> getUserRequests(Long userId);
    boolean updateRequestStatus(Long requestId, Integer status);
    SongRequest getRequestById(Long requestId);
}