package com.yanxue.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/**
 * 路线行程实体类
 */
@Data
@TableName("route_schedule")
public class RouteSchedule {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 路线ID
     */
    private Long routeId;

    /**
     * 第几天
     */
    private Integer dayNum;

    /**
     * 点位ID
     */
    private Long spotId;

    /**
     * 当天顺序
     */
    private Integer sortOrder;

    /**
     * 开始时间
     */
    private String startTime;

    /**
     * 结束时间
     */
    private String endTime;

    /**
     * 活动安排
     */
    private String activities;

    /**
     * 注意事项
     */
    private String tips;
}
