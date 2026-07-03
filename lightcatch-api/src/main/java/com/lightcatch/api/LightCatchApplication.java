package com.lightcatch.api;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.lightcatch")
@MapperScan({"com.lightcatch.**.mapper"})
public class LightCatchApplication {
    public static void main(String[] args) {
        SpringApplication.run(LightCatchApplication.class, args);
        System.out.println("========================================");
        System.out.println("  LightCatch 拾光 - AI 内容工坊 启动成功!");
        System.out.println("  http://localhost:8080");
        System.out.println("========================================");
    }
}
