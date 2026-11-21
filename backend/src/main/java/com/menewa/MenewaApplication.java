package com.menewa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.modulith.Modulith;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@Modulith
@EnableScheduling // tâches planifiées des modules (ménage identity, et plus tard relances d'échéances)
public class MenewaApplication {
    public static void main(String[] args) {
        SpringApplication.run(MenewaApplication.class, args);
    }
}
