package com.rgs.bamboonotifier;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RgsBambooNotifierApplication {
    public static void main(String[] args) {
        SpringApplication.run(RgsBambooNotifierApplication.class, args);
    }
}
