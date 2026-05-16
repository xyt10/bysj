package com.yanxue.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yanxue.entity.Route;
import com.yanxue.entity.RouteSchedule;
import com.yanxue.mapper.RouteMapper;
import com.yanxue.mapper.RouteScheduleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 路线服务
 */
@Service
@RequiredArgsConstructor
public class RouteService {

    private final RouteMapper routeMapper;
    private final RouteScheduleMapper routeScheduleMapper;

    /**
     * 分页查询路线
     */
    public Page<Route> page(int pageNum, int pageSize, String keyword, String theme) {
        Page<Route> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Route> wrapper = new LambdaQueryWrapper<>();

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Route::getName, keyword)
                   .or()
                   .like(Route::getDescription, keyword);
        }
        if (theme != null && !theme.isEmpty()) {
            wrapper.like(Route::getThemes, theme);
        }

        wrapper.eq(Route::getStatus, 1)
               .orderByDesc(Route::getCreatedAt);

        return routeMapper.selectPage(page, wrapper);
    }

    /**
     * 根据ID获取路线详情
     */
    public Route getById(Long id) {
        Route route = routeMapper.selectById(id);
        if (route != null) {
            // 增加浏览量（原子更新，避免并发覆盖）
            UpdateWrapper<Route> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("id", id).setSql("view_count = COALESCE(view_count, 0) + 1");
            routeMapper.update(null, updateWrapper);
            route.setViewCount((route.getViewCount() == null ? 0 : route.getViewCount()) + 1);
        }
        return route;
    }

    /**
     * 获取路线行程
     */
    public List<RouteSchedule> getScheduleByRouteId(Long routeId) {
        LambdaQueryWrapper<RouteSchedule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RouteSchedule::getRouteId, routeId)
               .orderByAsc(RouteSchedule::getDayNum)
               .orderByAsc(RouteSchedule::getSortOrder);
        return routeScheduleMapper.selectList(wrapper);
    }

    /**
     * 创建路线
     */
    @Transactional
    public Route create(Route route) {
        route.setViewCount(0);
        route.setLikeCount(0);
        route.setStatus(1);
        routeMapper.insert(route);
        return route;
    }

    /**
     * 更新路线
     */
    public Route update(Route route) {
        routeMapper.updateById(route);
        return route;
    }

    /**
     * 删除路线
     */
    @Transactional
    public void delete(Long id) {
        routeMapper.deleteById(id);
        // 删除关联的行程
        LambdaQueryWrapper<RouteSchedule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RouteSchedule::getRouteId, id);
        routeScheduleMapper.delete(wrapper);
    }

    /**
     * 保存路线行程
     */
    @Transactional
    public void saveSchedule(Long routeId, List<RouteSchedule> schedules) {
        // 先删除原有行程
        LambdaQueryWrapper<RouteSchedule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RouteSchedule::getRouteId, routeId);
        routeScheduleMapper.delete(wrapper);

        // 保存新行程
        for (RouteSchedule schedule : schedules) {
            schedule.setRouteId(routeId);
            routeScheduleMapper.insert(schedule);
        }
    }

    /**
     * 获取热门路线
     */
    public List<Route> getHotRoutes(int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 50));
        LambdaQueryWrapper<Route> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Route::getStatus, 1)
               .orderByDesc(Route::getViewCount)
               .last("LIMIT " + safeLimit);
        return routeMapper.selectList(wrapper);
    }
}
