package com.habitlink;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.habitlink.mapper")
@SpringBootApplication
public class HabitLinkApplication {

    public static void main(String[] args) {
        SpringApplication.run(HabitLinkApplication.class, args);
    }
}
