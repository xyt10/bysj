package com.yanxue;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 研学旅行平台启动类
 */
@SpringBootApplication
@MapperScan("com.yanxue.mapper")
public class YanxueApplication {

    public static void main(String[] args) {
        SpringApplication.run(YanxueApplication.class, args);
        System.out.println("====================================");
        System.out.println("  研学旅行平台后端服务启动成功!");
        System.out.println("  访问地址: http://localhost:8080");
        System.out.println("====================================");
    }
}
