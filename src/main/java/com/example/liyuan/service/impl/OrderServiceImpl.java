package com.example.liyuan.service.impl;

import com.example.liyuan.entity.Order;
import com.example.liyuan.mapper.OrderMapper;
import com.example.liyuan.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Override
    public boolean createOrderFromGrab(Long requestId, Long grabId, Long userId, Long singerId, String songName, String originalSinger) {
        Order order = new Order();
        order.setRequestId(requestId);
        order.setGrabId(grabId);
        order.setUserId(userId);
        order.setSingerId(singerId);
        order.setSongName(songName);
        order.setOriginalSinger(originalSinger);
        order.setStatus(0); // 待演唱

        // 设置排队号（当前最大排队号+1）
        Integer maxQueueNumber = orderMapper.selectMaxQueueNumber();
        order.setQueueNumber(maxQueueNumber != null ? maxQueueNumber + 1 : 1);

        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        return orderMapper.insert(order) > 0;
    }

    @Override
    public List<Order> getUserOrders(Long userId) {
        return orderMapper.selectByUserIdWithInfo(userId);
    }

    @Override
    public List<Order> getSingerOrders(Long singerId) {
        return orderMapper.selectBySingerIdWithInfo(singerId);
    }

    @Override
    public List<Order> getAllOrders() {
        return orderMapper.selectAllWithInfo();
    }

    @Override
    public boolean updateOrderStatus(Long orderId, Integer status) {
        Order order = orderMapper.selectById(orderId);
        if (order != null) {
            order.setStatus(status);
            order.setUpdateTime(LocalDateTime.now());
            if (status == 1) { // 演唱中，设置开始演唱时间
                order.setSingTime(LocalDateTime.now());
            } else if (status == 2) { // 已完成，设置完成时间
                order.setCompleteTime(LocalDateTime.now());
            }
            return orderMapper.update(order) > 0;
        }
        return false;
    }

    @Override
    public Order getOrderById(Long orderId) {
        return orderMapper.selectById(orderId);
    }
    @Override
    public boolean createOrderFromGrabWithPoints(Long requestId, Long grabId, Long userId, Long singerId,
                                                 String songName, String originalSinger, Integer points) {
        Order order = new Order();
        order.setRequestId(requestId);
        order.setGrabId(grabId);
        order.setUserId(userId);
        order.setSingerId(singerId);
        order.setSongName(songName);
        order.setOriginalSinger(originalSinger);
        order.setStatus(0); // 待演唱
        order.setAmount(BigDecimal.ZERO); // 积分支付，金额为0
        order.setPoints(points); // 记录消耗的积分
        order.setPaymentType("points"); // 支付类型为积分

        // 设置排队号（当前最大排队号+1）
        Integer maxQueueNumber = orderMapper.selectMaxQueueNumber();
        order.setQueueNumber(maxQueueNumber != null ? maxQueueNumber + 1 : 1);

        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());

        return orderMapper.insert(order) > 0;
    }

}
