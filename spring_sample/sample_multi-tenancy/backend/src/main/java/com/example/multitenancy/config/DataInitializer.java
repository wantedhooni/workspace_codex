package com.example.multitenancy.config;

import com.example.multitenancy.domain.AppUser;
import com.example.multitenancy.domain.Project;
import com.example.multitenancy.domain.ProjectStatus;
import com.example.multitenancy.repository.AppUserRepository;
import com.example.multitenancy.repository.ProjectRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 데모 로그인 계정과 테넌트별 프로젝트 데이터를 초기화한다.
 */
@Component
public class DataInitializer implements ApplicationRunner {

    private final AppUserRepository appUserRepository;
    private final ProjectRepository projectRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(AppUserRepository appUserRepository, ProjectRepository projectRepository, PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.projectRepository = projectRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (appUserRepository.count() > 0L) {
            return;
        }

        seedUser("alpha", "alpha.admin", "Alpha 운영 관리자", "ADMIN");
        seedUser("alpha", "alpha.viewer", "Alpha 조회 사용자", "VIEWER");
        seedUser("beta", "beta.admin", "Beta 운영 관리자", "ADMIN");
        seedUser("beta", "beta.viewer", "Beta 조회 사용자", "VIEWER");

        seedProject("alpha", "정산 API 고도화", "결제 정산 배치와 관리자 API를 통합 정비한다.", "김다온", ProjectStatus.ACTIVE);
        seedProject("alpha", "가맹점 셀프 온보딩", "신규 가맹점 가입 절차를 셀프 서비스로 전환한다.", "이수민", ProjectStatus.DISCOVERY);
        seedProject("beta", "병원 예약 대시보드", "운영실에서 진료 예약 현황을 실시간으로 확인한다.", "박선우", ProjectStatus.ACTIVE);
        seedProject("beta", "문진표 모바일 개편", "모바일 사용자 경험 개선과 다국어 지원을 포함한다.", "정하율", ProjectStatus.ON_HOLD);
    }

    private void seedUser(String tenantId, String username, String displayName, String role) {
        AppUser appUser = new AppUser();
        appUser.setTenantId(tenantId);
        appUser.setUsername(username);
        appUser.setDisplayName(displayName);
        appUser.setRole(role);
        appUser.setPasswordHash(passwordEncoder.encode("demo1234"));
        appUserRepository.save(appUser);
    }

    private void seedProject(String tenantId, String name, String description, String ownerName, ProjectStatus status) {
        Project project = new Project();
        project.setTenantId(tenantId);
        project.setName(name);
        project.setDescription(description);
        project.setOwnerName(ownerName);
        project.setStatus(status);
        projectRepository.save(project);
    }
}
