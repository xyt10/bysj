package com.yanxue.dto;

import lombok.Data;

/**
 * 登录请求DTO
 */
@Data
public class LoginRequest {
    /**
     * 用户名或手机号
     */
    private String account;

    /**
     * 密码
     */
    private String password;
}
