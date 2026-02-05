package com.yanxue.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yanxue.entity.Spot;
import com.yanxue.service.SpotService;
import com.yanxue.vo.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 研学点位控制器
 */
@RestController
@RequestMapping("/api/spot")
@RequiredArgsConstructor
public class SpotController {

    private final SpotService spotService;

    /**
     * 分页查询点位
     */
    @GetMapping("/page")
    public Result<Page<Spot>> page(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String type) {
        return Result.success(spotService.page(pageNum, pageSize, keyword, city, type));
    }

    /**
     * 获取点位详情
     */
    @GetMapping("/{id}")
    public Result<Spot> getById(@PathVariable Long id) {
        Spot spot = spotService.getById(id);
        if (spot == null) {
            return Result.error("点位不存在");
        }
        return Result.success(spot);
    }

    /**
     * 根据城市和主题获取点位
     */
    @GetMapping("/list")
    public Result<List<Spot>> list(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String theme) {
        return Result.success(spotService.getByCityAndTheme(city, theme));
    }

    /**
     * 创建点位（管理端）
     */
    @PostMapping
    public Result<Spot> create(@RequestBody Spot spot) {
        return Result.success(spotService.create(spot));
    }

    /**
     * 更新点位（管理端）
     */
    @PutMapping("/{id}")
    public Result<Spot> update(@PathVariable Long id, @RequestBody Spot spot) {
        spot.setId(id);
        return Result.success(spotService.update(spot));
    }

    /**
     * 删除点位（管理端）
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        spotService.delete(id);
        return Result.success();
    }
}
