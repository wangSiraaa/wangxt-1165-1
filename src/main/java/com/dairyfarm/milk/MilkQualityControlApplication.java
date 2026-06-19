package com.dairyfarm.milk;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.dairyfarm.milk.mapper")
@EnableScheduling
public class MilkQualityControlApplication {

    public static void main(String[] args) {
        SpringApplication.run(MilkQualityControlApplication.class, args);
    }
}
