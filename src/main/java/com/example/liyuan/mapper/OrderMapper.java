package com.example.liyuan.mapper;

import com.example.liyuan.dto.HotSingerDTO;
import com.example.liyuan.dto.HotSongDTO;
import com.example.liyuan.entity.Order;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface OrderMapper {
    @Insert("INSERT INTO orders (request_id, grab_id, user_id, singer_id, song_name, original_singer, status, amount, points, payment_type, queue_number, sing_time, complete_time, create_time, update_time) " +
            "VALUES (#{requestId}, #{grabId}, #{userId}, #{singerId}, #{songName}, #{originalSinger}, #{status}, #{amount}, #{points}, #{paymentType}, #{queueNumber}, #{singTime}, #{completeTime}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Order order);

    @Update("UPDATE orders SET status=#{status}, amount=#{amount}, points=#{points}, payment_type=#{paymentType}, queue_number=#{queueNumber}, sing_time=#{singTime}, complete_time=#{completeTime}, update_time=#{updateTime} WHERE id=#{id}")
    int update(Order order);

    @Select("SELECT * FROM orders WHERE id = #{id}")
    Order selectById(Long id);

    @Select("SELECT o.*, u1.nickname as requesterName, u1.avatar as requesterAvatar, " +
            "u2.nickname as singerName, u2.avatar as singerAvatar " +
            "FROM orders o " +
            "LEFT JOIN users u1 ON o.user_id = u1.id " +
            "LEFT JOIN users u2 ON o.singer_id = u2.id " +
            "WHERE o.user_id = #{userId} ORDER BY o.create_time DESC")
    List<Order> selectByUserIdWithInfo(Long userId);

    @Select("SELECT o.*, u1.nickname as requesterName, u1.avatar as requesterAvatar, " +
            "u2.nickname as singerName, u2.avatar as singerAvatar " +
            "FROM orders o " +
            "LEFT JOIN users u1 ON o.user_id = u1.id " +
            "LEFT JOIN users u2 ON o.singer_id = u2.id " +
            "WHERE o.singer_id = #{singerId} ORDER BY o.create_time DESC")
    List<Order> selectBySingerIdWithInfo(Long singerId);

    @Select("SELECT o.*, u1.nickname as requesterName, u1.avatar as requesterAvatar, " +
            "u2.nickname as singerName, u2.avatar as singerAvatar " +
            "FROM orders o " +
            "LEFT JOIN users u1 ON o.user_id = u1.id " +
            "LEFT JOIN users u2 ON o.singer_id = u2.id " +
            "ORDER BY o.queue_number")
    List<Order> selectAllWithInfo();

    @Select("SELECT MAX(queue_number) FROM orders")
    Integer selectMaxQueueNumber();

    @Select("SELECT song_name as songName, original_singer as artist, COUNT(*) as orderCount " +
            "FROM orders " +
            "WHERE song_name IS NOT NULL AND TRIM(song_name) != '' " +
            "GROUP BY song_name, original_singer " +
            "ORDER BY orderCount DESC " +
            "LIMIT 10")
    List<HotSongDTO> findHotSongs();

    @Select("SELECT o.singer_id as singerId, " +
            "u.nickname as singerName, " +
            "u.avatar as singerAvatar, " +
            "COUNT(*) as orderCount " +
            "FROM orders o " +
            "JOIN users u ON o.singer_id = u.id " +
            "WHERE o.singer_id IS NOT NULL AND o.singer_id > 0 " +
            "AND u.user_type = 1 " +  // 只统计歌手身份的用户
            "GROUP BY o.singer_id, u.nickname, u.avatar " +
            "ORDER BY orderCount DESC " +
            "LIMIT 10")
    List<HotSingerDTO> findHotSingers();

}