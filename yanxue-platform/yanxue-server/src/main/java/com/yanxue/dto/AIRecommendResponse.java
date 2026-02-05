package com.yanxue.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * AI推荐响应DTO
 */
@Data
public class AIRecommendResponse {

    /**
     * 推荐列表
     */
    private List<RecommendItem> recommendations;

    /**
     * 推荐项
     */
    @Data
    public static class RecommendItem {
        /**
         * 路线ID
         */
        private Long routeId;

        /**
         * 路线名称
         */
        private String name;

        /**
         * 推荐理由
         */
        private String reason;

        /**
         * 匹配度评分
         */
        private Double matchScore;

        /**
         * 天数
         */
        private Integer days;

        /**
         * 价格
         */
        private BigDecimal price;

        /**
         * 封面图
         */
        private String coverImage;

        /**
         * 描述
         */
        private String description;

        /**
         * 浏览量
         */
        private Integer viewCount;
    }
}
