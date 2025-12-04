package com.example.liyuan.service.impl;

import com.example.liyuan.entity.User;
import com.example.liyuan.entity.UserAudio;
import com.example.liyuan.entity.PersonalPlaylist;
import com.example.liyuan.mapper.UserMapper;
import com.example.liyuan.mapper.UserAudioMapper;
import com.example.liyuan.mapper.PersonalPlaylistMapper;
import com.example.liyuan.service.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProfileServiceImpl implements ProfileService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserAudioMapper userAudioMapper;

    @Autowired
    private PersonalPlaylistMapper personalPlaylistMapper;

    @Override
    public User getUserProfile(Long userId) {
        return userMapper.selectById(userId);
    }

    @Override
    @Transactional
    public boolean updateUserProfile(Long userId, String customNickname, String bio) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            return false;
        }

        user.setCustomNickname(customNickname);
        user.setBio(bio);
        user.setUpdateTime(LocalDateTime.now());

        return userMapper.updateCustomInfo(user) > 0;
    }

    @Override
    public List<UserAudio> getUserAudios(Long userId) {
        return userAudioMapper.findByUserId(userId);
    }

    @Override
    @Transactional
    public boolean addUserAudio(Long userId, String audioName, String audioLocalPath, Integer duration) {
        UserAudio userAudio = new UserAudio();
        userAudio.setUserId(userId);
        userAudio.setAudioName(audioName);
        userAudio.setAudioLocalPath(audioLocalPath);
        userAudio.setAudioDuration(duration);
        userAudio.setCreatedAt(LocalDateTime.now());

        return userAudioMapper.insert(userAudio) > 0;
    }

    @Override
    @Transactional
    public boolean deleteUserAudio(Long userId, Long audioId) {
        return userAudioMapper.deleteByIdAndUserId(audioId, userId) > 0;
    }

    @Override
    public List<PersonalPlaylist> getPersonalPlaylist(Long userId) {
        return personalPlaylistMapper.findByUserId(userId);
    }

    @Override
    @Transactional
    public boolean addToPlaylist(Long userId, String songName, String artist, Boolean isFavorite) {
        PersonalPlaylist playlist = new PersonalPlaylist();
        playlist.setUserId(userId);
        playlist.setSongName(songName);
        playlist.setArtist(artist);
        playlist.setIsFavorite(isFavorite != null ? isFavorite : false);
        playlist.setAddTime(LocalDateTime.now());

        return personalPlaylistMapper.insert(playlist) > 0;
    }

    @Override
    @Transactional
    public boolean removeFromPlaylist(Long userId, Long songId) {
        return personalPlaylistMapper.deleteByIdAndUserId(songId, userId) > 0;
    }

    // 新增：获取用户收藏的歌曲
    public List<PersonalPlaylist> getFavoriteSongs(Long userId) {
        return personalPlaylistMapper.findFavoritesByUserId(userId);
    }
}