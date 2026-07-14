package com.example.commerce.account.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 계좌 거래 원장의 멱등 키 조회와 최근 원장 조회를 제공하는 Spring Data 저장소다.
 */
public interface SpringDataAccountTransactionJpaRepository
        extends JpaRepository<AccountTransactionEntity, UUID> {

    /**
     * 멱등 키로 기존 거래 원장을 조회한다.
     *
     * @param idempotencyKey 요청 멱등 키
     * @return 기존 거래 원장
     */
    Optional<AccountTransactionEntity> findByIdempotencyKey(String idempotencyKey);

    /**
     * 계좌 원장을 최신순으로 제한 조회한다.
     *
     * @param accountId 계좌 식별자
     * @param pageable 조회 크기
     * @return 최근 거래 원장
     */
    List<AccountTransactionEntity> findByAccountIdOrderByCreatedAtDescIdDesc(
            UUID accountId,
            Pageable pageable);
}
