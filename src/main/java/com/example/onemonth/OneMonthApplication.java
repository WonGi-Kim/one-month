package com.example.onemonth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class OneMonthApplication {

    public static void main(String[] args) {
        SpringApplication.run(OneMonthApplication.class, args);
    }

}
