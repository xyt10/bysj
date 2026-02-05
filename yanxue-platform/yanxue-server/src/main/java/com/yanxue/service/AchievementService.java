package com.yanxue.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yanxue.entity.Achievement;
import com.yanxue.mapper.AchievementMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 研学成果服务
 */
@Service
@RequiredArgsConstructor
public class AchievementService {

    private final AchievementMapper achievementMapper;

    /**
     * 分页查询成果列表
     */
    public Page<Achievement> page(int pageNum, int pageSize, String keyword) {
        Page<Achievement> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Achievement> wrapper = new LambdaQueryWrapper<>();

        // 只查询已发布的成果
        wrapper.eq(Achievement::getStatus, 1);

        // 关键词搜索
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like(Achievement::getTitle, keyword)
                    .or()
                    .like(Achievement::getContent, keyword));
        }

        // 按创建时间倒序
        wrapper.orderByDesc(Achievement::getCreatedAt);

        return achievementMapper.selectPage(page, wrapper);
    }

    /**
     * 根据ID获取成果详情
     */
    public Achievement getById(Long id) {
        Achievement achievement = achievementMapper.selectById(id);
        if (achievement != null) {
            // 增加浏览量
            achievement.setViewCount(achievement.getViewCount() + 1);
            achievementMapper.updateById(achievement);
        }
        return achievement;
    }

    /**
     * 点赞/取消点赞
     */
    public void toggleLike(Long id, boolean like) {
        Achievement achievement = achievementMapper.selectById(id);
        if (achievement != null) {
            int likeCount = achievement.getLikeCount();
            if (like) {
                achievement.setLikeCount(likeCount + 1);
            } else {
                achievement.setLikeCount(Math.max(0, likeCount - 1));
            }
            achievementMapper.updateById(achievement);
        }
    }

    /**
     * 创建成果
     */
    public Achievement create(Achievement achievement) {
        // 初始化数据
        if (achievement.getLikeCount() == null) {
            achievement.setLikeCount(0);
        }
        if (achievement.getViewCount() == null) {
            achievement.setViewCount(0);
        }
        if (achievement.getStatus() == null) {
            achievement.setStatus(1); // 默认已发布
        }

        achievementMapper.insert(achievement);
        return achievement;
    }

    /**
     * 更新成果
     */
    public Achievement update(Achievement achievement) {
        achievementMapper.updateById(achievement);
        return achievement;
    }

    /**
     * 删除成果
     */
    public void delete(Long id) {
        achievementMapper.deleteById(id);
    }

    /**
     * 获取用户的成果列表
     */
    public List<Achievement> getByUserId(Long userId) {
        LambdaQueryWrapper<Achievement> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Achievement::getUserId, userId)
                .orderByDesc(Achievement::getCreatedAt);
        return achievementMapper.selectList(wrapper);
    }
}
