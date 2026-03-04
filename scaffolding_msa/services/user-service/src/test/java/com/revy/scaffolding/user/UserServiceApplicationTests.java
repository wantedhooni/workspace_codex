package com.revy.scaffolding.user;

import static org.assertj.core.api.Assertions.assertThat;

import com.revy.scaffolding.user.domain.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
    "eureka.client.enabled=false",
    "spring.cloud.discovery.enabled=false"
})
class UserServiceApplicationTests {
    @Autowired
    private UserRepository userRepository;

    @Test
    void loadsSampleUsers() {
        assertThat(userRepository.count()).isGreaterThanOrEqualTo(2);
    }
}

