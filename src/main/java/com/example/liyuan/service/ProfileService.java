package com.example.liyuan.service;

import com.example.liyuan.entity.User;
import com.example.liyuan.entity.UserAudio;
import com.example.liyuan.entity.PersonalPlaylist;
import java.util.List;

public interface ProfileService {

    /**
     * 获取用户个人信息
     * @param userId 用户ID
     * @return 用户信息
     */
    User getUserProfile(Long userId);

    /**
     * 更新用户自定义信息（昵称和简介）
     * @param userId 用户ID
     * @param customNickname 自定义昵称
     * @param bio 个人简介
     * @return 是否成功
     */
    boolean updateUserProfile(Long userId, String customNickname, String bio);

    /**
     * 获取用户的录音列表
     * @param userId 用户ID
     * @return 录音列表
     */
    List<UserAudio> getUserAudios(Long userId);  // 修改返回类型

    /**
     * 添加录音记录
     * @param userId 用户ID
     * @param audioName 录音名称
     * @param audioLocalPath 录音本地路径
     * @param duration 录音时长（秒）
     * @return 是否成功
     */
    boolean addUserAudio(Long userId, String audioName, String audioLocalPath, Integer duration);

    /**
     * 删除录音记录
     * @param userId 用户ID
     * @param audioId 录音ID
     * @return 是否成功
     */
    boolean deleteUserAudio(Long userId, Long audioId);

    /**
     * 获取个人歌单
     * @param userId 用户ID
     * @return 歌单列表
     */
    List<PersonalPlaylist> getPersonalPlaylist(Long userId);  // 修改返回类型

    /**
     * 添加歌曲到个人歌单
     * @param userId 用户ID
     * @param songName 歌曲名称
     * @param artist 歌手
     * @param isFavorite 是否收藏
     * @return 是否成功
     */
    boolean addToPlaylist(Long userId, String songName, String artist, Boolean isFavorite);

    /**
     * 从个人歌单删除歌曲
     * @param userId 用户ID
     * @param songId 歌曲ID
     * @return 是否成功
     */
    boolean removeFromPlaylist(Long userId, Long songId);

}