package com.example.liyuan.service;

import com.example.liyuan.entity.Order;

import java.util.List;

public interface OrderService {
    boolean createOrderFromGrab(Long requestId, Long grabId, Long userId, Long singerId, String songName, String originalSinger);
    List<Order> getUserOrders(Long userId);
    List<Order> getSingerOrders(Long singerId);
    List<Order> getAllOrders();
    boolean updateOrderStatus(Long orderId, Integer status);
    Order getOrderById(Long orderId);
}