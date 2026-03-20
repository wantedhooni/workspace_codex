package com.example.observability.config;

import com.example.observability.domain.AppUser;
import com.example.observability.domain.CustomerOrder;
import com.example.observability.domain.OrderStatus;
import com.example.observability.domain.UserRole;
import com.example.observability.repository.AppUserRepository;
import com.example.observability.repository.CustomerOrderRepository;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 실행 직후 데모 계정과 운영 주문 데이터를 적재한다.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final AppUserRepository appUserRepository;
    private final CustomerOrderRepository customerOrderRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(
            AppUserRepository appUserRepository,
            CustomerOrderRepository customerOrderRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.appUserRepository = appUserRepository;
        this.customerOrderRepository = customerOrderRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (appUserRepository.count() == 0L) {
            seedUsers();
        }
        if (customerOrderRepository.count() == 0L) {
            seedOrders();
        }
    }

    private void seedUsers() {
        appUserRepository.saveAll(List.of(
                new AppUser("ops", "ops.admin", passwordEncoder.encode("demo1234"), "운영 관리자", UserRole.ADMIN),
                new AppUser("ops", "ops.viewer", passwordEncoder.encode("demo1234"), "운영 조회 사용자", UserRole.VIEWER),
                new AppUser("biz", "biz.admin", passwordEncoder.encode("demo1234"), "사업 관리자", UserRole.ADMIN),
                new AppUser("biz", "biz.viewer", passwordEncoder.encode("demo1234"), "사업 조회 사용자", UserRole.VIEWER)
        ));
    }

    private void seedOrders() {
        OffsetDateTime now = OffsetDateTime.now();
        customerOrderRepository.saveAll(List.of(
                new CustomerOrder("ops", "OBS-1001", "Edge Gateway 장애 대응 패키지", "김운영", OrderStatus.PENDING, "HIGH",
                        new BigDecimal("1800000"), now.minusHours(4), now.plusHours(12)),
                new CustomerOrder("ops", "OBS-1002", "Prometheus 룰 정비 요청", "박민지", OrderStatus.IN_PROGRESS, "MEDIUM",
                        new BigDecimal("940000"), now.minusHours(2), now.plusHours(18)),
                new CustomerOrder("ops", "OBS-1003", "로그 샘플링 정책 점검", "정소율", OrderStatus.COMPLETED, "LOW",
                        new BigDecimal("420000"), now.minusDays(1), now.minusHours(3)),
                new CustomerOrder("biz", "OBS-2001", "VIP 고객 SLA 대시보드", "이서준", OrderStatus.PENDING, "HIGH",
                        new BigDecimal("2300000"), now.minusHours(1), now.plusHours(24)),
                new CustomerOrder("biz", "OBS-2002", "매출 API 알림 튜닝", "최다온", OrderStatus.IN_PROGRESS, "MEDIUM",
                        new BigDecimal("1150000"), now.minusHours(6), now.plusHours(8))
        ));
    }
}
