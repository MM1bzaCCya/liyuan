package com.example.liyuan.service;

import com.example.liyuan.entity.SongGrab;

import java.util.List;

public interface SongGrabService {
    boolean grabSong(Long requestId, Long singerId);
    List<SongGrab> getGrabsByRequest(Long requestId);
    List<SongGrab> getGrabsBySinger(Long singerId);
    boolean updateGrabStatus(Long grabId, Integer status);
    SongGrab getGrabById(Long grabId);
}