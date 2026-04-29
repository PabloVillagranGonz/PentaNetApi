package org.example.centrosnetapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CentrosNetApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(CentrosNetApiApplication.class, args);
    }

}

