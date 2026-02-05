package com.yanxue.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yanxue.entity.Achievement;
import com.yanxue.entity.UserFavorite;
import com.yanxue.mapper.AchievementMapper;
import com.yanxue.mapper.UserFavoriteMapper;
import com.yanxue.util.JwtUtil;
import com.yanxue.vo.Result;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserStatsController {

    private final JwtUtil jwtUtil;
    private final UserFavoriteMapper userFavoriteMapper;
    private final AchievementMapper achievementMapper;

    @GetMapping("/stats")
    public Result<UserStats> stats(@RequestHeader("Authorization") String authHeader) {
        Long userId = getUserId(authHeader);

        Long favoriteCount = userFavoriteMapper.selectCount(new QueryWrapper<UserFavorite>().eq("user_id", userId));
        Long achievementCount = achievementMapper.selectCount(new QueryWrapper<Achievement>().eq("user_id", userId));

        UserStats stats = new UserStats();
        stats.setFavoriteCount(favoriteCount);
        stats.setAchievementCount(achievementCount);
        return Result.success(stats);
    }

    private Long getUserId(String authHeader) {
        if (authHeader == null || authHeader.isEmpty()) {
            throw new IllegalArgumentException("缺少Authorization");
        }
        String token = authHeader.replace("Bearer ", "");
        return jwtUtil.getUserIdFromToken(token);
    }

    @Data
    public static class UserStats {
        private Long favoriteCount;
        private Long achievementCount;
    }
}
