package com.yanxue.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yanxue.entity.Route;
import com.yanxue.entity.RouteSchedule;
import com.yanxue.service.RouteService;
import com.yanxue.vo.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 路线控制器
 */
@RestController
@RequestMapping("/api/route")
@RequiredArgsConstructor
public class RouteController {

    private final RouteService routeService;

    /**
     * 分页查询路线
     */
    @GetMapping("/page")
    public Result<Page<Route>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String theme) {
        return Result.success(routeService.page(pageNum, pageSize, keyword, theme));
    }

    /**
     * 获取路线详情
     */
    @GetMapping("/{id}")
    public Result<Map<String, Object>> getById(@PathVariable Long id) {
        Route route = routeService.getById(id);
        if (route == null) {
            return Result.error("路线不存在");
        }

        List<RouteSchedule> schedules = routeService.getScheduleByRouteId(id);

        Map<String, Object> data = new HashMap<>();
        data.put("route", route);
        data.put("schedules", schedules);

        return Result.success(data);
    }

    /**
     * 获取热门路线
     */
    @GetMapping("/hot")
    public Result<List<Route>> getHotRoutes(@RequestParam(defaultValue = "6") int limit) {
        return Result.success(routeService.getHotRoutes(limit));
    }

    /**
     * 创建路线（管理端）
     */
    @PostMapping
    public Result<Route> create(@RequestBody Route route) {
        return Result.success(routeService.create(route));
    }

    /**
     * 更新路线（管理端）
     */
    @PutMapping("/{id}")
    public Result<Route> update(@PathVariable Long id, @RequestBody Route route) {
        route.setId(id);
        return Result.success(routeService.update(route));
    }

    /**
     * 删除路线（管理端）
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        routeService.delete(id);
        return Result.success();
    }

    /**
     * 保存路线行程
     */
    @PostMapping("/{id}/schedule")
    public Result<Void> saveSchedule(@PathVariable Long id, @RequestBody List<RouteSchedule> schedules) {
        routeService.saveSchedule(id, schedules);
        return Result.success();
    }
}
