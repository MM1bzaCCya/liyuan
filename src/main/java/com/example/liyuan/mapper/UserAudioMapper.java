package com.example.liyuan.mapper;

import com.example.liyuan.entity.User;
import com.example.liyuan.entity.UserAudio;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface UserAudioMapper {
    @Insert("INSERT INTO user_audio(user_id, audio_name, audio_local_path, " +
            "audio_duration) VALUES(#{userId}, #{audioName}, " +
            "#{audioLocalPath}, #{audioDuration})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(UserAudio userAudio);

    @Select("SELECT * FROM user_audio WHERE user_id = #{userId} " +
            "ORDER BY created_at DESC")
    List<UserAudio> findByUserId(Long userId);

    @Delete("DELETE FROM user_audio WHERE id = #{id} AND user_id = #{userId}")
    int deleteByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
    @Delete("DELETE FROM user_audio WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    // 获取所有音频（用于统计）
    @Select("SELECT COUNT(*) FROM user_audio")
    int countAll();

    // 根据歌手ID获取音频数量
    @Select("SELECT COUNT(*) FROM user_audio WHERE user_id = #{userId}")
    int countByUserId(@Param("userId") Long userId);
    @Select("<script>" +
            "SELECT * FROM users WHERE 1=1 " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "   AND (nickname LIKE CONCAT('%', #{keyword}, '%') " +
            "   OR custom_nickname LIKE CONCAT('%', #{keyword}, '%') " +
            "   OR id = #{keyword})" +
            "</if>" +
            "<if test='userType != null'>" +
            "   AND user_type = #{userType}" +
            "</if>" +
            "ORDER BY create_time DESC" +
            "</script>")
    List<User> searchUsers(@Param("keyword") String keyword, @Param("userType") Integer userType);
}