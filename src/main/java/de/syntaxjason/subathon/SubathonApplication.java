package de.syntaxjason.subathon;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SubathonApplication {

    public static void main(String[] args) {
        SpringApplication.run(SubathonApplication.class, args);
    }
}
