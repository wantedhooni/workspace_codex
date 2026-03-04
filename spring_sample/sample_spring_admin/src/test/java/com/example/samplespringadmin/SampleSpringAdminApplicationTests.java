package com.example.samplespringadmin;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.samplespringadmin.ops.AdminChecklistController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SampleSpringAdminApplicationTests {

    @Autowired
    private AdminChecklistController controller;

    @Test
    void contextLoads() {
        assertThat(controller.checklists()).isNotEmpty();
    }
}
