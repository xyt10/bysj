package com.yanxue.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 路线实体类
 */
@Data
@TableName("route")
public class Route {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 路线名称
     */
    private String name;

    /**
     * 路线描述
     */
    private String description;

    /**
     * 天数
     */
    private Integer days;

    /**
     * 参考价格
     */
    private BigDecimal price;

    /**
     * 适合学段JSON
     */
    private String suitableGrades;

    /**
     * 主题标签JSON
     */
    private String themes;

    /**
     * 封面图片
     */
    private String coverImage;

    /**
     * 来源：manual/ai
     */
    private String source;

    /**
     * AI生成时的prompt
     */
    private String aiPrompt;

    /**
     * 浏览量
     */
    private Integer viewCount;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 状态：1启用 0禁用
     */
    private Integer status;

    /**
     * 创建者ID
     */
    private Long createdBy;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
