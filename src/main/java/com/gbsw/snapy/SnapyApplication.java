package com.gbsw.snapy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SnapyApplication {

    public static void main(String[] args) {
        SpringApplication.run(SnapyApplication.class, args);
    }

}
