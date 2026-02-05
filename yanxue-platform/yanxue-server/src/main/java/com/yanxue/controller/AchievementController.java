package com.yanxue.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yanxue.entity.Achievement;
import com.yanxue.service.AchievementService;
import com.yanxue.vo.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 研学成果控制器
 */
@RestController
@RequestMapping("/api/achievement")
@RequiredArgsConstructor
public class AchievementController {

    private final AchievementService achievementService;

    /**
     * 分页查询成果列表
     */
    @GetMapping("/list")
    public Result<Page<Achievement>> list(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String keyword) {
        return Result.success(achievementService.page(pageNum, pageSize, keyword));
    }

    /**
     * 获取成果详情
     */
    @GetMapping("/{id}")
    public Result<Achievement> getById(@PathVariable Long id) {
        Achievement achievement = achievementService.getById(id);
        if (achievement == null) {
            return Result.error("成果不存在");
        }
        return Result.success(achievement);
    }

    /**
     * 点赞/取消点赞
     */
    @PostMapping("/{id}/like")
    public Result<Void> toggleLike(@PathVariable Long id, @RequestBody Map<String, Boolean> params) {
        Boolean like = params.get("like");
        if (like == null) {
            return Result.error("参数错误");
        }
        achievementService.toggleLike(id, like);
        return Result.success();
    }

    /**
     * 创建成果
     */
    @PostMapping
    public Result<Achievement> create(@RequestBody Achievement achievement) {
        return Result.success(achievementService.create(achievement));
    }

    /**
     * 更新成果
     */
    @PutMapping("/{id}")
    public Result<Achievement> update(@PathVariable Long id, @RequestBody Achievement achievement) {
        achievement.setId(id);
        return Result.success(achievementService.update(achievement));
    }

    /**
     * 删除成果
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        achievementService.delete(id);
        return Result.success();
    }

    /**
     * 获取用户的成果列表
     */
    @GetMapping("/user/{userId}")
    public Result<List<Achievement>> getByUserId(@PathVariable Long userId) {
        return Result.success(achievementService.getByUserId(userId));
    }
}
