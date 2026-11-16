package com.menewa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.modulith.Modulith;

@SpringBootApplication
@Modulith
public class MenewaApplication {
    public static void main(String[] args) {
        SpringApplication.run(MenewaApplication.class, args);
    }
}
