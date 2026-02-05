package com.yanxue.controller;

import com.yanxue.dto.AIGenerateRouteRequest;
import com.yanxue.dto.AIGenerateRouteResponse;
import com.yanxue.dto.AIRecommendRequest;
import com.yanxue.dto.AIRecommendResponse;
import com.yanxue.service.AIService;
import com.yanxue.vo.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * AI接口控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AIController {

    private final AIService aiService;

    /**
     * AI智能推荐路线
     */
    @PostMapping("/recommend")
    public Result<AIRecommendResponse> recommend(@RequestBody AIRecommendRequest request) {
        log.info("AI推荐请求: {}", request);
        try {
            AIRecommendResponse response = aiService.recommend(request);
            return Result.success(response);
        } catch (Exception e) {
            log.error("AI推荐失败", e);
            return Result.error("AI推荐服务暂时不可用，请稍后重试");
        }
    }

    /**
     * AI生成路线
     */
    @PostMapping("/generate-route")
    public Result<AIGenerateRouteResponse> generateRoute(@RequestBody AIGenerateRouteRequest request) {
        log.info("AI路线生成请求: {}", request);
        try {
            AIGenerateRouteResponse response = aiService.generateRoute(request);
            return Result.success(response);
        } catch (Exception e) {
            log.error("AI路线生成失败", e);
            return Result.error("AI路线生成服务暂时不可用，请稍后重试");
        }
    }
}
