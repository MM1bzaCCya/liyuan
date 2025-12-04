package com.example.liyuan.service.impl;

import com.example.liyuan.entity.User;
import com.example.liyuan.mapper.UserMapper;
import com.example.liyuan.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public User getUserById(Long id) {
        return userMapper.selectById(id);
    }

    @Override
    public boolean updateUser(User user) {
        user.setUpdateTime(LocalDateTime.now());
        return userMapper.update(user) > 0;
    }

    @Override
    public Integer getUserPoints(Long id) {
        User user = userMapper.selectById(id);
        return user != null ? user.getPoints() : 0;
    }

    @Override
    public boolean addUserPoints(Long id, Integer points) {
        User user = userMapper.selectById(id);
        if (user != null) {
            user.setPoints(user.getPoints() + points);
            user.setUpdateTime(LocalDateTime.now());
            return userMapper.update(user) > 0;
        }
        return false;
    }

    @Override
    public boolean updateUserType(Long userId, Integer userType) {
        User user = userMapper.selectById(userId);
        if (user != null) {
            user.setUserType(userType);
            user.setUpdateTime(LocalDateTime.now());
            return userMapper.updateUserType(user) > 0;
        }
        return false;
    }
    @Override
    public boolean deductUserPoints(Long id, Integer points) {
        User user = userMapper.selectById(id);
        if (user != null) {
            if (user.getPoints() < points) {
                throw new RuntimeException("积分不足，当前积分：" + user.getPoints());
            }
            user.setPoints(user.getPoints() - points);
            user.setUpdateTime(LocalDateTime.now());
            return userMapper.update(user) > 0;
        }
        return false;
    }
}