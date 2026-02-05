package com.yanxue.dto;

import lombok.Data;
import java.util.List;

/**
 * AI推荐请求DTO
 */
@Data
public class AIRecommendRequest {

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户画像
     */
    private UserProfile userProfile;

    /**
     * 推荐场景/上下文
     */
    private String context;

    /**
     * 用户画像内部类
     */
    @Data
    public static class UserProfile {
        /**
         * 学段
         */
        private String grade;

        /**
         * 兴趣标签
         */
        private List<String> interests;

        /**
         * 预算范围
         */
        private String budget;

        /**
         * 期望天数
         */
        private Integer preferredDays;
    }
}
