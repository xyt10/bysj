package com.yanxue.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * AI生成路线响应DTO
 */
@Data
public class AIGenerateRouteResponse {

    /**
     * 路线信息
     */
    private GeneratedRoute route;

    /**
     * 生成的路线
     */
    @Data
    public static class GeneratedRoute {
        /**
         * 路线名称
         */
        private String name;

        /**
         * 路线描述
         */
        private String description;

        /**
         * 总预算
         */
        private BigDecimal totalBudget;

        /**
         * 每日行程
         */
        private List<DaySchedule> schedule;
    }

    /**
     * 每日行程
     */
    @Data
    public static class DaySchedule {
        /**
         * 第几天
         */
        private Integer day;

        /**
         * 当天点位
         */
        private List<SpotArrangement> spots;
    }

    /**
     * 点位安排
     */
    @Data
    public static class SpotArrangement {
        /**
         * 点位名称
         */
        private String name;

        /**
         * 点位ID（如果能匹配到数据库）
         */
        private Long spotId;

        /**
         * 持续时长
         */
        private String duration;

        /**
         * 开始时间
         */
        private String startTime;

        /**
         * 结束时间
         */
        private String endTime;

        /**
         * 活动安排
         */
        private String activities;

        /**
         * 知识点
         */
        private List<String> knowledgePoints;

        /**
         * 温馨提示
         */
        private String tips;
    }
}
