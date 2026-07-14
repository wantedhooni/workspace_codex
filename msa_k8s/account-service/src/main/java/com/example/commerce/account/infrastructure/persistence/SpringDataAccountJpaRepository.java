package com.example.commerce.account.infrastructure.persistence;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import jakarta.persistence.LockModeType;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * 계좌 엔티티의 기본 조회와 거래용 행 잠금 조회를 제공하는 Spring Data 저장소다.
 */
public interface SpringDataAccountJpaRepository extends JpaRepository<AccountEntity, UUID> {

    /**
     * 회원의 계좌를 개설 시각과 식별자 순으로 조회한다.
     *
     * @param ownerId 회원 식별자
     * @return 회원 계좌 엔티티 목록
     */
    List<AccountEntity> findAllByOwnerIdOrderByCreatedAtAscIdAsc(UUID ownerId);

    /**
     * 잔액 변경 동안 동일 계좌의 동시 수정을 직렬화하도록 행 잠금으로 조회한다.
     *
     * @param id 계좌 식별자
     * @return 잠긴 계좌 엔티티
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select account from AccountEntity account where account.id = :id")
    Optional<AccountEntity> findByIdForUpdate(@Param("id") UUID id);
}
