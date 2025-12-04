// service/impl/AdminServiceImpl.java
package com.example.liyuan.service.impl;

import com.example.liyuan.entity.User;
import com.example.liyuan.entity.UserAudio;
import com.example.liyuan.mapper.AdminOrderMapper;
import com.example.liyuan.mapper.UserMapper;
import com.example.liyuan.mapper.UserAudioMapper;
import com.example.liyuan.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.liyuan.dto.OrderDetailDTO;
import com.example.liyuan.dto.OrderGroupByDateDTO;
import java.util.LinkedHashMap;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.example.liyuan.util.WechatUtils.logger;

@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserAudioMapper userAudioMapper;

    @Autowired
    private AdminOrderMapper adminOrderMapper;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public Map<String, Object> getUsers(int page, int size, String keyword, Integer userType) {
        logger.info("获取用户列表 - page: {}, size: {}, keyword: {}, userType: {}",
                page, size, keyword, userType);

        // 计算分页
        int offset = (page - 1) * size;

        // 获取所有符合条件的用户
        List<User> allUsers = userMapper.searchUsers(keyword, userType);

        logger.info("数据库查询结果: {} 条记录", allUsers.size());
        for (User user : allUsers) {
            logger.debug("用户ID: {}, 昵称: {}, 用户类型: {}",
                    user.getId(), user.getNickname(), user.getUserType());
        }

        // 手动分页
        int total = allUsers.size();
        int totalPages = (int) Math.ceil((double) total / size);

        // 截取当前页的数据
        List<User> pageData = new ArrayList<>();
        int start = offset;
        int end = Math.min(start + size, total);

        if (start < total) {
            pageData = allUsers.subList(start, end);
        }

        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("list", pageData);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        result.put("pages", totalPages);

        logger.info("返回结果: 总记录数={}, 当前页记录数={}, 总页数={}",
                total, pageData.size(), totalPages);

        return result;
    }

    @Override
    @Transactional
    public boolean updateUserRole(Long userId, Integer userType) {
        logger.info("修改用户身份 - userId: {}, userType: {}", userId, userType);

        User user = userMapper.selectById(userId);
        if (user == null) {
            logger.warn("用户不存在 - userId: {}", userId);
            return false;
        }

        // 只更新用户类型
        user.setUserType(userType);
        user.setUpdateTime(LocalDateTime.now());

        int rows = userMapper.updateUserType(user);
        logger.info("更新用户类型结果: {} 行受影响", rows);

        return rows > 0;
    }

    @Override
    @Transactional
    public int batchUpdateUserRole(List<Long> userIds, Integer userType) {
        int successCount = 0;

        for (Long userId : userIds) {
            User user = userMapper.selectById(userId);
            if (user != null) {
                user.setUserType(userType);
                user.setUpdateTime(LocalDateTime.now());

                if (userMapper.updateUserType(user) > 0) {
                    successCount++;
                }
            }
        }

        return successCount;
    }

    @Override
    public Map<String, Object> getUserStats() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("total", userMapper.countAllUsers());
        stats.put("normal", userMapper.countUsersByType(0));
        stats.put("singer", userMapper.countUsersByType(1));
        stats.put("boss", userMapper.countUsersByType(2));

        return stats;
    }

    // =========== 语音审核相关方法实现 ===========

    @Override
    public Map<String, Object> getSingerAudios(int page, int size, String keyword) {
        // 获取所有歌手（用户类型为1）
        List<User> singers = getSingersByType(1);

        if (keyword != null && !keyword.trim().isEmpty()) {
            // 如果有搜索关键词，筛选包含该关键词的歌手
            String lowerKeyword = keyword.toLowerCase().trim();
            singers = singers.stream()
                    .filter(singer -> {
                        String nickname = singer.getNickname() != null ? singer.getNickname().toLowerCase() : "";
                        String customNickname = singer.getCustomNickname() != null ? singer.getCustomNickname().toLowerCase() : "";
                        return nickname.contains(lowerKeyword) || customNickname.contains(lowerKeyword);
                    })
                    .collect(Collectors.toList());
        }

        // 获取每个歌手的音频
        List<Map<String, Object>> singerAudioList = new ArrayList<>();

        for (User singer : singers) {
            // 获取该歌手的音频
            List<UserAudio> audios = userAudioMapper.findByUserId(singer.getId());

            if (!audios.isEmpty()) {
                // 歌手信息
                Map<String, Object> singerInfo = new HashMap<>();
                singerInfo.put("id", singer.getId());
                singerInfo.put("name", getDisplayName(singer));
                singerInfo.put("avatar", singer.getAvatar());
                singerInfo.put("userType", singer.getUserType());
                singerInfo.put("points", singer.getPoints());

                // 音频信息
                List<Map<String, Object>> audioList = new ArrayList<>();
                for (UserAudio audio : audios) {
                    Map<String, Object> audioInfo = new HashMap<>();
                    audioInfo.put("id", audio.getId());
                    audioInfo.put("name", audio.getAudioName());
                    audioInfo.put("path", audio.getAudioLocalPath());
                    audioInfo.put("duration", audio.getAudioDuration());
                    audioInfo.put("createdAt", audio.getCreatedAt() != null ?
                            audio.getCreatedAt().format(DATE_FORMATTER) : null);
                    audioInfo.put("singerId", singer.getId());
                    audioInfo.put("singerName", singerInfo.get("name"));
                    audioInfo.put("singerAvatar", singerInfo.get("avatar"));
                    audioInfo.put("isPlaying", false);

                    audioList.add(audioInfo);
                }

                Map<String, Object> singerAudio = new HashMap<>();
                singerAudio.put("singer", singerInfo);
                singerAudio.put("audios", audioList);
                singerAudio.put("audioCount", audioList.size());

                singerAudioList.add(singerAudio);
            }
        }

        // 手动分页
        int total = singerAudioList.size();
        int totalPages = (int) Math.ceil((double) total / size);
        int offset = (page - 1) * size;

        List<Map<String, Object>> pageData = new ArrayList<>();
        int start = offset;
        int end = Math.min(start + size, total);

        if (start < total) {
            pageData = singerAudioList.subList(start, end);
        }

        // 构建返回结果
        Map<String, Object> result = new HashMap<>();
        result.put("list", pageData);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        result.put("pages", totalPages);

        return result;
    }

    @Override
    @Transactional
    public boolean deleteAudio(Long audioId) {
        // 由于 UserAudioMapper 目前只有 deleteByIdAndUserId 方法，我们需要创建 deleteById 方法
        // 这里假设已经在 UserAudioMapper 中添加了 deleteById 方法
        int rows = userAudioMapper.deleteById(audioId);
        return rows > 0;
    }

    @Override
    public Map<String, Object> getAudioStats() {
        // 获取所有歌手
        List<User> singers = getSingersByType(1);
        int totalSingers = singers.size();

        // 统计有音频的歌手数量
        int singersWithAudio = 0;
        int totalAudios = 0;
        int totalAudioDuration = 0;

        for (User singer : singers) {
            List<UserAudio> audios = userAudioMapper.findByUserId(singer.getId());
            if (!audios.isEmpty()) {
                singersWithAudio++;
                totalAudios += audios.size();

                // 计算总时长
                for (UserAudio audio : audios) {
                    if (audio.getAudioDuration() != null) {
                        totalAudioDuration += audio.getAudioDuration();
                    }
                }
            }
        }

        // 计算平均音频数量
        double avgAudiosPerSinger = totalSingers > 0 ? (double) totalAudios / totalSingers : 0;

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalSingers", totalSingers);
        stats.put("singersWithAudio", singersWithAudio);
        stats.put("totalAudios", totalAudios);
        stats.put("totalAudioDuration", totalAudioDuration);
        stats.put("avgAudiosPerSinger", String.format("%.1f", avgAudiosPerSinger));

        return stats;
    }

    // =========== 辅助方法 ===========

    /**
     * 获取歌手列表
     * 注意：这里使用了现有的 searchUsers 方法，传入 userType=1 来获取歌手
     */
    private List<User> getSingersByType(Integer userType) {
        return userMapper.searchUsers(null, userType);
    }

    /**
     * 获取显示名称（优先使用自定义昵称）
     */
    private String getDisplayName(User user) {
        if (user.getCustomNickname() != null && !user.getCustomNickname().isEmpty()) {
            return user.getCustomNickname();
        }
        return user.getNickname();
    }
    @Override
    public Map<String, Object> getOrderStats() {
        try {
            // 获取订单总数
            Integer totalOrders = adminOrderMapper.countAllOrders();

            // 获取总金额 - 确保不为null
            BigDecimal totalAmount = adminOrderMapper.sumAllAmount();
            totalAmount = totalAmount != null ? totalAmount : BigDecimal.ZERO;

            // 获取今日订单数和今日金额
            LocalDate today = LocalDate.now();
            Integer todayOrders = adminOrderMapper.countOrdersByDate(today.toString());
            BigDecimal todayAmount = adminOrderMapper.sumAmountByDate(today.toString());
            todayAmount = todayAmount != null ? todayAmount : BigDecimal.ZERO;

            // 获取不同状态的订单数
            Map<Integer, Integer> statusCounts = new HashMap<>();
            List<Map<String, Object>> statusStats = adminOrderMapper.countOrdersByStatus();
            for (Map<String, Object> stat : statusStats) {
                Integer status = (Integer) stat.get("status");
                Long count = (Long) stat.get("count");
                statusCounts.put(status, count.intValue());
            }

            // 获取支付方式统计
            Map<String, Integer> paymentTypeCounts = new HashMap<>();
            List<Map<String, Object>> paymentStats = adminOrderMapper.countOrdersByPaymentType();
            for (Map<String, Object> stat : paymentStats) {
                String paymentType = (String) stat.get("payment_type");
                Long count = (Long) stat.get("count");
                paymentTypeCounts.put(paymentType != null ? paymentType : "unknown", count.intValue());
            }

            // 确保金额以字符串形式返回，避免序列化问题
            return Map.of(
                    "totalOrders", totalOrders,
                    "totalAmount", totalAmount.toString(), // 转换为字符串
                    "todayOrders", todayOrders,
                    "todayAmount", todayAmount.toString(), // 转换为字符串
                    "statusCounts", statusCounts,
                    "paymentTypeCounts", paymentTypeCounts
            );
        } catch (Exception e) {
            throw new RuntimeException("获取订单统计信息失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getOrders(int page, int size, String startDate, String endDate,
                                         String keyword, Integer status) {
        try {
            // 计算偏移量
            int offset = (page - 1) * size;

            // 构建查询参数
            Map<String, Object> params = new HashMap<>();
            params.put("offset", offset);
            params.put("size", size);
            params.put("keyword", keyword);
            params.put("status", status);
            params.put("startDate", startDate);
            params.put("endDate", endDate);

            // 获取订单列表
            List<OrderDetailDTO> orders = adminOrderMapper.selectOrdersWithDetails(params);

            // 按日期分组
            Map<String, List<OrderDetailDTO>> ordersByDate = new LinkedHashMap<>();

            for (OrderDetailDTO order : orders) {
                // 确定分组日期（优先使用完成时间）
                LocalDate date = null;
                if (order.getCompleteTime() != null) {
                    date = order.getCompleteTime().toLocalDate();
                } else if (order.getCreateTime() != null) {
                    date = order.getCreateTime().toLocalDate();
                } else {
                    date = LocalDate.now(); // 默认使用今天
                }

                String dateKey = date.toString();
                ordersByDate.computeIfAbsent(dateKey, k -> new ArrayList<>()).add(order);
            }

            // 构建按日期分组的数据
            List<Map<String, Object>> groupedOrders = new ArrayList<>();

            for (Map.Entry<String, List<OrderDetailDTO>> entry : ordersByDate.entrySet()) {
                String dateKey = entry.getKey();
                List<OrderDetailDTO> dailyOrders = entry.getValue();

                // 计算当日总收入
                BigDecimal dailyIncome = BigDecimal.ZERO;
                for (OrderDetailDTO order : dailyOrders) {
                    if (order.getAmount() != null) {
                        dailyIncome = dailyIncome.add(order.getAmount());
                    }
                }

                // 添加状态文本
                for (OrderDetailDTO order : dailyOrders) {
                    String statusText = getStatusText(order.getStatus());
                    order.setStatusText(statusText);

                    // 确保金额不为null
                    if (order.getAmount() == null) {
                        order.setAmount(BigDecimal.ZERO);
                    }
                }

                // 创建分组数据
                Map<String, Object> group = new HashMap<>();
                group.put("date", dateKey);
                group.put("dailyIncome", dailyIncome.toString()); // 转换为字符串
                group.put("orderCount", dailyOrders.size());
                group.put("orders", dailyOrders);

                groupedOrders.add(group);
            }

            // 按日期降序排序
            groupedOrders.sort((a, b) -> ((String) b.get("date")).compareTo((String) a.get("date")));

            // 获取总记录数（用于分页）
            Integer totalCount = adminOrderMapper.countOrders(params);

            return Map.of(
                    "total", totalCount,
                    "page", page,
                    "size", size,
                    "ordersGroupedByDate", groupedOrders
            );
        } catch (Exception e) {
            throw new RuntimeException("获取订单列表失败: " + e.getMessage());
        }
    }

    @Override
    public boolean updateOrderStatus(Long orderId, Integer status) {
        try {
            // 验证状态值
            if (status < 0 || status > 3) {
                throw new IllegalArgumentException("无效的订单状态");
            }

            // 获取订单信息
            Map<String, Object> orderInfo = adminOrderMapper.selectOrderById(orderId);
            if (orderInfo == null) {
                throw new RuntimeException("订单不存在");
            }

            // 更新订单状态
            int result = adminOrderMapper.updateOrderStatus(orderId, status);

            // 如果状态更新为已完成，设置完成时间
            if (status == 2 && result > 0) {
                adminOrderMapper.updateOrderCompleteTime(orderId, LocalDateTime.now());
            }

            return result > 0;
        } catch (Exception e) {
            throw new RuntimeException("更新订单状态失败: " + e.getMessage());
        }
    }

    /**
     * 获取订单状态文本
     */
    private String getStatusText(Integer status) {
        if (status == null) return "未知";

        switch (status) {
            case 0: return "待演唱";
            case 1: return "演唱中";
            case 2: return "已完成";
            case 3: return "已取消";
            default: return "未知";
        }
    }
}