package com.commerce.service_auth.config;

import com.commerce.service_auth.user.Role;
import com.commerce.service_auth.user.User;
import com.commerce.service_auth.user.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
public class SeedDataConfig {

    @Bean
    CommandLineRunner seedUsers(UserRepository users) {
        return args -> {
            if (users.count() == 0) {
                BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
                users.save(new User("admin", encoder.encode("admin1234"), Role.ADMIN, "aurora-global"));
                users.save(new User("operator", encoder.encode("operator1234"), Role.OPERATOR, "aurora-global"));
                users.save(new User("customer", encoder.encode("customer1234"), Role.CUSTOMER, "aurora-global"));
            }
        };
    }
}
