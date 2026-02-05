-- 研学旅行平台数据库初始化脚本
-- 创建数据库
CREATE DATABASE IF NOT EXISTS yanxue DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE yanxue;

-- 1. 用户表
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `openid` VARCHAR(64) UNIQUE COMMENT '微信openid',
    `username` VARCHAR(50) UNIQUE COMMENT '用户名',
    `password` VARCHAR(100) COMMENT '密码(BCrypt加密)',
    `nickname` VARCHAR(50) COMMENT '昵称',
    `avatar` VARCHAR(255) COMMENT '头像',
    `phone` VARCHAR(20) COMMENT '手机号',
    `email` VARCHAR(100) COMMENT '邮箱',
    `login_type` VARCHAR(20) DEFAULT 'password' COMMENT '登录方式: password/wechat/phone',
    `grade` VARCHAR(20) COMMENT '学段：小学/初中/高中',
    `school` VARCHAR(100) COMMENT '学校',
    `interests` JSON COMMENT '兴趣标签数组',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_username` (`username`),
    INDEX `idx_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 2. 研学点位表
DROP TABLE IF EXISTS `spot`;
CREATE TABLE `spot` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `name` VARCHAR(100) NOT NULL COMMENT '名称',
    `type` VARCHAR(50) COMMENT '类型：博物馆/科技馆/景区/基地',
    `province` VARCHAR(50) COMMENT '省份',
    `city` VARCHAR(50) COMMENT '城市',
    `address` VARCHAR(255) COMMENT '详细地址',
    `longitude` DECIMAL(10,7) COMMENT '经度',
    `latitude` DECIMAL(10,7) COMMENT '纬度',
    `description` TEXT COMMENT '简介',
    `open_time` VARCHAR(100) COMMENT '开放时间',
    `ticket_price` DECIMAL(10,2) COMMENT '门票价格',
    `suitable_grades` JSON COMMENT '适合学段',
    `themes` JSON COMMENT '主题标签',
    `knowledge_points` JSON COMMENT '关联知识点',
    `images` JSON COMMENT '图片列表',
    `status` TINYINT DEFAULT 1 COMMENT '状态：1启用 0禁用',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='研学点位表';

-- 3. 路线表
DROP TABLE IF EXISTS `route`;
CREATE TABLE `route` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `name` VARCHAR(100) NOT NULL COMMENT '路线名称',
    `description` TEXT COMMENT '路线描述',
    `days` INT COMMENT '天数',
    `price` DECIMAL(10,2) COMMENT '参考价格',
    `suitable_grades` JSON COMMENT '适合学段',
    `themes` JSON COMMENT '主题标签',
    `cover_image` VARCHAR(255) COMMENT '封面图片',
    `source` VARCHAR(20) DEFAULT 'manual' COMMENT '来源：manual/ai',
    `ai_prompt` TEXT COMMENT 'AI生成时的prompt',
    `view_count` INT DEFAULT 0 COMMENT '浏览量',
    `like_count` INT DEFAULT 0 COMMENT '点赞数',
    `status` TINYINT DEFAULT 1 COMMENT '状态：1启用 0禁用',
    `created_by` BIGINT COMMENT '创建者ID',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='路线表';

-- 4. 路线行程表
DROP TABLE IF EXISTS `route_schedule`;
CREATE TABLE `route_schedule` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `route_id` BIGINT NOT NULL COMMENT '路线ID',
    `day_num` INT COMMENT '第几天',
    `spot_id` BIGINT COMMENT '点位ID',
    `sort_order` INT COMMENT '当天顺序',
    `start_time` VARCHAR(10) COMMENT '开始时间',
    `end_time` VARCHAR(10) COMMENT '结束时间',
    `activities` TEXT COMMENT '活动安排',
    `tips` TEXT COMMENT '注意事项',
    KEY `idx_route_id` (`route_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='路线行程表';

-- 5. 订单表
DROP TABLE IF EXISTS `order`;
CREATE TABLE `order` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `order_no` VARCHAR(32) UNIQUE COMMENT '订单号',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `route_id` BIGINT NOT NULL COMMENT '路线ID',
    `travel_date` DATE COMMENT '出行日期',
    `people_count` INT COMMENT '人数',
    `total_price` DECIMAL(10,2) COMMENT '总价',
    `status` VARCHAR(20) DEFAULT 'pending' COMMENT '状态：pending/paid/completed/cancelled',
    `contact_name` VARCHAR(50) COMMENT '联系人姓名',
    `contact_phone` VARCHAR(20) COMMENT '联系人电话',
    `remark` TEXT COMMENT '备注',
    `paid_at` DATETIME COMMENT '支付时间',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    KEY `idx_user_id` (`user_id`),
    KEY `idx_route_id` (`route_id`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 6. 用户收藏表
DROP TABLE IF EXISTS `user_favorite`;
CREATE TABLE `user_favorite` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `route_id` BIGINT NOT NULL COMMENT '路线ID',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_user_route` (`user_id`, `route_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户收藏表';

-- 7. 研学成果表
DROP TABLE IF EXISTS `achievement`;
CREATE TABLE `achievement` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `route_id` BIGINT COMMENT '路线ID',
    `title` VARCHAR(100) COMMENT '标题',
    `content` TEXT COMMENT '内容',
    `images` JSON COMMENT '图片',
    `video_url` VARCHAR(255) COMMENT '视频地址',
    `like_count` INT DEFAULT 0 COMMENT '点赞数',
    `view_count` INT DEFAULT 0 COMMENT '浏览量',
    `status` TINYINT DEFAULT 1 COMMENT '状态',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='研学成果表';

-- 8. AI调用日志表
DROP TABLE IF EXISTS `ai_log`;
CREATE TABLE `ai_log` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `user_id` BIGINT COMMENT '用户ID',
    `type` VARCHAR(20) COMMENT '类型：recommend/generate',
    `model` VARCHAR(50) COMMENT '使用的模型',
    `prompt` TEXT COMMENT '请求prompt',
    `response` TEXT COMMENT '响应内容',
    `tokens_used` INT COMMENT '使用的token数',
    `cost` DECIMAL(10,4) COMMENT '费用',
    `duration_ms` INT COMMENT '耗时毫秒',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    KEY `idx_user_id` (`user_id`),
    KEY `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI调用日志表';

-- 9. 管理员表
DROP TABLE IF EXISTS `admin`;
CREATE TABLE `admin` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT,
    `username` VARCHAR(50) UNIQUE NOT NULL COMMENT '用户名',
    `password` VARCHAR(100) NOT NULL COMMENT '密码',
    `nickname` VARCHAR(50) COMMENT '昵称',
    `role` VARCHAR(20) DEFAULT 'admin' COMMENT '角色',
    `status` TINYINT DEFAULT 1 COMMENT '状态',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员表';

-- 插入默认管理员（密码: admin123）
INSERT INTO `admin` (`username`, `password`, `nickname`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iKUJ5FfLHJMNVNsEMXNz3Gp9jEmi', '超级管理员');
