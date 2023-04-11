package ru.skillbox.diplom.group35.microservice.account;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import ru.skillbox.diplom.group35.library.core.annotation.EnableBaseRepository;
import ru.skillbox.diplom.group35.library.core.annotation.EnableOpenFeign;
import ru.skillbox.diplom.group35.library.core.annotation.EnableSecurity;

/**
 * Application
 *
 * @author Denis_Kholmogorov
 */

@EnableSecurity
@EnableOpenFeign
@EnableBaseRepository
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}