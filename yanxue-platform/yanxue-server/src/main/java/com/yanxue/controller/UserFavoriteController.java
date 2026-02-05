package com.yanxue.controller;

import com.yanxue.entity.UserFavorite;
import com.yanxue.service.UserFavoriteService;
import com.yanxue.util.JwtUtil;
import com.yanxue.vo.Result;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserFavoriteController {

    private final JwtUtil jwtUtil;
    private final UserFavoriteService userFavoriteService;

    @GetMapping("/favorites")
    public Result<List<UserFavoriteService.FavoriteVO>> list(@RequestHeader("Authorization") String authHeader) {
        Long userId = getUserId(authHeader);
        return Result.success(userFavoriteService.listByUserId(userId));
    }

    @PostMapping("/favorite")
    public Result<UserFavorite> add(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody AddFavoriteRequest request) {
        Long userId = getUserId(authHeader);
        if (request.getRouteId() == null) {
            return Result.error("routeId不能为空");
        }
        return Result.success(userFavoriteService.add(userId, request.getRouteId()));
    }

    @DeleteMapping("/favorite/{id}")
    public Result<Boolean> remove(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id) {
        Long userId = getUserId(authHeader);
        boolean ok = userFavoriteService.remove(userId, id);
        return ok ? Result.success(true) : Result.error("删除失败");
    }

    private Long getUserId(String authHeader) {
        if (authHeader == null || authHeader.isEmpty()) {
            throw new IllegalArgumentException("缺少Authorization");
        }
        String token = authHeader.replace("Bearer ", "");
        return jwtUtil.getUserIdFromToken(token);
    }

    @Data
    public static class AddFavoriteRequest {
        private Long routeId;
    }
}
