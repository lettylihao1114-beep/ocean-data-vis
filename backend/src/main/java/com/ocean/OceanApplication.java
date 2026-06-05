package com.ocean;

import io.github.cdimascio.dotenv.Dotenv;
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
        // 自动加载 .env 文件到系统属性（不存在则跳过，生产环境使用环境变量）
        Dotenv dotenv = Dotenv.configure().ignoreIfMissing().load();
        dotenv.entries().forEach(e -> {
            String key = e.getKey();
            // 仅在系统属性未设置时从 .env 加载，环境变量优先级更高
            if (System.getProperty(key) == null) {
                System.setProperty(key, e.getValue());
            }
        });

        SpringApplication.run(OceanApplication.class, args);
    }
}
