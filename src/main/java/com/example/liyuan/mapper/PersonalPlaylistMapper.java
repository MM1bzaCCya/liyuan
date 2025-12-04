package com.example.liyuan.mapper;

import com.example.liyuan.entity.PersonalPlaylist;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface PersonalPlaylistMapper {
    @Insert("INSERT INTO personal_playlist(user_id, song_name, artist, " +
            "is_favorite) VALUES(#{userId}, #{songName}, #{artist}, " +
            "#{isFavorite})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(PersonalPlaylist playlist);

    @Select("SELECT * FROM personal_playlist WHERE user_id = #{userId} " +
            "ORDER BY add_time DESC")
    List<PersonalPlaylist> findByUserId(Long userId);

    @Delete("DELETE FROM personal_playlist WHERE id = #{id} AND user_id = #{userId}")
    int deleteByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    @Select("SELECT * FROM personal_playlist WHERE user_id = #{userId} " +
            "AND is_favorite = true ORDER BY add_time DESC")
    List<PersonalPlaylist> findFavoritesByUserId(Long userId);

}