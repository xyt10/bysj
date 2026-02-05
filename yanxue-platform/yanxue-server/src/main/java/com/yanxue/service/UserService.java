package com.yanxue.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yanxue.dto.RegisterRequest;
import com.yanxue.entity.User;
import com.yanxue.mapper.UserMapper;
import com.yanxue.util.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 用户服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @Value("${wechat.miniprogram.app-id}")
    private String appId;

    @Value("${wechat.miniprogram.app-secret}")
    private String appSecret;

    /**
     * 微信小程序登录
     */
    public String wxLogin(String code) {
        // 调用微信接口获取openid
        String url = String.format(
            "https://api.weixin.qq.com/sns/jscode2session?appid=%s&secret=%s&js_code=%s&grant_type=authorization_code",
            appId, appSecret, code
        );

        try {
            HttpResponse response = HttpRequest.get(url).execute();
            JSONObject result = JSON.parseObject(response.body());

            String openid = result.getString("openid");
            if (StrUtil.isBlank(openid)) {
                log.error("微信登录失败: {}", result);
                return null;
            }

            // 查找或创建用户
            User user = getByOpenid(openid);
            if (user == null) {
                user = new User();
                user.setOpenid(openid);
                user.setNickname("用户" + openid.substring(0, 6));
                userMapper.insert(user);
            }

            // 生成JWT
            return jwtUtil.generateToken(user.getId());

        } catch (Exception e) {
            log.error("微信登录异常", e);
            return null;
        }
    }

    /**
     * 模拟登录（开发测试用）
     */
    public String mockLogin(String mockOpenid) {
        User user = getByOpenid(mockOpenid);
        if (user == null) {
            user = new User();
            user.setOpenid(mockOpenid);
            user.setNickname("测试用户");
            userMapper.insert(user);
        }
        return jwtUtil.generateToken(user.getId());
    }

    /**
     * 根据openid获取用户
     */
    public User getByOpenid(String openid) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getOpenid, openid);
        return userMapper.selectOne(wrapper);
    }

    /**
     * 根据ID获取用户
     */
    public User getById(Long id) {
        return userMapper.selectById(id);
    }

    /**
     * 更新用户信息
     */
    public User update(User user) {
        userMapper.updateById(user);
        return user;
    }

    /**
     * 更新用户画像
     */
    public User updateProfile(Long userId, String grade, String school, String interests) {
        User user = userMapper.selectById(userId);
        if (user != null) {
            user.setGrade(grade);
            user.setSchool(school);
            user.setInterests(interests);
            userMapper.updateById(user);
        }
        return user;
    }

    /**
     * 用户名/手机号+密码登录
     */
    public String login(String account, String password) {
        // 先按用户名查找
        User user = getByUsername(account);
        if (user == null) {
            // 再按手机号查找
            user = getByPhone(account);
        }

        if (user == null || user.getPassword() == null) {
            return null;
        }

        // 验证密码
        if (!passwordEncoder.matches(password, user.getPassword())) {
            return null;
        }

        return jwtUtil.generateToken(user.getId());
    }

    /**
     * 用户注册
     */
    public String register(RegisterRequest request) {
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname() != null ? request.getNickname() : request.getUsername());
        user.setPhone(request.getPhone());
        user.setLoginType("password");
        userMapper.insert(user);

        return jwtUtil.generateToken(user.getId());
    }

    /**
     * 根据用户名获取用户
     */
    public User getByUsername(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        return userMapper.selectOne(wrapper);
    }

    /**
     * 根据手机号获取用户
     */
    public User getByPhone(String phone) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, phone);
        return userMapper.selectOne(wrapper);
    }

    /**
     * 检查用户名是否存在
     */
    public boolean existsByUsername(String username) {
        return getByUsername(username) != null;
    }

    /**
     * 检查手机号是否存在
     */
    public boolean existsByPhone(String phone) {
        return getByPhone(phone) != null;
    }

    /**
     * 分页查询用户
     */
    public IPage<User> page(Integer pageNum, Integer pageSize, String keyword, String grade) {
        Page<User> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.and(w -> w
                .like(User::getNickname, keyword)
                .or()
                .like(User::getUsername, keyword)
                .or()
                .like(User::getPhone, keyword)
            );
        }
        
        if (StrUtil.isNotBlank(grade)) {
            wrapper.eq(User::getGrade, grade);
        }
        
        wrapper.orderByDesc(User::getCreatedAt);
        return userMapper.selectPage(page, wrapper);
    }
}
