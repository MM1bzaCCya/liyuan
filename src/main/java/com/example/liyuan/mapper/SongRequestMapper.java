package com.example.liyuan.mapper;

import com.example.liyuan.entity.SongRequest;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface SongRequestMapper {

    @Insert("INSERT INTO song_requests (user_id, song_name, original_singer, status, create_time, update_time) " +
            "VALUES (#{userId}, #{songName}, #{originalSinger}, #{status}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SongRequest songRequest);

    @Update("UPDATE song_requests SET status=#{status}, update_time=#{updateTime} WHERE id=#{id}")
    int updateStatus(SongRequest songRequest);

    @Select("SELECT * FROM song_requests WHERE id = #{id}")
    SongRequest selectById(Long id);

    @Select("SELECT * FROM song_requests WHERE user_id = #{userId} ORDER BY create_time DESC")
    List<SongRequest> selectByUserId(Long userId);

    @Select("SELECT * FROM song_requests WHERE status = #{status} ORDER BY create_time DESC")
    List<SongRequest> selectByStatus(Integer status);

    @Select("SELECT sr.*, u.nickname as requesterName, u.avatar as requesterAvatar " +
            "FROM song_requests sr " +
            "LEFT JOIN users u ON sr.user_id = u.id " +
            "WHERE sr.status = 0 ORDER BY sr.create_time DESC")
    List<SongRequest> selectAvailableRequestsWithUserInfo();

}