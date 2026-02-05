package com.yanxue.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * AI服务配置类
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "ai")
public class AIConfig {

    /**
     * OpenAI配置
     */
    private OpenAIConfig openai = new OpenAIConfig();

    /**
     * Qwen配置
     */
    private QwenConfig qwen = new QwenConfig();

    /**
     * 默认AI服务提供商
     */
    private String defaultProvider = "qwen";

    @Data
    public static class OpenAIConfig {
        private boolean enabled = true;
        private String apiKey;
        private String baseUrl = "https://api.openai.com/v1";
        private String model = "gpt-3.5-turbo";
        private int timeout = 60;
    }

    @Data
    public static class QwenConfig {
        private boolean enabled = true;
        private String apiKey;
        private String baseUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1";
        private String model = "qwen-turbo";
        private int timeout = 60;
    }

    /**
     * 获取当前使用的配置
     */
    public Object getCurrentConfig() {
        if ("openai".equalsIgnoreCase(defaultProvider)) {
            return openai;
        }
        return qwen;
    }
}
