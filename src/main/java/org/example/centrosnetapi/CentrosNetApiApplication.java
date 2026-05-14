package org.example.centrosnetapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableScheduling
@EnableJpaAuditing
@org.springframework.scheduling.annotation.EnableAsync
public class CentrosNetApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(CentrosNetApiApplication.class, args);
    }

}

