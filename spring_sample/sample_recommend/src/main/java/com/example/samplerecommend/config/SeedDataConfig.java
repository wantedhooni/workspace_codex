package com.example.samplerecommend.config;

import com.example.samplerecommend.domain.CustomerAction;
import com.example.samplerecommend.domain.CustomerProfile;
import com.example.samplerecommend.domain.Product;
import com.example.samplerecommend.repository.CustomerActionRepository;
import com.example.samplerecommend.repository.CustomerProfileRepository;
import com.example.samplerecommend.repository.ProductRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 데모 실행에 필요한 샘플 데이터를 초기 적재한다.
 */
@Configuration
public class SeedDataConfig {

    @Bean
    public CommandLineRunner seedData(ProductRepository productRepository,
                                      CustomerProfileRepository customerProfileRepository,
                                      CustomerActionRepository customerActionRepository) {
        return args -> {
            if (productRepository.count() > 0) {
                return;
            }

            productRepository.save(new Product("P-1001", "노이즈 캔슬링 헤드폰", "AUDIO", new BigDecimal("249000"), 18, 82.5));
            productRepository.save(new Product("P-1002", "무선 블루투스 스피커", "AUDIO", new BigDecimal("119000"), 14, 75.0));
            productRepository.save(new Product("P-2001", "인체공학 기계식 키보드", "OFFICE", new BigDecimal("189000"), 7, 88.0));
            productRepository.save(new Product("P-2002", "4K 울트라 모니터", "OFFICE", new BigDecimal("459000"), 4, 91.0));
            productRepository.save(new Product("P-3001", "스마트 워치 프로", "WEARABLE", new BigDecimal("329000"), 11, 86.0));
            productRepository.save(new Product("P-3002", "피트니스 밴드 라이트", "WEARABLE", new BigDecimal("89000"), 25, 63.0));

            customerProfileRepository.save(new CustomerProfile("CUST-001", "AUDIO", new BigDecimal("300000"), "VIP"));
            customerProfileRepository.save(new CustomerProfile("CUST-002", "OFFICE", new BigDecimal("500000"), "BASIC"));
            customerProfileRepository.save(new CustomerProfile("CUST-003", "WEARABLE", new BigDecimal("150000"), "BASIC"));

            customerActionRepository.save(new CustomerAction("CUST-001", "P-1001", "VIEW", 3.0, LocalDateTime.now().minusHours(5)));
            customerActionRepository.save(new CustomerAction("CUST-001", "P-1002", "CART", 4.0, LocalDateTime.now().minusHours(2)));
            customerActionRepository.save(new CustomerAction("CUST-002", "P-2001", "PURCHASE", 5.0, LocalDateTime.now().minusDays(1)));
            customerActionRepository.save(new CustomerAction("CUST-003", "P-3002", "VIEW", 2.0, LocalDateTime.now().minusHours(6)));
        };
    }
}

