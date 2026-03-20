package com.aiboot.ecommerce;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.aiboot.ecommerce.mapper")
@SpringBootApplication
public class AiBootApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiBootApplication.class, args);
    }
}
