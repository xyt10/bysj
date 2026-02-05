package com.yanxue.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.yanxue.entity.Route;
import com.yanxue.entity.UserFavorite;
import com.yanxue.mapper.RouteMapper;
import com.yanxue.mapper.UserFavoriteMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserFavoriteService {

    private final UserFavoriteMapper userFavoriteMapper;
    private final RouteMapper routeMapper;

    public List<FavoriteVO> listByUserId(Long userId) {
        QueryWrapper<UserFavorite> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).orderByDesc("created_at");
        List<UserFavorite> favorites = userFavoriteMapper.selectList(wrapper);

        return favorites.stream().map(f -> {
            Route route = routeMapper.selectById(f.getRouteId());
            FavoriteVO vo = new FavoriteVO();
            vo.setId(f.getId());
            vo.setUserId(f.getUserId());
            vo.setRouteId(f.getRouteId());
            vo.setCreatedAt(f.getCreatedAt());

            if (route != null) {
                vo.setRouteName(route.getName());
                vo.setRouteDays(route.getDays());
                vo.setRoutePrice(route.getPrice() != null ? route.getPrice().doubleValue() : null);
                vo.setCoverImage(route.getCoverImage());
            }
            return vo;
        }).collect(Collectors.toList());
    }

    public UserFavorite add(Long userId, Long routeId) {
        QueryWrapper<UserFavorite> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).eq("route_id", routeId);
        UserFavorite existing = userFavoriteMapper.selectOne(wrapper);
        if (existing != null) {
            return existing;
        }

        UserFavorite favorite = new UserFavorite();
        favorite.setUserId(userId);
        favorite.setRouteId(routeId);
        if (favorite.getCreatedAt() == null) {
            favorite.setCreatedAt(LocalDateTime.now());
        }

        userFavoriteMapper.insert(favorite);
        return favorite;
    }

    public boolean remove(Long userId, Long favoriteId) {
        UserFavorite favorite = userFavoriteMapper.selectById(favoriteId);
        if (favorite == null) {
            return false;
        }
        if (!Objects.equals(favorite.getUserId(), userId)) {
            return false;
        }
        return userFavoriteMapper.deleteById(favoriteId) > 0;
    }

    @Data
    public static class FavoriteVO {
        private Long id;
        private Long userId;
        private Long routeId;
        private String routeName;
        private Integer routeDays;
        private Double routePrice;
        private String coverImage;
        private LocalDateTime createdAt;
    }
}
