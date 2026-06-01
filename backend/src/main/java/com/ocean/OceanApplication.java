package com.ocean;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 南海海洋环境数据可视化系统
 */
@SpringBootApplication
@MapperScan("com.ocean.mapper")
@EnableAsync
public class OceanApplication {

    public static void main(String[] args) {
        SpringApplication.run(OceanApplication.class, args);
    }
}
