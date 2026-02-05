package com.yanxue.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AI调用日志实体类
 */
@Data
@TableName("ai_log")
public class AILog {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 类型：recommend/generate
     */
    private String type;

    /**
     * 使用的模型
     */
    private String model;

    /**
     * 请求prompt
     */
    private String prompt;

    /**
     * 响应内容
     */
    private String response;

    /**
     * 使用的token数
     */
    private Integer tokensUsed;

    /**
     * 费用
     */
    private BigDecimal cost;

    /**
     * 耗时毫秒
     */
    private Integer durationMs;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
