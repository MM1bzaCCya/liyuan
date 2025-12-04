// src/main/java/com/example/liyuan/mapper/AdminOrderMapper.java
package com.example.liyuan.mapper;

import com.example.liyuan.dto.OrderDetailDTO;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface AdminOrderMapper {

    /**
     * 获取订单总数
     */
    @Select("SELECT COUNT(*) FROM orders")
    Integer countAllOrders();

    /**
     * 获取总金额
     */
    @Select("SELECT SUM(amount) FROM orders WHERE amount IS NOT NULL")
    BigDecimal sumAllAmount();

    /**
     * 获取指定日期的订单数
     */
    @Select("SELECT COUNT(*) FROM orders WHERE DATE(complete_time) = #{date} OR " +
            "(complete_time IS NULL AND DATE(create_time) = #{date})")
    Integer countOrdersByDate(String date);

    /**
     * 获取指定日期的总金额
     */
    @Select("SELECT SUM(amount) FROM orders WHERE " +
            "(complete_time IS NOT NULL AND DATE(complete_time) = #{date}) OR " +
            "(complete_time IS NULL AND DATE(create_time) = #{date})")
    BigDecimal sumAmountByDate(String date);

    /**
     * 按状态统计订单数
     */
    @Select("SELECT status, COUNT(*) as count FROM orders GROUP BY status")
    List<Map<String, Object>> countOrdersByStatus();

    /**
     * 按支付方式统计订单数
     */
    @Select("SELECT payment_type, COUNT(*) as count FROM orders GROUP BY payment_type")
    List<Map<String, Object>> countOrdersByPaymentType();

    /**
     * 获取订单列表（带详细信息）
     */
    @Select({
            "<script>",
            "SELECT o.*,",
            "       u1.nickname as userNickname,",
            "       u1.avatar as userAvatar,",
            "       u2.nickname as singerNickname,",
            "       u2.avatar as singerAvatar",
            "FROM orders o",
            "LEFT JOIN users u1 ON o.user_id = u1.id",
            "LEFT JOIN users u2 ON o.singer_id = u2.id",
            "WHERE 1=1",
            "<if test='keyword != null and keyword != \"\"'>",
            "  AND (u1.nickname LIKE CONCAT('%', #{keyword}, '%')",
            "       OR u2.nickname LIKE CONCAT('%', #{keyword}, '%')",
            "       OR o.song_name LIKE CONCAT('%', #{keyword}, '%')",
            "       OR o.original_singer LIKE CONCAT('%', #{keyword}, '%'))",
            "</if>",
            "<if test='status != null'>",
            "  AND o.status = #{status}",
            "</if>",
            "<if test='startDate != null'>",
            "  AND DATE(o.complete_time) &gt;= #{startDate}",
            "</if>",
            "<if test='endDate != null'>",
            "  AND DATE(o.complete_time) &lt;= #{endDate}",
            "</if>",
            "ORDER BY",
            "  CASE WHEN o.complete_time IS NOT NULL THEN o.complete_time ELSE o.create_time END DESC",
            "LIMIT #{offset}, #{size}",
            "</script>"
    })
    List<OrderDetailDTO> selectOrdersWithDetails(Map<String, Object> params);

    /**
     * 获取订单总数（带条件）
     */
    @Select({
            "<script>",
            "SELECT COUNT(*) FROM orders o",
            "LEFT JOIN users u1 ON o.user_id = u1.id",
            "LEFT JOIN users u2 ON o.singer_id = u2.id",
            "WHERE 1=1",
            "<if test='keyword != null and keyword != \"\"'>",
            "  AND (u1.nickname LIKE CONCAT('%', #{keyword}, '%')",
            "       OR u2.nickname LIKE CONCAT('%', #{keyword}, '%')",
            "       OR o.song_name LIKE CONCAT('%', #{keyword}, '%')",
            "       OR o.original_singer LIKE CONCAT('%', #{keyword}, '%'))",
            "</if>",
            "<if test='status != null'>",
            "  AND o.status = #{status}",
            "</if>",
            "<if test='startDate != null'>",
            "  AND DATE(o.complete_time) &gt;= #{startDate}",
            "</if>",
            "<if test='endDate != null'>",
            "  AND DATE(o.complete_time) &lt;= #{endDate}",
            "</if>",
            "</script>"
    })
    Integer countOrders(Map<String, Object> params);

    /**
     * 获取订单信息
     */
    @Select("SELECT * FROM orders WHERE id = #{orderId}")
    Map<String, Object> selectOrderById(@Param("orderId") Long orderId);

    /**
     * 更新订单状态
     */
    @Update("UPDATE orders SET status = #{status}, update_time = NOW() WHERE id = #{orderId}")
    int updateOrderStatus(@Param("orderId") Long orderId, @Param("status") Integer status);

    /**
     * 更新订单完成时间
     */
    @Update("UPDATE orders SET complete_time = #{completeTime}, update_time = NOW() WHERE id = #{orderId}")
    int updateOrderCompleteTime(@Param("orderId") Long orderId, @Param("completeTime") LocalDateTime completeTime);
}