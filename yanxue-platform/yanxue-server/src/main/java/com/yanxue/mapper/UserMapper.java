package com.yanxue.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yanxue.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户Mapper
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
