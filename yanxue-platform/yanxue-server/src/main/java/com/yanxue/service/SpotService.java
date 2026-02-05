package com.yanxue.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yanxue.entity.Spot;
import com.yanxue.mapper.SpotMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 研学点位服务
 */
@Service
@RequiredArgsConstructor
public class SpotService {

    private final SpotMapper spotMapper;

    /**
     * 分页查询点位
     */
    public Page<Spot> page(int pageNum, int pageSize, String keyword, String city, String type) {
        Page<Spot> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Spot> wrapper = new LambdaQueryWrapper<>();

        if (keyword != null && !keyword.isEmpty()) {
            wrapper.like(Spot::getName, keyword)
                   .or()
                   .like(Spot::getDescription, keyword);
        }
        if (city != null && !city.isEmpty()) {
            wrapper.eq(Spot::getCity, city);
        }
        if (type != null && !type.isEmpty()) {
            wrapper.eq(Spot::getType, type);
        }

        wrapper.eq(Spot::getStatus, 1)
               .orderByDesc(Spot::getCreatedAt);

        return spotMapper.selectPage(page, wrapper);
    }

    /**
     * 根据ID获取点位
     */
    public Spot getById(Long id) {
        return spotMapper.selectById(id);
    }

    /**
     * 根据城市获取点位
     */
    public List<Spot> getByCityAndTheme(String city, String theme) {
        LambdaQueryWrapper<Spot> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Spot::getStatus, 1);

        if (city != null && !city.isEmpty()) {
            wrapper.eq(Spot::getCity, city);
        }
        if (theme != null && !theme.isEmpty()) {
            wrapper.like(Spot::getThemes, theme);
        }

        return spotMapper.selectList(wrapper);
    }

    /**
     * 创建点位
     */
    public Spot create(Spot spot) {
        spot.setStatus(1);
        spotMapper.insert(spot);
        return spot;
    }

    /**
     * 更新点位
     */
    public Spot update(Spot spot) {
        spotMapper.updateById(spot);
        return spot;
    }

    /**
     * 删除点位
     */
    public void delete(Long id) {
        spotMapper.deleteById(id);
    }

    /**
     * 获取所有点位（用于AI）
     */
    public List<Spot> getAllActive() {
        LambdaQueryWrapper<Spot> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Spot::getStatus, 1);
        return spotMapper.selectList(wrapper);
    }
}
