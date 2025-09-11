package com.example.liyuan.mapper;

import com.example.liyuan.entity.SongGrab;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface SongGrabMapper {
    @Insert("INSERT INTO song_grabs (request_id, singer_id, status, create_time, update_time) " +
            "VALUES (#{requestId}, #{singerId}, #{status}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(SongGrab songGrab);

    @Update("UPDATE song_grabs SET status=#{status}, update_time=#{updateTime} WHERE id=#{id}")
    int updateStatus(SongGrab songGrab);

    @Select("SELECT * FROM song_grabs WHERE id = #{id}")
    SongGrab selectById(Long id);

    @Select("SELECT sg.*, u.nickname as singerName, u.avatar as singerAvatar, " +
            "sr.song_name as songName, sr.original_singer as originalSinger " +
            "FROM song_grabs sg " +
            "LEFT JOIN users u ON sg.singer_id = u.id " +
            "LEFT JOIN song_requests sr ON sg.request_id = sr.id " +
            "WHERE sg.request_id = #{requestId}")
    List<SongGrab> selectByRequestIdWithInfo(Long requestId);

    @Select("SELECT sg.*, u.nickname as singerName, u.avatar as singerAvatar, " +
            "sr.song_name as songName, sr.original_singer as originalSinger " +
            "FROM song_grabs sg " +
            "LEFT JOIN users u ON sg.singer_id = u.id " +
            "LEFT JOIN song_requests sr ON sg.request_id = sr.id " +
            "WHERE sg.singer_id = #{singerId}")
    List<SongGrab> selectBySingerIdWithInfo(Long singerId);

    @Select("SELECT COUNT(*) FROM song_grabs WHERE request_id = #{requestId} AND singer_id = #{singerId}")
    int countByRequestAndSinger(@Param("requestId") Long requestId, @Param("singerId") Long singerId);
}