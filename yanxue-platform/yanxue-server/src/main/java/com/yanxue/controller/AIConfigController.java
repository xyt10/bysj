package com.yanxue.controller;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yanxue.config.AIConfig;
import com.yanxue.entity.AILog;
import com.yanxue.mapper.AILogMapper;
import com.yanxue.vo.Result;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * AI配置管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/ai-config")
@RequiredArgsConstructor
public class AIConfigController {

    private final AIConfig aiConfig;
    private final AILogMapper aiLogMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String AI_CONFIG_KEY = "ai:config";

    /**
     * 获取AI配置
     */
    @GetMapping
    public Result<AIConfigDTO> getConfig() {
        // 先从Redis获取运行时配置
        AIConfigDTO config = (AIConfigDTO) redisTemplate.opsForValue().get(AI_CONFIG_KEY);

        if (config == null) {
            // 没有运行时配置，使用默认配置
            config = new AIConfigDTO();
            config.setDefaultProvider(aiConfig.getDefaultProvider());

            AIConfigDTO.ProviderConfig qwen = new AIConfigDTO.ProviderConfig();
            qwen.setEnabled(aiConfig.getQwen().isEnabled());
            qwen.setApiKey(maskApiKey(aiConfig.getQwen().getApiKey()));
            qwen.setBaseUrl(aiConfig.getQwen().getBaseUrl());
            qwen.setModel(aiConfig.getQwen().getModel());
            qwen.setTimeout(aiConfig.getQwen().getTimeout());
            config.setQwen(qwen);

            AIConfigDTO.ProviderConfig openai = new AIConfigDTO.ProviderConfig();
            openai.setEnabled(aiConfig.getOpenai().isEnabled());
            openai.setApiKey(maskApiKey(aiConfig.getOpenai().getApiKey()));
            openai.setBaseUrl(aiConfig.getOpenai().getBaseUrl());
            openai.setModel(aiConfig.getOpenai().getModel());
            openai.setTimeout(aiConfig.getOpenai().getTimeout());
            config.setOpenai(openai);
        } else {
            // 对API Key进行脱敏
            config.getQwen().setApiKey(maskApiKey(config.getQwen().getApiKey()));
            config.getOpenai().setApiKey(maskApiKey(config.getOpenai().getApiKey()));
        }

        return Result.success(config);
    }

    /**
     * 保存AI配置
     */
    @PostMapping
    public Result<Void> saveConfig(@RequestBody AIConfigDTO config) {
        // 如果API Key是脱敏的，保留原来的值
        AIConfigDTO existingConfig = (AIConfigDTO) redisTemplate.opsForValue().get(AI_CONFIG_KEY);

        if (config.getQwen().getApiKey().contains("***")) {
            if (existingConfig != null) {
                config.getQwen().setApiKey(existingConfig.getQwen().getApiKey());
            } else {
                config.getQwen().setApiKey(aiConfig.getQwen().getApiKey());
            }
        }

        if (config.getOpenai().getApiKey().contains("***")) {
            if (existingConfig != null) {
                config.getOpenai().setApiKey(existingConfig.getOpenai().getApiKey());
            } else {
                config.getOpenai().setApiKey(aiConfig.getOpenai().getApiKey());
            }
        }

        // 保存到Redis
        redisTemplate.opsForValue().set(AI_CONFIG_KEY, config);

        // 同步更新到AIConfig bean
        aiConfig.setDefaultProvider(config.getDefaultProvider());
        aiConfig.getQwen().setApiKey(config.getQwen().getApiKey());
        aiConfig.getQwen().setBaseUrl(config.getQwen().getBaseUrl());
        aiConfig.getQwen().setModel(config.getQwen().getModel());
        aiConfig.getQwen().setTimeout(config.getQwen().getTimeout());
        aiConfig.getOpenai().setApiKey(config.getOpenai().getApiKey());
        aiConfig.getOpenai().setBaseUrl(config.getOpenai().getBaseUrl());
        aiConfig.getOpenai().setModel(config.getOpenai().getModel());
        aiConfig.getOpenai().setTimeout(config.getOpenai().getTimeout());

        log.info("AI配置已更新: provider={}", config.getDefaultProvider());
        return Result.success(null);
    }

    /**
     * 测试AI连接
     */
    @PostMapping("/test")
    public Result<TestResult> testConnection(@RequestBody TestRequest request) {
        String provider = request.getProvider();
        String baseUrl;
        String apiKey;
        String model;

        // 获取配置
        AIConfigDTO config = (AIConfigDTO) redisTemplate.opsForValue().get(AI_CONFIG_KEY);

        if ("openai".equalsIgnoreCase(provider)) {
            if (config != null && !config.getOpenai().getApiKey().contains("***")) {
                baseUrl = config.getOpenai().getBaseUrl();
                apiKey = config.getOpenai().getApiKey();
                model = config.getOpenai().getModel();
            } else {
                baseUrl = aiConfig.getOpenai().getBaseUrl();
                apiKey = aiConfig.getOpenai().getApiKey();
                model = aiConfig.getOpenai().getModel();
            }
        } else {
            if (config != null && !config.getQwen().getApiKey().contains("***")) {
                baseUrl = config.getQwen().getBaseUrl();
                apiKey = config.getQwen().getApiKey();
                model = config.getQwen().getModel();
            } else {
                baseUrl = aiConfig.getQwen().getBaseUrl();
                apiKey = aiConfig.getQwen().getApiKey();
                model = aiConfig.getQwen().getModel();
            }
        }

        TestResult result = new TestResult();
        long startTime = System.currentTimeMillis();

        try {
            // 构建测试请求
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", model);
            requestBody.put("max_tokens", 50);

            JSONArray messages = new JSONArray();
            JSONObject userMessage = new JSONObject();
            userMessage.put("role", "user");
            userMessage.put("content", "请回复'连接成功'四个字");
            messages.add(userMessage);
            requestBody.put("messages", messages);

            HttpResponse response = HttpRequest.post(baseUrl + "/chat/completions")
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .body(requestBody.toJSONString())
                    .timeout(30000)
                    .execute();

            result.setDurationMs((int) (System.currentTimeMillis() - startTime));

            if (response.isOk()) {
                JSONObject respJson = JSON.parseObject(response.body());
                JSONArray choices = respJson.getJSONArray("choices");
                if (choices != null && !choices.isEmpty()) {
                    String content = choices.getJSONObject(0)
                            .getJSONObject("message")
                            .getString("content");
                    result.setSuccess(true);
                    result.setMessage("连接成功");
                    result.setResponse(content);
                    result.setModel(model);
                } else {
                    result.setSuccess(false);
                    result.setMessage("AI响应格式异常");
                }
            } else {
                result.setSuccess(false);
                result.setMessage("请求失败: " + response.getStatus() + " - " + response.body());
            }
        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage("连接失败: " + e.getMessage());
            result.setDurationMs((int) (System.currentTimeMillis() - startTime));
            log.error("AI连接测试失败", e);
        }

        return Result.success(result);
    }

    /**
     * 获取AI调用日志
     */
    @GetMapping("/logs")
    public Result<Page<AILog>> getLogs(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        Page<AILog> page = new Page<>(pageNum, pageSize);
        QueryWrapper<AILog> wrapper = new QueryWrapper<>();

        if (type != null && !type.isEmpty()) {
            wrapper.eq("type", type);
        }
        if (startDate != null && !startDate.isEmpty()) {
            wrapper.ge("created_at", LocalDate.parse(startDate).atStartOfDay());
        }
        if (endDate != null && !endDate.isEmpty()) {
            wrapper.lt("created_at", LocalDate.parse(endDate).plusDays(1).atStartOfDay());
        }

        wrapper.orderByDesc("created_at");

        return Result.success(aiLogMapper.selectPage(page, wrapper));
    }

    /**
     * 获取AI统计数据
     */
    @GetMapping("/stats")
    public Result<AIStats> getStats() {
        AIStats stats = new AIStats();

        // 总调用次数
        stats.setTotalCalls(aiLogMapper.selectCount(null));

        // 今日调用次数
        QueryWrapper<AILog> todayWrapper = new QueryWrapper<>();
        todayWrapper.ge("created_at", LocalDate.now().atStartOfDay());
        stats.setTodayCalls(aiLogMapper.selectCount(todayWrapper));

        // 推荐调用次数
        QueryWrapper<AILog> recommendWrapper = new QueryWrapper<>();
        recommendWrapper.eq("type", "recommend");
        stats.setRecommendCalls(aiLogMapper.selectCount(recommendWrapper));

        // 生成调用次数
        QueryWrapper<AILog> generateWrapper = new QueryWrapper<>();
        generateWrapper.eq("type", "generate");
        stats.setGenerateCalls(aiLogMapper.selectCount(generateWrapper));

        // 平均响应时间
        QueryWrapper<AILog> avgWrapper = new QueryWrapper<>();
        avgWrapper.isNotNull("duration_ms");
        avgWrapper.select("AVG(duration_ms) as avg_duration");
        Map<String, Object> avgResult = aiLogMapper.selectMaps(avgWrapper).stream().findFirst().orElse(new HashMap<>());
        Object avgDuration = avgResult.get("avg_duration");
        stats.setAvgDurationMs(avgDuration != null ? ((Number) avgDuration).intValue() : 0);

        return Result.success(stats);
    }

    /**
     * 对API Key进行脱敏
     */
    private String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.length() < 8) {
            return "***";
        }
        return apiKey.substring(0, 4) + "***" + apiKey.substring(apiKey.length() - 4);
    }

    @Data
    public static class AIConfigDTO implements java.io.Serializable {
        private String defaultProvider;
        private ProviderConfig qwen;
        private ProviderConfig openai;

        @Data
        public static class ProviderConfig implements java.io.Serializable {
            private boolean enabled;
            private String apiKey;
            private String baseUrl;
            private String model;
            private int timeout;
        }
    }

    @Data
    public static class TestRequest {
        private String provider;
    }

    @Data
    public static class TestResult {
        private boolean success;
        private String message;
        private String response;
        private String model;
        private int durationMs;
    }

    @Data
    public static class AIStats {
        private Long totalCalls;
        private Long todayCalls;
        private Long recommendCalls;
        private Long generateCalls;
        private Integer avgDurationMs;
    }
}
