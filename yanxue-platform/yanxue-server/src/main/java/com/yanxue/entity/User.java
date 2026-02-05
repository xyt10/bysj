package com.yanxue.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户实体类
 */
@Data
@TableName("user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 微信openid
     */
    private String openid;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码(BCrypt加密)
     */
    @JsonIgnore
    private String password;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 登录方式: password/wechat/phone
     */
    private String loginType;

    /**
     * 学段：小学/初中/高中
     */
    private String grade;

    /**
     * 学校
     */
    private String school;

    /**
     * 兴趣标签JSON数组
     */
    private String interests;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
