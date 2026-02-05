package com.yanxue.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.yanxue.config.AIConfig;
import com.yanxue.dto.*;
import com.yanxue.entity.AILog;
import com.yanxue.entity.Route;
import com.yanxue.entity.Spot;
import com.yanxue.mapper.AILogMapper;
import com.yanxue.mapper.RouteMapper;
import com.yanxue.mapper.SpotMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * AI服务 - 核心推荐和路线生成服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AIService {

    private final AIConfig aiConfig;
    private final RouteMapper routeMapper;
    private final SpotMapper spotMapper;
    private final AILogMapper aiLogMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * AI智能推荐路线（增加降级处理）n     */
    public AIRecommendResponse recommend(AIRecommendRequest request) {
        long startTime = System.currentTimeMillis();

        // 1. 获取可推荐的路线列表
        List<Route> routes = routeMapper.selectList(null);
        if (routes.isEmpty()) {
            return new AIRecommendResponse();
        }

        // 2. 构建prompt
        String prompt = buildRecommendPrompt(request, routes);

        // 3. 调用AI接口
        String aiResponse = callAI(prompt);

        // 4. 解析响应（如果AI调用失败，使用基于规则的推荐）
        AIRecommendResponse response;
        if (aiResponse != null) {
            response = parseRecommendResponse(aiResponse, routes);
        } else {
            log.warn("AI推荐失败，使用基于规则的降级推荐");
            response = fallbackRecommend(request, routes);
        }

        // 5. 记录日志
        saveAILog(request.getUserId(), "recommend", prompt, aiResponse, startTime);

        return response;
    }
    
    /**
     * 基于规则的降级推荐（AI失败时使用）
     */
    private AIRecommendResponse fallbackRecommend(AIRecommendRequest request, List<Route> routes) {
        AIRecommendResponse response = new AIRecommendResponse();
        List<AIRecommendResponse.RecommendItem> recommendations = new ArrayList<>();
        
        // 获取用户偏好
        String grade = request.getUserProfile() != null ? request.getUserProfile().getGrade() : null;
        List<String> interests = request.getUserProfile() != null ? request.getUserProfile().getInterests() : null;
        Integer preferredDays = request.getUserProfile() != null ? request.getUserProfile().getPreferredDays() : null;
        
        // 为每条路线计算匹配分数
        for (Route route : routes) {
            double score = 0;
            StringBuilder reason = new StringBuilder();
            
            // 学段匹配
            if (grade != null && route.getSuitableGrades() != null) {
                if (route.getSuitableGrades().contains(grade)) {
                    score += 0.3;
                    reason.append("适合").append(grade).append("学生；");
                }
            }
            
            // 兴趣匹配
            if (interests != null && !interests.isEmpty() && route.getThemes() != null) {
                for (String interest : interests) {
                    if (route.getThemes().contains(interest)) {
                        score += 0.2;
                        reason.append("包含").append(interest).append("主题；");
                        break;
                    }
                }
            }
            
            // 天数匹配
            if (preferredDays != null && route.getDays() != null) {
                if (route.getDays().equals(preferredDays)) {
                    score += 0.3;
                    reason.append("天数符合期望；");
                } else if (Math.abs(route.getDays() - preferredDays) <= 1) {
                    score += 0.1;
                }
            }
            
            // 热门路线加分
            if (route.getViewCount() != null && route.getViewCount() > 500) {
                score += 0.1;
                reason.append("热门路线；");
            }
            
            // 基础分
            score += 0.1;
            
            if (score > 0.3) { // 只返回匹配度较高的
                AIRecommendResponse.RecommendItem item = new AIRecommendResponse.RecommendItem();
                item.setRouteId(route.getId());
                item.setName(route.getName());
                item.setReason(reason.length() > 0 ? reason.toString() : "综合推荐路线");
                item.setMatchScore(Math.min(score, 0.95));
                item.setDays(route.getDays());
                item.setPrice(route.getPrice());
                item.setCoverImage(route.getCoverImage());
                item.setDescription(route.getDescription());
                item.setViewCount(route.getViewCount());
                recommendations.add(item);
            }
        }
        
        // 按匹配分数排序，取前5条
        recommendations.sort((a, b) -> Double.compare(b.getMatchScore(), a.getMatchScore()));
        if (recommendations.size() > 5) {
            recommendations = recommendations.subList(0, 5);
        }
        
        // 如果没有匹配项，返回热门路线
        if (recommendations.isEmpty()) {
            List<AIRecommendResponse.RecommendItem> hotRoutes = routes.stream()
                .sorted((a, b) -> (b.getViewCount() != null ? b.getViewCount() : 0) 
                                 - (a.getViewCount() != null ? a.getViewCount() : 0))
                .limit(3)
                .map(route -> {
                    AIRecommendResponse.RecommendItem item = new AIRecommendResponse.RecommendItem();
                    item.setRouteId(route.getId());
                    item.setName(route.getName());
                    item.setReason("热门推荐路线");
                    item.setMatchScore(0.7);
                    item.setDays(route.getDays());
                    item.setPrice(route.getPrice());
                    item.setCoverImage(route.getCoverImage());
                    item.setDescription(route.getDescription());
                    item.setViewCount(route.getViewCount());
                    return item;
                })
                .collect(Collectors.toList());
            recommendations.addAll(hotRoutes);
        }
        
        response.setRecommendations(recommendations);
        log.info("降级推荐返回 {} 条路线", recommendations.size());
        return response;
    }

    /**
     * AI生成路线（增加降级处理）
     */
    public AIGenerateRouteResponse generateRoute(AIGenerateRouteRequest request) {
        long startTime = System.currentTimeMillis();

        // 1. 获取相关点位
        List<Spot> spots = spotMapper.selectList(null);

        // 2. 构建prompt
        String prompt = buildGenerateRoutePrompt(request, spots);

        // 3. 调用AI接口
        String aiResponse = callAI(prompt);

        // 4. 解析响应（AI失败时返回基于规则的生成路线）
        AIGenerateRouteResponse response;
        if (aiResponse != null) {
            response = parseGenerateRouteResponse(aiResponse);
        } else {
            log.warn("AI生成路线失败，使用基于规则的降级生成");
            response = fallbackGenerateRoute(request, spots);
        }

        // 5. 记录日志
        saveAILog(null, "generate", prompt, aiResponse, startTime);

        return response;
    }
    
    /**
     * 基于规则的降级路线生成（AI失败时使用）
     */
    private AIGenerateRouteResponse fallbackGenerateRoute(AIGenerateRouteRequest request, List<Spot> spots) {
        AIGenerateRouteResponse response = new AIGenerateRouteResponse();
        AIGenerateRouteResponse.GeneratedRoute route = new AIGenerateRouteResponse.GeneratedRoute();
        
        // 筛选符合条件的点位（同城市、适合学段）
        List<Spot> filteredSpots = spots.stream()
            .filter(s -> {
                // 城市匹配
                if (request.getStartCity() != null && s.getCity() != null 
                    && !s.getCity().contains(request.getStartCity()) 
                    && !request.getStartCity().contains(s.getCity())) {
                    return false;
                }
                // 类型匹配主题
                if (request.getTheme() != null && s.getThemes() != null) {
                    return s.getThemes().toLowerCase().contains(request.getTheme().toLowerCase())
                        || request.getTheme().toLowerCase().contains(s.getType() != null ? s.getType().toLowerCase() : "");
                }
                return true;
            })
            .limit(6) // 最多选6个点位
            .collect(Collectors.toList());
        
        if (filteredSpots.isEmpty()) {
            filteredSpots = spots.stream().limit(4).collect(Collectors.toList());
        }
        
        // 构建路线基本信息
        String theme = request.getTheme() != null ? request.getTheme() : "研学";
        route.setName(request.getStartCity() + theme + "研学路线（系统生成）");
        route.setDescription("基于您的需求生成的研学路线，包含" + filteredSpots.size() + "个精选点位，" +
                           "适合" + request.getGrade() + "学生，共" + request.getDays() + "天行程。");
        
        // 估算预算
        BigDecimal totalBudget = BigDecimal.ZERO;
        for (Spot spot : filteredSpots) {
            if (spot.getTicketPrice() != null) {
                totalBudget = totalBudget.add(spot.getTicketPrice());
            }
        }
        // 加上餐饮交通估算
        totalBudget = totalBudget.add(new BigDecimal(request.getDays() * 100)); // 每天餐饮交通约100元
        route.setTotalBudget(totalBudget);
        
        // 生成每日行程
        List<AIGenerateRouteResponse.DaySchedule> schedules = new ArrayList<>();
        int spotsPerDay = Math.max(2, Math.min(3, filteredSpots.size() / request.getDays()));
        
        for (int day = 1; day <= request.getDays(); day++) {
            AIGenerateRouteResponse.DaySchedule daySchedule = new AIGenerateRouteResponse.DaySchedule();
            daySchedule.setDay(day);
            
            List<AIGenerateRouteResponse.SpotArrangement> daySpots = new ArrayList<>();
            int startIdx = (day - 1) * spotsPerDay;
            
            for (int i = 0; i < spotsPerDay && (startIdx + i) < filteredSpots.size(); i++) {
                Spot spot = filteredSpots.get(startIdx + i);
                AIGenerateRouteResponse.SpotArrangement arrangement = new AIGenerateRouteResponse.SpotArrangement();
                arrangement.setSpotId(spot.getId());
                arrangement.setName(spot.getName());
                
                // 时间安排
                int startHour = 9 + i * 3;
                arrangement.setStartTime(startHour + ":00");
                arrangement.setEndTime((startHour + 2) + ":00");
                arrangement.setDuration("2小时");
                
                // 活动内容
                arrangement.setActivities("参观" + spot.getName() + "，了解" + 
                    (spot.getType() != null ? spot.getType() : "相关") + "知识");
                arrangement.setTips("请遵守场馆规定，注意安全");
                
                // 知识点
                List<String> kpList = new ArrayList<>();
                if (spot.getKnowledgePoints() != null) {
                    kpList.add(spot.getKnowledgePoints());
                } else {
                    kpList.add(theme + "知识学习");
                    kpList.add("实践能力培养");
                }
                arrangement.setKnowledgePoints(kpList);
                
                daySpots.add(arrangement);
            }
            
            daySchedule.setSpots(daySpots);
            schedules.add(daySchedule);
        }
        
        route.setSchedule(schedules);
        response.setRoute(route);
        
        log.info("降级生成路线: {}，包含{}个点位", route.getName(), filteredSpots.size());
        return response;
    }

    /**
     * 构建推荐prompt
     */
    private String buildRecommendPrompt(AIRecommendRequest request, List<Route> routes) {
        StringBuilder sb = new StringBuilder();
        sb.append("你是一个研学旅行推荐专家。请根据以下用户信息推荐合适的研学路线：\n\n");

        sb.append("用户信息：\n");
        if (request.getUserProfile() != null) {
            sb.append("- 学段：").append(request.getUserProfile().getGrade()).append("\n");
            sb.append("- 兴趣爱好：").append(String.join("、", request.getUserProfile().getInterests())).append("\n");
            sb.append("- 预算范围：").append(request.getUserProfile().getBudget()).append("\n");
            sb.append("- 期望天数：").append(request.getUserProfile().getPreferredDays()).append("天\n");
        }
        if (StrUtil.isNotBlank(request.getContext())) {
            sb.append("- 场景：").append(request.getContext()).append("\n");
        }

        sb.append("\n可选路线库：\n");
        for (Route route : routes) {
            sb.append("- ID:").append(route.getId())
              .append(" 名称:").append(route.getName())
              .append(" 天数:").append(route.getDays())
              .append(" 价格:").append(route.getPrice())
              .append(" 主题:").append(route.getThemes())
              .append("\n");
        }

        sb.append("\n请从上述路线中选择最适合该用户的3-5条路线，并说明推荐理由。\n");
        sb.append("请严格按照以下JSON格式输出，不要添加任何其他内容：\n");
        sb.append("{\"recommendations\": [{\"routeId\": 路线ID数字, \"name\": \"路线名称\", \"reason\": \"推荐理由\", \"matchScore\": 匹配度0-1之间的小数}]}\n");

        return sb.toString();
    }

    /**
     * 构建路线生成prompt
     */
    private String buildGenerateRoutePrompt(AIGenerateRouteRequest request, List<Spot> spots) {
        StringBuilder sb = new StringBuilder();
        sb.append("你是一个专业的研学旅行规划师。请根据以下要求生成研学路线：\n\n");

        sb.append("需求信息：\n");
        sb.append("- 主题：").append(request.getTheme()).append("\n");
        sb.append("- 天数：").append(request.getDays()).append("天\n");
        sb.append("- 预算：").append(request.getBudget()).append("元/人\n");
        sb.append("- 出发城市：").append(request.getStartCity()).append("\n");
        sb.append("- 学段：").append(request.getGrade()).append("\n");
        sb.append("- 人数：").append(request.getPeopleCount()).append("人\n");
        if (StrUtil.isNotBlank(request.getRequirements())) {
            sb.append("- 特殊要求：").append(request.getRequirements()).append("\n");
        }

        sb.append("\n可用研学点位：\n");
        for (Spot spot : spots) {
            sb.append("- ID:").append(spot.getId())
              .append(" 名称:").append(spot.getName())
              .append(" 城市:").append(spot.getCity())
              .append(" 类型:").append(spot.getType())
              .append(" 门票:").append(spot.getTicketPrice())
              .append("元\n");
        }

        sb.append("\n请生成一条合理的研学路线，要求：\n");
        sb.append("1. 每天安排2-3个点位\n");
        sb.append("2. 时间安排合理（9:00-17:00）\n");
        sb.append("3. 预算控制在要求范围内\n");
        sb.append("4. 每个点位标注教育目标和知识点\n\n");

        sb.append("请严格按照以下JSON格式输出：\n");
        sb.append("{\"route\": {\"name\": \"路线名称\", \"description\": \"路线简介\", \"totalBudget\": 总预算数字, ");
        sb.append("\"schedule\": [{\"day\": 天数, \"spots\": [{\"name\": \"点位名称\", \"spotId\": 点位ID或null, ");
        sb.append("\"duration\": \"时长\", \"startTime\": \"开始时间\", \"endTime\": \"结束时间\", ");
        sb.append("\"activities\": \"活动安排\", \"knowledgePoints\": [\"知识点1\"], \"tips\": \"提示\"}]}]}}\n");

        return sb.toString();
    }

    /**
     * 调用AI接口（支持OpenAI兼容接口和Qwen）
     * 优化：简化配置读取，增加超时降级处理
     */
    private String callAI(String prompt) {
        // 获取配置（优先从内存，其次Redis，最后默认配置）
        AIConfigData config = loadAIConfig();
        
        // 检查主配置是否有效
        boolean mainConfigValid = StrUtil.isNotBlank(config.apiKey) && !"sk-xxx".equals(config.apiKey);
        boolean backupConfigValid = config.backupProvider != null && 
                                   StrUtil.isNotBlank(config.backupApiKey) && 
                                   !"sk-xxx".equals(config.backupApiKey);
        
        log.info("AI配置检查: 主配置有效={}, 备用配置有效={}, 主provider={}, 备用provider={}", 
                mainConfigValid, backupConfigValid, config.provider, config.backupProvider);
        
        String result = null;
        
        // 先尝试主配置（如果有效）
        if (mainConfigValid) {
            result = executeAICall(prompt, config.provider, config.baseUrl, config.apiKey, config.model, config.timeout);
        } else {
            log.warn("主AI配置无效(apiKey为空或占位符)，跳过主配置调用");
        }
        
        // 主配置失败且备用配置有效，尝试备用
        if (result == null && backupConfigValid) {
            log.info("尝试备用配置: provider={}, model={}", config.backupProvider, config.backupModel);
            result = executeAICall(prompt, config.backupProvider, config.backupBaseUrl, 
                                  config.backupApiKey, config.backupModel, config.backupTimeout);
        } else if (result == null) {
            log.warn("备用配置也无效，无法调用AI服务");
        }
        
        return result;
    }
    
    /**
     * AI配置数据类
     */
    private static class AIConfigData {
        String provider;
        String baseUrl;
        String apiKey;
        String model;
        int timeout;
        // 备用配置
        String backupProvider;
        String backupBaseUrl;
        String backupApiKey;
        String backupModel;
        int backupTimeout;
    }
    
    /**
     * 加载AI配置（优先从Redis读取运行时配置）
     */
    private AIConfigData loadAIConfig() {
        AIConfigData data = new AIConfigData();
        
        // 先尝试从Redis读取配置
        try {
            Object redisConfig = redisTemplate.opsForValue().get("ai:config");
            if (redisConfig != null) {
                log.info("从Redis读取AI配置: {}", redisConfig.getClass().getName());
                
                // 使用反射获取配置（Redis存储的是AIConfigController.AIConfigDTO）
                java.lang.reflect.Method getDefaultProvider = redisConfig.getClass().getMethod("getDefaultProvider");
                String provider = (String) getDefaultProvider.invoke(redisConfig);
                
                if (provider != null) {
                    data.provider = provider;
                    
                    if ("openai".equalsIgnoreCase(provider)) {
                        java.lang.reflect.Method getOpenai = redisConfig.getClass().getMethod("getOpenai");
                        Object openaiConfig = getOpenai.invoke(redisConfig);
                        if (openaiConfig != null) {
                            data.baseUrl = (String) openaiConfig.getClass().getMethod("getBaseUrl").invoke(openaiConfig);
                            data.apiKey = (String) openaiConfig.getClass().getMethod("getApiKey").invoke(openaiConfig);
                            data.model = (String) openaiConfig.getClass().getMethod("getModel").invoke(openaiConfig);
                            Object timeoutObj = openaiConfig.getClass().getMethod("getTimeout").invoke(openaiConfig);
                            data.timeout = timeoutObj != null ? Math.min((int) timeoutObj, 120) : 60;
                            
                            // 备用qwen
                            java.lang.reflect.Method getQwen = redisConfig.getClass().getMethod("getQwen");
                            Object qwenConfig = getQwen.invoke(redisConfig);
                            if (qwenConfig != null) {
                                data.backupProvider = "qwen";
                                data.backupBaseUrl = (String) qwenConfig.getClass().getMethod("getBaseUrl").invoke(qwenConfig);
                                data.backupApiKey = (String) qwenConfig.getClass().getMethod("getApiKey").invoke(qwenConfig);
                                data.backupModel = (String) qwenConfig.getClass().getMethod("getModel").invoke(qwenConfig);
                                Object qwenTimeout = qwenConfig.getClass().getMethod("getTimeout").invoke(qwenConfig);
                                data.backupTimeout = qwenTimeout != null ? Math.min((int) qwenTimeout, 120) : 60;
                            }
                        }
                    } else {
                        java.lang.reflect.Method getQwen = redisConfig.getClass().getMethod("getQwen");
                        Object qwenConfig = getQwen.invoke(redisConfig);
                        if (qwenConfig != null) {
                            data.baseUrl = (String) qwenConfig.getClass().getMethod("getBaseUrl").invoke(qwenConfig);
                            data.apiKey = (String) qwenConfig.getClass().getMethod("getApiKey").invoke(qwenConfig);
                            data.model = (String) qwenConfig.getClass().getMethod("getModel").invoke(qwenConfig);
                            Object timeoutObj = qwenConfig.getClass().getMethod("getTimeout").invoke(qwenConfig);
                            data.timeout = timeoutObj != null ? Math.min((int) timeoutObj, 120) : 60;
                            
                            // 备用openai
                            java.lang.reflect.Method getOpenai = redisConfig.getClass().getMethod("getOpenai");
                            Object openaiConfig = getOpenai.invoke(redisConfig);
                            if (openaiConfig != null) {
                                data.backupProvider = "openai";
                                data.backupBaseUrl = (String) openaiConfig.getClass().getMethod("getBaseUrl").invoke(openaiConfig);
                                data.backupApiKey = (String) openaiConfig.getClass().getMethod("getApiKey").invoke(openaiConfig);
                                data.backupModel = (String) openaiConfig.getClass().getMethod("getModel").invoke(openaiConfig);
                                Object openaiTimeout = openaiConfig.getClass().getMethod("getTimeout").invoke(openaiConfig);
                                data.backupTimeout = openaiTimeout != null ? Math.min((int) openaiTimeout, 120) : 60;
                            }
                        }
                    }
                    
                    // 验证Redis配置是否有效（主配置或备用配置至少一个有效）
                    boolean mainValid = StrUtil.isNotBlank(data.apiKey) && !"sk-xxx".equals(data.apiKey);
                    boolean backupValid = StrUtil.isNotBlank(data.backupApiKey) && !"sk-xxx".equals(data.backupApiKey);
                    
                    if (mainValid || backupValid) {
                        log.info("使用Redis AI配置: provider={}, model={}, timeout={}s, 主配置有效={}, 备用配置有效={}", 
                                data.provider, data.model, data.timeout, mainValid, backupValid);
                        return data;
                    } else {
                        log.warn("Redis中AI配置无效(主配置和备用配置的apiKey都为空或占位符)，使用默认配置");
                    }
                }
            }
        } catch (Exception e) {
            log.warn("从Redis读取AI配置失败: {}", e.getMessage());
        }
        
        // 使用默认配置（application.yml）
        String provider = aiConfig.getDefaultProvider();
        data.provider = provider;
        
        if ("openai".equalsIgnoreCase(provider)) {
            data.baseUrl = aiConfig.getOpenai().getBaseUrl();
            data.apiKey = aiConfig.getOpenai().getApiKey();
            data.model = aiConfig.getOpenai().getModel();
            data.timeout = Math.min(aiConfig.getOpenai().getTimeout(), 120);
            data.backupProvider = "qwen";
            data.backupBaseUrl = aiConfig.getQwen().getBaseUrl();
            data.backupApiKey = aiConfig.getQwen().getApiKey();
            data.backupModel = aiConfig.getQwen().getModel();
            data.backupTimeout = Math.min(aiConfig.getQwen().getTimeout(), 120);
        } else {
            data.baseUrl = aiConfig.getQwen().getBaseUrl();
            data.apiKey = aiConfig.getQwen().getApiKey();
            data.model = aiConfig.getQwen().getModel();
            data.timeout = Math.min(aiConfig.getQwen().getTimeout(), 120);
            data.backupProvider = "openai";
            data.backupBaseUrl = aiConfig.getOpenai().getBaseUrl();
            data.backupApiKey = aiConfig.getOpenai().getApiKey();
            data.backupModel = aiConfig.getOpenai().getModel();
            data.backupTimeout = Math.min(aiConfig.getOpenai().getTimeout(), 120);
        }
        
        log.info("使用默认AI配置: provider={}, model={}, timeout={}s", data.provider, data.model, data.timeout);
        return data;
    }

    /**
     * 执行AI调用（使用JSONObject构建请求，避免转义问题）
     */
    private String executeAICall(String prompt, String provider, String baseUrl, String apiKey, String model, int timeout) {
        // 参数校验
        if (StrUtil.isBlank(baseUrl) || StrUtil.isBlank(apiKey) || "sk-xxx".equals(apiKey)) {
            log.warn("AI配置无效: baseUrl或apiKey未配置");
            return null;
        }

        try {
            // 使用JSONObject构建请求体，避免手动转义问题
            JSONObject requestBody = new JSONObject();
            requestBody.put("model", model);
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 2000);
            
            JSONArray messages = new JSONArray();
            JSONObject userMessage = new JSONObject();
            userMessage.put("role", "user");
            userMessage.put("content", prompt);  // 直接使用原始prompt，JSONObject会自动转义
            messages.add(userMessage);
            requestBody.put("messages", messages);
            
            String requestBodyStr = requestBody.toJSONString();
            log.info("调用AI服务: provider={}, baseUrl={}, model={}, timeout={}s", provider, baseUrl, model, timeout);
            log.debug("AI请求体: {}", requestBodyStr);
            long start = System.currentTimeMillis();

            HttpResponse response = HttpRequest.post(baseUrl + "/chat/completions")
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .body(requestBodyStr)
                    .timeout(timeout * 1000)
                    .execute();

            long duration = System.currentTimeMillis() - start;
            log.info("AI调用完成: status={}, duration={}ms", response.getStatus(), duration);

            if (response.isOk()) {
                String responseBody = response.body();
                log.debug("AI原始响应: {}", responseBody);
                
                JSONObject result = JSON.parseObject(responseBody);
                JSONArray choices = result.getJSONArray("choices");
                if (choices != null && !choices.isEmpty()) {
                    JSONObject messageObj = choices.getJSONObject(0).getJSONObject("message");

                    // 尝试获取content，如果为null则尝试reasoning_content
                    String content = messageObj.getString("content");
                    if (content == null || content.trim().isEmpty()) {
                        content = messageObj.getString("reasoning_content");
                        if (content != null && !content.trim().isEmpty()) {
                            log.info("使用reasoning_content字段获取AI响应");
                        }
                    }

                    if (content != null && !content.trim().isEmpty()) {
                        log.info("AI响应内容长度: {}字符", content.length());
                        return content;
                    } else {
                        log.warn("AI响应内容为空，message对象: {}", messageObj);
                    }
                } else {
                    log.warn("AI响应格式异常: 无choices字段，响应: {}", responseBody);
                }
            } else {
                log.error("AI调用失败: status={}, body={}", response.getStatus(), 
                         response.body() != null && response.body().length() > 500 
                         ? response.body().substring(0, 500) + "..." : response.body());
            }
        } catch (cn.hutool.core.io.IORuntimeException e) {
            log.error("AI调用超时或网络错误: {}", e.getMessage());
        } catch (Exception e) {
            log.error("AI调用异常: {}", e.getMessage(), e);
        }

        return null;
    }

    /**
     * 解析推荐响应
     */
    private AIRecommendResponse parseRecommendResponse(String aiResponse, List<Route> routes) {
        AIRecommendResponse response = new AIRecommendResponse();
        response.setRecommendations(new ArrayList<>());

        if (StrUtil.isBlank(aiResponse)) {
            return response;
        }

        // 创建路线ID到路线对象的映射
        Map<Long, Route> routeMap = routes.stream()
                .collect(Collectors.toMap(Route::getId, r -> r));

        try {
            // 提取JSON部分
            String jsonStr = extractJson(aiResponse);
            log.info("提取的JSON字符串: {}", jsonStr);

            JSONObject json = JSON.parseObject(jsonStr);
            JSONArray recommendations = json.getJSONArray("recommendations");

            if (recommendations != null) {
                log.info("解析到 {} 条推荐", recommendations.size());
                for (int i = 0; i < recommendations.size(); i++) {
                    JSONObject item = recommendations.getJSONObject(i);
                    Long routeId = item.getLong("routeId");
                    log.info("处理推荐路线ID: {}", routeId);

                    // 从路线列表中获取完整的路线信息
                    Route route = routeMap.get(routeId);
                    if (route != null) {
                        AIRecommendResponse.RecommendItem recommendItem = new AIRecommendResponse.RecommendItem();
                        recommendItem.setRouteId(routeId);
                        recommendItem.setName(route.getName());
                        recommendItem.setReason(item.getString("reason"));
                        recommendItem.setMatchScore(item.getDouble("matchScore"));

                        // 添加路线的完整信息
                        recommendItem.setDays(route.getDays());
                        recommendItem.setPrice(route.getPrice());
                        recommendItem.setCoverImage(route.getCoverImage());
                        recommendItem.setDescription(route.getDescription());
                        recommendItem.setViewCount(route.getViewCount());

                        response.getRecommendations().add(recommendItem);
                        log.info("成功添加推荐: {}", route.getName());
                    } else {
                        log.warn("路线ID {} 在数据库中不存在", routeId);
                    }
                }
            } else {
                log.warn("AI响应中没有recommendations字段");
            }
        } catch (Exception e) {
            log.error("解析AI推荐响应失败，原始响应: {}", aiResponse, e);
        }

        return response;
    }

    /**
     * 解析路线生成响应
     */
    private AIGenerateRouteResponse parseGenerateRouteResponse(String aiResponse) {
        AIGenerateRouteResponse response = new AIGenerateRouteResponse();

        if (StrUtil.isBlank(aiResponse)) {
            return response;
        }

        try {
            String jsonStr = extractJson(aiResponse);
            JSONObject json = JSON.parseObject(jsonStr);
            JSONObject routeJson = json.getJSONObject("route");

            if (routeJson != null) {
                AIGenerateRouteResponse.GeneratedRoute route = new AIGenerateRouteResponse.GeneratedRoute();
                route.setName(routeJson.getString("name"));
                route.setDescription(routeJson.getString("description"));
                route.setTotalBudget(routeJson.getBigDecimal("totalBudget"));

                List<AIGenerateRouteResponse.DaySchedule> schedules = new ArrayList<>();
                JSONArray scheduleArray = routeJson.getJSONArray("schedule");
                if (scheduleArray != null) {
                    for (int i = 0; i < scheduleArray.size(); i++) {
                        JSONObject dayJson = scheduleArray.getJSONObject(i);
                        AIGenerateRouteResponse.DaySchedule daySchedule = new AIGenerateRouteResponse.DaySchedule();
                        daySchedule.setDay(dayJson.getInteger("day"));

                        List<AIGenerateRouteResponse.SpotArrangement> spots = new ArrayList<>();
                        JSONArray spotsArray = dayJson.getJSONArray("spots");
                        if (spotsArray != null) {
                            for (int j = 0; j < spotsArray.size(); j++) {
                                JSONObject spotJson = spotsArray.getJSONObject(j);
                                AIGenerateRouteResponse.SpotArrangement spot = new AIGenerateRouteResponse.SpotArrangement();
                                spot.setName(spotJson.getString("name"));
                                spot.setSpotId(spotJson.getLong("spotId"));
                                spot.setDuration(spotJson.getString("duration"));
                                spot.setStartTime(spotJson.getString("startTime"));
                                spot.setEndTime(spotJson.getString("endTime"));
                                spot.setActivities(spotJson.getString("activities"));
                                spot.setTips(spotJson.getString("tips"));

                                JSONArray kpArray = spotJson.getJSONArray("knowledgePoints");
                                if (kpArray != null) {
                                    spot.setKnowledgePoints(kpArray.toJavaList(String.class));
                                }
                                spots.add(spot);
                            }
                        }
                        daySchedule.setSpots(spots);
                        schedules.add(daySchedule);
                    }
                }
                route.setSchedule(schedules);
                response.setRoute(route);
            }
        } catch (Exception e) {
            log.error("解析AI生成路线响应失败", e);
        }

        return response;
    }

    /**
     * 从响应中提取JSON（改进版：处理Markdown代码块和嵌套JSON）
     */
    private String extractJson(String text) {
        if (text == null) return "{}";
        
        text = text.trim();
        
        // 首先尝试提取Markdown代码块中的JSON
        // 匹配 ```json ... ``` 或 ``` ... ```
        if (text.contains("```")) {
            int codeBlockStart = text.indexOf("```json");
            if (codeBlockStart >= 0) {
                codeBlockStart = text.indexOf("\n", codeBlockStart) + 1;
                int codeBlockEnd = text.indexOf("```", codeBlockStart);
                if (codeBlockEnd > codeBlockStart) {
                    String jsonContent = text.substring(codeBlockStart, codeBlockEnd).trim();
                    log.info("从Markdown代码块中提取JSON (json标签)");
                    return jsonContent;
                }
            }
            
            // 尝试普通的代码块 ``` ... ```
            codeBlockStart = text.indexOf("```");
            if (codeBlockStart >= 0) {
                codeBlockStart = text.indexOf("\n", codeBlockStart);
                if (codeBlockStart < 0) {
                    codeBlockStart = text.indexOf("```") + 3;
                } else {
                    codeBlockStart += 1;
                }
                int codeBlockEnd = text.indexOf("```", codeBlockStart);
                if (codeBlockEnd > codeBlockStart) {
                    String jsonContent = text.substring(codeBlockStart, codeBlockEnd).trim();
                    if (jsonContent.startsWith("{") || jsonContent.startsWith("[")) {
                        log.info("从Markdown代码块中提取JSON (普通代码块)");
                        return jsonContent;
                    }
                }
            }
        }
        
        // 尝试找到最外层的JSON对象（找到第一个 { 和最后一个匹配的 }）
        int start = text.indexOf("{");
        if (start >= 0) {
            // 尝试匹配括号，找到正确的结束位置
            int braceCount = 0;
            int end = -1;
            for (int i = start; i < text.length(); i++) {
                char c = text.charAt(i);
                if (c == '{') {
                    braceCount++;
                } else if (c == '}') {
                    braceCount--;
                    if (braceCount == 0) {
                        end = i;
                        break;
                    }
                }
            }
            
            if (end > start) {
                return text.substring(start, end + 1);
            }
        }
        
        // 尝试找到JSON数组
        start = text.indexOf("[");
        if (start >= 0) {
            int bracketCount = 0;
            int end = -1;
            for (int i = start; i < text.length(); i++) {
                char c = text.charAt(i);
                if (c == '[') {
                    bracketCount++;
                } else if (c == ']') {
                    bracketCount--;
                    if (bracketCount == 0) {
                        end = i;
                        break;
                    }
                }
            }
            
            if (end > start) {
                return text.substring(start, end + 1);
            }
        }
        
        // 兜底：返回原文本
        log.warn("无法从响应中提取JSON，返回原文本前100字符: {}", 
                text.length() > 100 ? text.substring(0, 100) + "..." : text);
        return text;
    }

    /**
     * 保存AI调用日志
     */
    private void saveAILog(Long userId, String type, String prompt, String response, long startTime) {
        try {
            AILog log = new AILog();
            log.setUserId(userId);
            log.setType(type);
            log.setModel(aiConfig.getDefaultProvider());
            log.setPrompt(prompt);
            log.setResponse(response);
            log.setDurationMs((int) (System.currentTimeMillis() - startTime));
            aiLogMapper.insert(log);
        } catch (Exception e) {
            // 日志保存失败不影响主流程
        }
    }
}
