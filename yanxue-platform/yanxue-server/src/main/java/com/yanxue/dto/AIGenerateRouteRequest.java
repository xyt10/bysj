package com.yanxue.dto;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * AI路线生成请求DTO
 */
@Data
public class AIGenerateRouteRequest {

    /**
     * 主题
     */
    @NotBlank(message = "主题不能为空")
    private String theme;

    /**
     * 天数
     */
    @NotNull(message = "天数不能为空")
    @Min(value = 1, message = "天数最少为1天")
    @Max(value = 14, message = "天数最多为14天")
    private Integer days;

    /**
     * 预算（元/人）
     */
    @NotNull(message = "预算不能为空")
    @Min(value = 0, message = "预算不能为负数")
    private Integer budget;

    /**
     * 出发城市
     */
    @NotBlank(message = "出发城市不能为空")
    private String startCity;

    /**
     * 学段
     */
    @NotBlank(message = "学段不能为空")
    private String grade;

    /**
     * 人数
     */
    @NotNull(message = "人数不能为空")
    @Min(value = 1, message = "人数最少为1")
    @Max(value = 500, message = "人数不能超过500")
    private Integer peopleCount;

    /**
     * 特殊要求
     */
    private String requirements;
}
