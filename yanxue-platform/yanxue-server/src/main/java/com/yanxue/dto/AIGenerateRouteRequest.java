package com.yanxue.dto;

import lombok.Data;

/**
 * AI路线生成请求DTO
 */
@Data
public class AIGenerateRouteRequest {

    /**
     * 主题
     */
    private String theme;

    /**
     * 天数
     */
    private Integer days;

    /**
     * 预算（元/人）
     */
    private Integer budget;

    /**
     * 出发城市
     */
    private String startCity;

    /**
     * 学段
     */
    private String grade;

    /**
     * 人数
     */
    private Integer peopleCount;

    /**
     * 特殊要求
     */
    private String requirements;
}
