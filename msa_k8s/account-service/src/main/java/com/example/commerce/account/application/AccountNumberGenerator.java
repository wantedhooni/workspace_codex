package com.example.commerce.account.application;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * PostgreSQL sequence를 사용해 여러 Pod에서도 중복되지 않는 12자리 계좌번호를 발급한다.
 */
@Component
public class AccountNumberGenerator {

    private static final long MAX_SEQUENCE = 999_999_999L;
    private static final String ACCOUNT_NUMBER_PREFIX = "110";

    private final JdbcTemplate jdbcTemplate;

    /**
     * 계좌번호 sequence를 조회할 JDBC 도구를 주입받는다.
     *
     * @param jdbcTemplate PostgreSQL sequence 조회 도구
     */
    public AccountNumberGenerator(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * 은행 식별 접두사와 9자리 sequence를 결합해 계좌번호를 생성한다.
     *
     * @return 신규 계좌번호
     */
    public String generate() {
        Long sequence = jdbcTemplate.queryForObject(
                "select nextval('account.account_number_sequence')",
                Long.class);
        if (sequence == null || sequence > MAX_SEQUENCE) {
            throw new IllegalStateException("계좌번호 발급 범위를 초과했습니다.");
        }
        return ACCOUNT_NUMBER_PREFIX + "%09d".formatted(sequence);
    }
}
