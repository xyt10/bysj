package com.yanxue.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 研学点位实体类
 */
@Data
@TableName("spot")
public class Spot {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 名称
     */
    private String name;

    /**
     * 类型：博物馆/科技馆/景区/基地
     */
    private String type;

    /**
     * 省份
     */
    private String province;

    /**
     * 城市
     */
    private String city;

    /**
     * 详细地址
     */
    private String address;

    /**
     * 经度
     */
    private BigDecimal longitude;

    /**
     * 纬度
     */
    private BigDecimal latitude;

    /**
     * 简介
     */
    private String description;

    /**
     * 开放时间
     */
    private String openTime;

    /**
     * 门票价格
     */
    private BigDecimal ticketPrice;

    /**
     * 适合学段JSON
     */
    private String suitableGrades;

    /**
     * 主题标签JSON
     */
    private String themes;

    /**
     * 知识点JSON
     */
    private String knowledgePoints;

    /**
     * 图片列表JSON
     */
    private String images;

    /**
     * 状态：1启用 0禁用
     */
    private Integer status;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
