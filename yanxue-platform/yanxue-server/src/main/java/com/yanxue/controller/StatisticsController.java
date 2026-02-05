package com.yanxue.controller;

import com.yanxue.entity.Route;
import com.yanxue.entity.Spot;
import com.yanxue.entity.User;
import com.yanxue.mapper.RouteMapper;
import com.yanxue.mapper.SpotMapper;
import com.yanxue.mapper.UserMapper;
import com.yanxue.vo.Result;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * 统计数据控制器
 */
@RestController
@RequestMapping("/api/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final UserMapper userMapper;
    private final RouteMapper routeMapper;
    private final SpotMapper spotMapper;

    /**
     * 获取仪表盘统计数据
     */
    @GetMapping("/dashboard")
    public Result<DashboardStats> getDashboardStats() {
        DashboardStats stats = new DashboardStats();

        // 基础统计数据
        stats.setUserCount(userMapper.selectCount(null));
        stats.setRouteCount(routeMapper.selectCount(null));
        stats.setSpotCount(spotMapper.selectCount(null));

        // 路线主题分布
        stats.setThemeDistribution(getThemeDistribution());

        return Result.success(stats);
    }


    /**
     * 获取路线主题分布
     */
    private List<ThemeDistributionItem> getThemeDistribution() {
        List<Route> routes = routeMapper.selectList(null);
        Map<String, Long> themeCount = new HashMap<>();

        for (Route route : routes) {
            String themes = route.getThemes();
            if (themes != null && !themes.isEmpty()) {
                // 假设themes是JSON数组格式，如 ["历史文化","科技探索"]
                // 简单解析
                themes = themes.replace("[", "").replace("]", "").replace("\"", "");
                String[] themeArr = themes.split(",");
                for (String theme : themeArr) {
                    theme = theme.trim();
                    if (!theme.isEmpty()) {
                        themeCount.put(theme, themeCount.getOrDefault(theme, 0L) + 1);
                    }
                }
            }
        }

        List<ThemeDistributionItem> distribution = new ArrayList<>();
        for (Map.Entry<String, Long> entry : themeCount.entrySet()) {
            ThemeDistributionItem item = new ThemeDistributionItem();
            item.setName(entry.getKey());
            item.setValue(entry.getValue());
            distribution.add(item);
        }

        // 按数量排序
        distribution.sort((a, b) -> Long.compare(b.getValue(), a.getValue()));

        return distribution;
    }

    @Data
    public static class DashboardStats {
        private Long userCount;
        private Long routeCount;
        private Long spotCount;
        private List<ThemeDistributionItem> themeDistribution;
    }


    @Data
    public static class ThemeDistributionItem {
        private String name;
        private Long value;
    }
}
