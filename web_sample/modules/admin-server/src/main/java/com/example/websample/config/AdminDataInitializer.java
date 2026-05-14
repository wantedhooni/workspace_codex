package com.example.websample.config;

import com.example.websample.domain.admin.Admin;
import com.example.websample.domain.admin.AdminRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Admin 서버의 로컬 실행과 데모 테스트에 필요한 기본 데이터를 준비합니다. */
@Component
public class AdminDataInitializer implements ApplicationRunner {

    private static final String ADMIN_DEMO_EMAIL = "admin@example.com";
    private static final String ADMIN_DEMO_PASSWORD = "Admin1234!";

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminDataInitializer(AdminRepository adminRepository, PasswordEncoder passwordEncoder) {
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /** 데모 관리자가 없을 때 관리자 계정을 생성합니다. */
    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (adminRepository.existsByEmail(ADMIN_DEMO_EMAIL)) {
            return;
        }
        adminRepository.save(Admin.create(
                ADMIN_DEMO_EMAIL,
                passwordEncoder.encode(ADMIN_DEMO_PASSWORD),
                "관리자"
        ));
    }
}
