package com.example.liyuan.service;

import com.example.liyuan.entity.User;

public interface UserService {
    User getUserById(Long id);
    boolean updateUser(User user);
    Integer getUserPoints(Long id);
    boolean addUserPoints(Long id, Integer points);
    boolean updateUserType(Long userId, Integer userType);
}