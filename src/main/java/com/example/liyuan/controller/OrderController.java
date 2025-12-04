// 在OrderController中添加积分支付接口
package com.example.liyuan.controller;

import com.example.liyuan.entity.Order;
import com.example.liyuan.entity.SongGrab;
import com.example.liyuan.entity.SongRequest;
import com.example.liyuan.service.OrderService;
import com.example.liyuan.service.SongGrabService;
import com.example.liyuan.service.SongRequestService;
import com.example.liyuan.service.UserService;
import com.example.liyuan.util.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private SongGrabService songGrabService;

    @Autowired
    private SongRequestService songRequestService;

    @Autowired
    private UserService userService;  // 新增：注入UserService

    @Autowired
    private JwtUtils jwtUtils;

    @PostMapping("/confirmGrab")
    public ResponseEntity<Map<String, Object>> confirmGrab(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, Long> request) {

        try {
            // 从token中获取用户ID
            Long userId = getUserIdFromToken(token);
            Long grabId = request.get("grabId");

            if (grabId == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "code", 400,
                        "msg", "抢歌记录ID不能为空"
                ));
            }

            // 根据grabId获取抢歌记录
            SongGrab songGrab = songGrabService.getGrabById(grabId);
            if (songGrab == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "code", 400,
                        "msg", "抢歌记录不存在"
                ));
            }

            // 验证当前用户是否有权限确认这个抢歌记录
            // 只有点歌请求的发起者才能确认抢歌
            SongRequest songRequest = songRequestService.getRequestById(songGrab.getRequestId());
            if (songRequest == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "code", 400,
                        "msg", "点歌请求不存在"
                ));
            }

            if (!songRequest.getUserId().equals(userId)) {
                return ResponseEntity.badRequest().body(Map.of(
                        "code", 400,
                        "msg", "无权确认此抢歌记录"
                ));
            }

            // 检查抢歌记录状态是否为待确认
            if (songGrab.getStatus() != 0) {
                return ResponseEntity.badRequest().body(Map.of(
                        "code", 400,
                        "msg", "抢歌记录状态不正确"
                ));
            }

            // 获取点歌请求的详细信息
            String songName = songRequest.getSongName();
            String originalSinger = songRequest.getOriginalSinger();
            Long requestId = songRequest.getId();
            Long singerId = songGrab.getSingerId();

            // 创建订单
            boolean result = orderService.createOrderFromGrab(requestId, grabId, userId, singerId, songName, originalSinger);

            if (result) {
                // 更新抢歌记录状态为已确认
                songGrabService.updateGrabStatus(grabId, 1);

                // 更新点歌请求状态为已抢
                songRequestService.updateRequestStatus(requestId, 1);

                // 拒绝其他抢歌同一首歌的记录
                List<SongGrab> otherGrabs = songGrabService.getGrabsByRequest(requestId);
                for (SongGrab otherGrab : otherGrabs) {
                    if (!otherGrab.getId().equals(grabId) && otherGrab.getStatus() == 0) {
                        songGrabService.updateGrabStatus(otherGrab.getId(), 2); // 设置为已拒绝
                    }
                }

                return ResponseEntity.ok(Map.of(
                        "code", 200,
                        "msg", "订单创建成功"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                        "code", 400,
                        "msg", "订单创建失败"
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "code", 400,
                    "msg", "订单创建失败: " + e.getMessage()
            ));
        }
    }

    /**
     * 积分支付确认抢歌创建订单
     * 消耗30积分（29.9四舍五入）
     * 新增接口，不影响原有功能
     */
    @PostMapping("/confirmGrabWithPoints")
    @Transactional
    public ResponseEntity<Map<String, Object>> confirmGrabWithPoints(
            @RequestHeader("Authorization") String token,
            @RequestBody Map<String, Object> request) {

        try {
            // 从token中获取用户ID
            Long userId = getUserIdFromToken(token);

            // 获取请求参数
            Long grabId = Long.valueOf(request.get("grabId").toString());
            Integer points = Integer.valueOf(request.get("points").toString());

            if (grabId == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "code", 400,
                        "msg", "抢歌记录ID不能为空"
                ));
            }

            // 验证积分数量（点歌固定消耗30积分）
            if (points != 30) {
                return ResponseEntity.badRequest().body(Map.of(
                        "code", 400,
                        "msg", "点歌需要支付30积分"
                ));
            }

            // 检查用户积分是否足够
            Integer userPoints = userService.getUserPoints(userId);
            if (userPoints < points) {
                return ResponseEntity.badRequest().body(Map.of(
                        "code", 400,
                        "msg", "积分不足，当前积分：" + userPoints
                ));
            }

            // 根据grabId获取抢歌记录
            SongGrab songGrab = songGrabService.getGrabById(grabId);
            if (songGrab == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "code", 400,
                        "msg", "抢歌记录不存在"
                ));
            }

            // 验证当前用户是否有权限确认这个抢歌记录
            SongRequest songRequest = songRequestService.getRequestById(songGrab.getRequestId());
            if (songRequest == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "code", 400,
                        "msg", "点歌请求不存在"
                ));
            }

            if (!songRequest.getUserId().equals(userId)) {
                return ResponseEntity.badRequest().body(Map.of(
                        "code", 400,
                        "msg", "无权确认此抢歌记录"
                ));
            }

            // 检查抢歌记录状态是否为待确认
            if (songGrab.getStatus() != 0) {
                return ResponseEntity.badRequest().body(Map.of(
                        "code", 400,
                        "msg", "抢歌记录状态不正确"
                ));
            }

            // 扣除用户积分
            boolean pointsDeducted = userService.deductUserPoints(userId, points);
            if (!pointsDeducted) {
                return ResponseEntity.badRequest().body(Map.of(
                        "code", 400,
                        "msg", "积分扣除失败"
                ));
            }

            // 获取点歌请求的详细信息
            String songName = songRequest.getSongName();
            String originalSinger = songRequest.getOriginalSinger();
            Long requestId = songRequest.getId();
            Long singerId = songGrab.getSingerId();

            // 创建订单（使用原有的createOrderFromGrab方法，金额默认为29.90）
            boolean orderCreated = orderService.createOrderFromGrab(requestId, grabId, userId, singerId, songName, originalSinger);

            if (orderCreated) {
                // 获取最新创建的订单ID
                List<Order> userOrders = orderService.getUserOrders(userId);
                Order latestOrder = userOrders.stream()
                        .filter(order -> order.getGrabId().equals(grabId))
                        .findFirst()
                        .orElse(null);

                // 如果是最新订单，标记为积分支付（这里可以先简单处理，如果需要可以更新订单字段）
                if (latestOrder != null) {
                    // 如果需要记录积分支付信息，可以在这里更新订单
                    // 由于订单表目前可能没有积分字段，这里先不处理
                    // 后续可以扩展订单表添加积分支付记录
                }

                // 更新抢歌记录状态为已确认
                songGrabService.updateGrabStatus(grabId, 1);

                // 更新点歌请求状态为已抢
                songRequestService.updateRequestStatus(requestId, 1);

                // 拒绝其他抢歌同一首歌的记录
                List<SongGrab> otherGrabs = songGrabService.getGrabsByRequest(requestId);
                for (SongGrab otherGrab : otherGrabs) {
                    if (!otherGrab.getId().equals(grabId) && otherGrab.getStatus() == 0) {
                        songGrabService.updateGrabStatus(otherGrab.getId(), 2); // 设置为已拒绝
                    }
                }

                // 获取扣除积分后的用户信息
                Integer currentPoints = userService.getUserPoints(userId);

                return ResponseEntity.ok(Map.of(
                        "code", 200,
                        "msg", "积分支付成功，订单创建成功",
                        "data", Map.of(
                                "orderId", latestOrder != null ? latestOrder.getId() : null,
                                "remainingPoints", currentPoints,
                                "deductedPoints", points
                        )
                ));
            } else {
                // 如果订单创建失败，返还积分
                userService.addUserPoints(userId, points);
                return ResponseEntity.badRequest().body(Map.of(
                        "code", 400,
                        "msg", "订单创建失败，积分已返还"
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "code", 400,
                    "msg", "积分支付失败: " + e.getMessage()
            ));
        }
    }

    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        try {
            List<Order> orders = orderService.getAllOrders();
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/user")
    public ResponseEntity<List<Order>> getUserOrders(@RequestHeader("Authorization") String token) {
        try {
            Long userId = getUserIdFromToken(token);
            List<Order> orders = orderService.getUserOrders(userId);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/singer")
    public ResponseEntity<List<Order>> getSingerOrders(@RequestHeader("Authorization") String token) {
        try {
            Long singerId = getUserIdFromToken(token);
            List<Order> orders = orderService.getSingerOrders(singerId);
            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<Map<String, Object>> updateOrderStatus(
            @RequestHeader("Authorization") String token,
            @PathVariable Long orderId,
            @RequestBody Map<String, Integer> request) {

        try {
            Long userId = getUserIdFromToken(token);
            Integer status = request.get("status");

            if (status == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "code", 400,
                        "msg", "状态不能为空"
                ));
            }

            // 检查订单是否存在且用户有权操作
            Order order = orderService.getOrderById(orderId);
            if (order == null) {
                return ResponseEntity.badRequest().body(Map.of(
                        "code", 400,
                        "msg", "订单不存在"
                ));
            }

            // 只有订单的创建者或演唱者可以更新状态
            if (!order.getUserId().equals(userId) && !order.getSingerId().equals(userId)) {
                return ResponseEntity.badRequest().body(Map.of(
                        "code", 400,
                        "msg", "无权操作此订单"
                ));
            }

            boolean result = orderService.updateOrderStatus(orderId, status);
            if (result) {
                return ResponseEntity.ok(Map.of(
                        "code", 200,
                        "msg", "订单状态更新成功"
                ));
            } else {
                return ResponseEntity.badRequest().body(Map.of(
                        "code", 400,
                        "msg", "订单状态更新失败"
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "code", 400,
                    "msg", "订单状态更新失败: " + e.getMessage()
            ));
        }
    }

    // 从token中获取用户ID的方法
    private Long getUserIdFromToken(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("无效的认证信息");
        }

        String token = authHeader.substring(7);
        if (!jwtUtils.validateToken(token)) {
            throw new RuntimeException("Token验证失败");
        }

        return jwtUtils.getUserIdFromToken(token);
    }
}