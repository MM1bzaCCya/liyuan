package com.example.liyuan.mapper;

import com.example.liyuan.entity.User;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserMapper {

    @Insert("INSERT INTO users (wechat_openid, wechat_unionid, nickname, avatar, gender, country, province, city, user_type, points, create_time, update_time) " +
            "VALUES (#{wechatOpenid}, #{wechatUnionid}, #{nickname}, #{avatar}, #{gender}, #{country}, #{province}, #{city}, #{userType}, #{points}, #{createTime}, #{updateTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(User user);

    @Update("UPDATE users SET wechat_unionid=#{wechatUnionid}, nickname=#{nickname}, avatar=#{avatar}, " +
            "gender=#{gender}, country=#{country}, province=#{province}, city=#{city}, " +
            "user_type=#{userType}, points=#{points}, update_time=#{updateTime} WHERE id=#{id}")
    int update(User user);

    @Select("SELECT * FROM users WHERE id = #{id}")
    User selectById(Long id);

    @Select("SELECT * FROM users WHERE wechat_openid = #{openid}")
    User selectByWechatOpenid(String openid);

    @Select("SELECT * FROM users WHERE wechat_unionid = #{unionid}")
    User selectByWechatUnionid(String unionid);

    @Delete("DELETE FROM users WHERE id = #{id}")
    int deleteById(Long id);

    @Update("UPDATE users SET user_type=#{userType}, update_time=#{updateTime} WHERE id=#{id}")
    int updateUserType(User user);
}