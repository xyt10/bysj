package com.yanxue.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 研学成果实体类
 */
@Data
@TableName("achievement")
public class Achievement {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 路线ID
     */
    private Long routeId;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 图片JSON
     */
    private String images;

    /**
     * 视频地址
     */
    private String videoUrl;

    /**
     * 点赞数
     */
    private Integer likeCount;

    /**
     * 浏览量
     */
    private Integer viewCount;

    /**
     * 状态
     */
    private Integer status;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
