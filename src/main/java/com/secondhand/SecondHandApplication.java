package com.secondhand;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.secondhand.repository")
@EnableScheduling
public class SecondHandApplication {
    public static void main(String[] args) {
        SpringApplication.run(SecondHandApplication.class, args);
    }
} 