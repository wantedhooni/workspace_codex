package com.example.commerce.account.api;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.commerce.account.application.AccountService;

/**
 * 금융 계좌 개설, 조회, 입출금, 원장 조회 HTTP API를 제공하는 컨트롤러다.
 */
@Validated
@RestController
@RequestMapping("/api/v1/accounts")
public class AccountController {

    private final AccountService accountService;

    /**
     * 계좌 애플리케이션 서비스를 주입받아 컨트롤러를 생성한다.
     *
     * @param accountService 계좌 애플리케이션 서비스
     */
    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    /**
     * 회원 소유의 신규 금융 계좌를 개설한다.
     *
     * @param request 계좌 개설 요청
     * @return 생성 위치와 계좌 정보
     */
    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(@Valid @RequestBody CreateAccountRequest request) {
        AccountResponse response =
                AccountResponse.from(accountService.createAccount(request.ownerId(), request.currency()));
        return ResponseEntity.created(URI.create("/api/v1/accounts/" + response.id())).body(response);
    }

    /**
     * 식별자로 단일 계좌를 조회한다.
     *
     * @param id 계좌 식별자
     * @return 계좌 정보
     */
    @GetMapping("/{id}")
    public AccountResponse getAccount(@PathVariable UUID id) {
        return AccountResponse.from(accountService.getAccount(id));
    }

    /**
     * 회원이 보유한 모든 계좌를 조회한다.
     *
     * @param ownerId 회원 식별자
     * @return 회원 계좌 목록
     */
    @GetMapping
    public List<AccountResponse> getAccounts(@RequestParam UUID ownerId) {
        return accountService.getAccounts(ownerId).stream().map(AccountResponse::from).toList();
    }

    /**
     * 멱등 키를 사용해 계좌 입금을 처리한다.
     *
     * @param id 계좌 식별자
     * @param idempotencyKey 중복 방지 키
     * @param request 입금 요청
     * @return 입금 원장 항목
     */
    @PostMapping("/{id}/deposits")
    public AccountTransactionResponse deposit(
            @PathVariable UUID id,
            @RequestHeader("Idempotency-Key")
            @NotBlank(message = "Idempotency-Key 헤더는 필수입니다.")
            @Size(max = 100, message = "Idempotency-Key는 100자 이하여야 합니다.")
            String idempotencyKey,
            @Valid @RequestBody BalanceChangeRequest request) {
        return AccountTransactionResponse.from(
                accountService.deposit(id, request.amount(), request.memo(), idempotencyKey));
    }

    /**
     * 잔액을 확인하고 멱등 키를 사용해 계좌 출금을 처리한다.
     *
     * @param id 계좌 식별자
     * @param idempotencyKey 중복 방지 키
     * @param request 출금 요청
     * @return 출금 원장 항목
     */
    @PostMapping("/{id}/withdrawals")
    public AccountTransactionResponse withdraw(
            @PathVariable UUID id,
            @RequestHeader("Idempotency-Key")
            @NotBlank(message = "Idempotency-Key 헤더는 필수입니다.")
            @Size(max = 100, message = "Idempotency-Key는 100자 이하여야 합니다.")
            String idempotencyKey,
            @Valid @RequestBody BalanceChangeRequest request) {
        return AccountTransactionResponse.from(
                accountService.withdraw(id, request.amount(), request.memo(), idempotencyKey));
    }

    /**
     * 계좌의 최근 거래 원장을 최신순으로 조회한다.
     *
     * @param id 계좌 식별자
     * @param limit 최대 조회 수
     * @return 최근 거래 원장
     */
    @GetMapping("/{id}/transactions")
    public List<AccountTransactionResponse> getTransactions(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "20")
            @Min(value = 1, message = "조회 크기는 1 이상이어야 합니다.")
            @Max(value = 100, message = "조회 크기는 100 이하여야 합니다.")
            int limit) {
        return accountService.getTransactions(id, limit).stream()
                .map(AccountTransactionResponse::from)
                .toList();
    }
}
