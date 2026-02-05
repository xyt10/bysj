package com.yanxue.controller;

import com.yanxue.dto.LoginRequest;
import com.yanxue.dto.RegisterRequest;
import com.yanxue.entity.User;
import com.yanxue.service.UserService;
import com.yanxue.util.JwtUtil;
import com.yanxue.vo.Result;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    /**
     * 分页查询用户列表（管理后台）
     */
    @GetMapping("/page")
    public Result<IPage<User>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String grade) {
        IPage<User> page = userService.page(pageNum, pageSize, keyword, grade);
        // 隐藏敏感信息
        page.getRecords().forEach(user -> {
            user.setPassword(null);
            user.setOpenid(null);
        });
        return Result.success(page);
    }

    /**
     * 微信登录
     */
    @PostMapping("/wx-login")
    public Result<Map<String, Object>> wxLogin(@RequestBody WxLoginRequest request) {
        String token = userService.wxLogin(request.getCode());
        if (token == null) {
            return Result.error("微信登录失败");
        }

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        return Result.success(data);
    }

    /**
     * 模拟登录（开发测试用）
     */
    @PostMapping("/mock-login")
    public Result<Map<String, Object>> mockLogin(@RequestBody MockLoginRequest request) {
        String token = userService.mockLogin(request.getMockOpenid());

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        return Result.success(data);
    }

    /**
     * Web端登录（用户名/手机号+密码）
     */
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody LoginRequest request) {
        String token = userService.login(request.getAccount(), request.getPassword());
        if (token == null) {
            return Result.error("用户名或密码错误");
        }

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        return Result.success(data);
    }

    /**
     * Web端注册
     */
    @PostMapping("/register")
    public Result<Map<String, Object>> register(@RequestBody RegisterRequest request) {
        // 检查用户名是否已存在
        if (userService.existsByUsername(request.getUsername())) {
            return Result.error("用户名已被注册");
        }

        // 检查手机号是否已存在
        if (request.getPhone() != null && userService.existsByPhone(request.getPhone())) {
            return Result.error("手机号已被注册");
        }

        String token = userService.register(request);
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        return Result.success("注册成功", data);
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/current")
    public Result<User> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        Long userId = jwtUtil.getUserIdFromToken(token);
        User user = userService.getById(userId);
        return Result.success(user);
    }

    /**
     * 获取用户信息
     */
    @GetMapping("/{id}")
    public Result<User> getById(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        return Result.success(user);
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/{id}")
    public Result<User> update(@PathVariable Long id, @RequestBody User user) {
        user.setId(id);
        return Result.success(userService.update(user));
    }

    /**
     * 更新用户画像
     */
    @PostMapping("/{id}/profile")
    public Result<User> updateProfile(
            @PathVariable Long id,
            @RequestBody UpdateProfileRequest request) {
        User user = userService.updateProfile(id, request.getGrade(), request.getSchool(), request.getInterests());
        return Result.success(user);
    }

    @Data
    public static class WxLoginRequest {
        private String code;
    }

    @Data
    public static class MockLoginRequest {
        private String mockOpenid;
    }

    @Data
    public static class UpdateProfileRequest {
        private String grade;
        private String school;
        private String interests;
    }
}
