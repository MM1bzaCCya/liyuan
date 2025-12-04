// service/AdminService.java
package com.example.liyuan.service;

import java.util.List;
import java.util.Map;

public interface AdminService {

    /**
     * 获取用户列表（分页、搜索、筛选）
     */
    Map<String, Object> getUsers(int page, int size, String keyword, Integer userType);

    /**
     * 更新用户身份
     */
    boolean updateUserRole(Long userId, Integer userType);

    /**
     * 批量更新用户身份
     */
    int batchUpdateUserRole(List<Long> userIds, Integer userType);

    /**
     * 获取用户统计信息
     */
    Map<String, Object> getUserStats();

    // =========== 语音审核相关方法 ===========

    /**
     * 获取歌手音频列表（分组显示）
     */
    Map<String, Object> getSingerAudios(int page, int size, String keyword);

    /**
     * 删除音频
     */
    boolean deleteAudio(Long audioId);

    /**
     * 获取音频统计信息
     */
    Map<String, Object> getAudioStats();
    /**
     * 获取订单统计信息
     */
    Map<String, Object> getOrderStats();

    /**
     * 获取订单列表（按日期分组）
     */
    Map<String, Object> getOrders(int page, int size, String startDate, String endDate,
                                  String keyword, Integer status);

    /**
     * 更新订单状态
     */
    boolean updateOrderStatus(Long orderId, Integer status);
}