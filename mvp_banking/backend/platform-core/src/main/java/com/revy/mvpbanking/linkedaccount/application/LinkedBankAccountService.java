package com.revy.mvpbanking.linkedaccount.application;

import com.revy.mvpbanking.audit.application.AuditLogService;
import com.revy.mvpbanking.audit.domain.AuditActionType;
import com.revy.mvpbanking.customer.domain.Customer;
import com.revy.mvpbanking.customer.domain.CustomerRepository;
import com.revy.mvpbanking.linkedaccount.domain.LinkedBankAccount;
import com.revy.mvpbanking.linkedaccount.domain.LinkedBankAccountBlockReasonCode;
import com.revy.mvpbanking.linkedaccount.domain.LinkedBankAccountRepository;
import com.revy.mvpbanking.linkedaccount.domain.LinkedBankAccountStatus;
import com.revy.mvpbanking.notification.application.NotificationService;
import com.revy.mvpbanking.notification.domain.NotificationCategory;
import com.revy.mvpbanking.notification.domain.NotificationSeverity;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class LinkedBankAccountService {

    private static final int MAX_VERIFICATION_ATTEMPTS = 5;

    private final LinkedBankAccountRepository linkedBankAccountRepository;
    private final CustomerRepository customerRepository;
    private final AuditLogService auditLogService;
    private final NotificationService notificationService;

    public LinkedBankAccountService(
            LinkedBankAccountRepository linkedBankAccountRepository,
            CustomerRepository customerRepository,
            AuditLogService auditLogService,
            NotificationService notificationService
    ) {
        this.linkedBankAccountRepository = linkedBankAccountRepository;
        this.customerRepository = customerRepository;
        this.auditLogService = auditLogService;
        this.notificationService = notificationService;
    }

    @Transactional(readOnly = true)
    public List<LinkedBankAccount> getAdminLinkedBankAccounts() {
        auditLogService.logCurrentActor(AuditActionType.LINKED_BANK_ACCOUNT_LIST_VIEWED, "LINKED_BANK_ACCOUNT", "all", "Viewed linked bank account list");
        Instant now = Instant.now();
        return linkedBankAccountRepository.findAllByOrderByCreatedAtDesc().stream()
                .sorted(adminQueueComparator(now))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LinkedBankAccount> getUserLinkedBankAccounts(UUID endUserId) {
        UUID customerId = customerRepository.findByEndUserId(endUserId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Customer not found"))
                .getId();
        return linkedBankAccountRepository.findByCustomerIdOrderByPrimaryWithdrawalDescCreatedAtDesc(customerId);
    }

    @Transactional
    public LinkedBankAccount create(
            UUID endUserId,
            String bankName,
            String accountAlias,
            String accountHolderName,
            String accountNumber,
            boolean primaryWithdrawal
    ) {
        Customer customer = customerRepository.findByEndUserId(endUserId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Customer not found"));

        linkedBankAccountRepository.findByCustomerIdAndAccountNumber(customer.getId(), accountNumber)
                .ifPresent(existing -> {
                    throw new ResponseStatusException(CONFLICT, "Linked bank account already exists");
                });

        List<LinkedBankAccount> existingAccounts = linkedBankAccountRepository.findByCustomerIdOrderByPrimaryWithdrawalDescCreatedAtDesc(customer.getId());
        boolean hasActivePrimary = existingAccounts.stream()
                .filter(account -> account.getStatus() == LinkedBankAccountStatus.ACTIVE)
                .anyMatch(LinkedBankAccount::isPrimaryWithdrawal);
        boolean requestedPrimary = primaryWithdrawal || !hasActivePrimary;
        String verificationReference = generateVerificationReference();

        LinkedBankAccount account = linkedBankAccountRepository.save(
                LinkedBankAccount.pendingVerification(
                        customer.getId(),
                        customer.getEmail(),
                        bankName.trim(),
                        accountAlias.trim(),
                        accountHolderName.trim(),
                        accountNumber.trim(),
                        requestedPrimary,
                        verificationReference
                )
        );

        auditLogService.logCurrentActor(
                AuditActionType.LINKED_BANK_ACCOUNT_CREATED,
                "LINKED_BANK_ACCOUNT",
                account.getId().toString(),
                "Created linked bank account " + account.getBankName() + " / " + account.getAccountAlias()
        );
        notificationService.notifyUser(
                "USER-LINKED-BANK-PENDING:" + account.getId(),
                customer.getEndUserId(),
                NotificationCategory.FUNDING,
                NotificationSeverity.INFO,
                "연결 계좌 등록 완료",
                account.getBankName() + " / " + account.getAccountAlias() + " 계좌가 검증 대기 상태로 등록되었습니다. 데모 인증 문구는 " + verificationReference + " 이며 10분 안에 입력해야 합니다.",
                "/linked-bank-accounts",
                "LINKED_BANK_ACCOUNT",
                account.getId()
        );
        notificationService.notifyActiveAdmins(
                "ADMIN-LINKED-BANK-PENDING:" + account.getId(),
                NotificationCategory.FUNDING,
                NotificationSeverity.ACTION_REQUIRED,
                "신규 연결 계좌 검증 대기",
                customer.getEmail() + " 사용자의 " + account.getBankName() + " / " + account.getAccountAlias() + " 계좌를 검증해 주세요.",
                "/linked-bank-accounts",
                "LINKED_BANK_ACCOUNT",
                account.getId()
        );
        return account;
    }

    @Transactional(noRollbackFor = ResponseStatusException.class)
    public LinkedBankAccount verify(UUID endUserId, UUID linkedBankAccountId, String verificationReference) {
        Customer customer = customerRepository.findByEndUserId(endUserId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Customer not found"));
        LinkedBankAccount target = getOwnedLinkedBankAccount(customer.getId(), linkedBankAccountId);

        if (target.getStatus() == LinkedBankAccountStatus.ACTIVE) {
            return target;
        }
        if (target.getStatus() == LinkedBankAccountStatus.BLOCKED) {
            throw new ResponseStatusException(CONFLICT, "Blocked linked bank account cannot be verified");
        }
        if (target.isVerificationExpired(Instant.now())) {
            throw new ResponseStatusException(CONFLICT, "Verification reference expired. Request a new verification code");
        }

        target.registerVerificationAttempt();
        String normalizedReference = normalizeVerificationReference(verificationReference);
        if (!target.matchesVerificationReference(normalizedReference)) {
            auditLogService.logCurrentActor(
                    AuditActionType.LINKED_BANK_ACCOUNT_VERIFICATION_FAILED,
                    "LINKED_BANK_ACCOUNT",
                    target.getId().toString(),
                    "Failed verification attempt for linked bank account " + target.getBankName() + " / " + target.getAccountAlias()
            );
            if (target.getVerificationAttemptCount() >= MAX_VERIFICATION_ATTEMPTS) {
                target.block(LinkedBankAccountBlockReasonCode.VERIFICATION_ATTEMPTS_EXCEEDED);
                notificationService.notifyUser(
                        "USER-LINKED-BANK-BLOCKED-VERIFY:" + target.getId(),
                        endUserId,
                        NotificationCategory.FUNDING,
                        NotificationSeverity.WARNING,
                        "연결 계좌 인증 실패",
                        target.getBankName() + " / " + target.getAccountAlias() + " 계좌가 인증 실패 누적으로 차단되었습니다. 운영팀에 문의해 주세요.",
                        "/linked-bank-accounts",
                        "LINKED_BANK_ACCOUNT",
                        target.getId()
                );
                throw new ResponseStatusException(CONFLICT, "Verification reference does not match. Linked bank account was blocked");
            }
            throw new ResponseStatusException(
                    CONFLICT,
                    "Verification reference does not match. Remaining attempts: " + (MAX_VERIFICATION_ATTEMPTS - target.getVerificationAttemptCount())
            );
        }

        activateInternal(target);
        notificationService.notifyUser(
                "USER-LINKED-BANK-VERIFIED:" + target.getId(),
                endUserId,
                NotificationCategory.FUNDING,
                NotificationSeverity.SUCCESS,
                "연결 계좌 인증 완료",
                target.getBankName() + " / " + target.getAccountAlias() + " 계좌 인증이 완료되어 출금 목적지로 사용할 수 있습니다.",
                "/linked-bank-accounts",
                "LINKED_BANK_ACCOUNT",
                target.getId()
        );
        auditLogService.logCurrentActor(
                AuditActionType.LINKED_BANK_ACCOUNT_VERIFIED,
                "LINKED_BANK_ACCOUNT",
                target.getId().toString(),
                "Verified linked bank account " + target.getBankName() + " / " + target.getAccountAlias()
        );
        return target;
    }

    @Transactional
    public LinkedBankAccount resendVerification(UUID endUserId, UUID linkedBankAccountId) {
        Customer customer = customerRepository.findByEndUserId(endUserId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Customer not found"));
        LinkedBankAccount target = getOwnedLinkedBankAccount(customer.getId(), linkedBankAccountId);

        if (target.getStatus() == LinkedBankAccountStatus.ACTIVE) {
            throw new ResponseStatusException(CONFLICT, "Active linked bank account does not require verification resend");
        }
        if (target.getStatus() == LinkedBankAccountStatus.BLOCKED) {
            throw new ResponseStatusException(CONFLICT, "Blocked linked bank account cannot receive verification resend");
        }
        Instant now = Instant.now();
        if (!target.isVerificationResendAllowed(now)) {
            throw new ResponseStatusException(
                    CONFLICT,
                    "Verification resend is cooling down until " + target.getVerificationResendAvailableAt()
            );
        }

        String newVerificationReference = generateVerificationReference();
        target.refreshVerificationChallenge(newVerificationReference);

        notificationService.notifyUser(
                "USER-LINKED-BANK-RESENT:" + target.getId() + ":" + newVerificationReference,
                endUserId,
                NotificationCategory.FUNDING,
                NotificationSeverity.INFO,
                "연결 계좌 인증 문구 재발송",
                target.getBankName() + " / " + target.getAccountAlias() + " 계좌의 새 인증 문구는 " + newVerificationReference + " 입니다. 10분 안에 입력해 주세요.",
                "/linked-bank-accounts",
                "LINKED_BANK_ACCOUNT",
                target.getId()
        );
        auditLogService.logCurrentActor(
                AuditActionType.LINKED_BANK_ACCOUNT_VERIFICATION_RESENT,
                "LINKED_BANK_ACCOUNT",
                target.getId().toString(),
                "Resent verification reference for linked bank account " + target.getBankName() + " / " + target.getAccountAlias()
        );
        return target;
    }

    @Transactional
    public LinkedBankAccount activate(UUID linkedBankAccountId) {
        LinkedBankAccount target = linkedBankAccountRepository.findById(linkedBankAccountId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Linked bank account not found"));

        if (target.getStatus() == LinkedBankAccountStatus.ACTIVE) {
            return target;
        }
        if (target.getStatus() == LinkedBankAccountStatus.BLOCKED) {
            throw new ResponseStatusException(CONFLICT, "Blocked linked bank account cannot be activated");
        }

        activateInternal(target);

        customerRepository.findById(target.getCustomerId())
                .map(Customer::getEndUserId)
                .ifPresent(endUserId -> notificationService.notifyUser(
                        "USER-LINKED-BANK-ACTIVE:" + target.getId(),
                        endUserId,
                        NotificationCategory.FUNDING,
                        NotificationSeverity.SUCCESS,
                        "연결 계좌 검증 완료",
                        target.getBankName() + " / " + target.getAccountAlias() + " 계좌가 출금 목적지로 활성화되었습니다.",
                        "/linked-bank-accounts",
                        "LINKED_BANK_ACCOUNT",
                        target.getId()
                ));

        auditLogService.logCurrentActor(
                AuditActionType.LINKED_BANK_ACCOUNT_ACTIVATED,
                "LINKED_BANK_ACCOUNT",
                target.getId().toString(),
                "Activated linked bank account " + target.getBankName() + " / " + target.getAccountAlias()
        );
        return target;
    }

    @Transactional
    public LinkedBankAccount markPrimary(UUID endUserId, UUID linkedBankAccountId) {
        Customer customer = customerRepository.findByEndUserId(endUserId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Customer not found"));

        LinkedBankAccount target = getOwnedActiveLinkedBankAccount(customer.getId(), linkedBankAccountId);
        List<LinkedBankAccount> existingAccounts = linkedBankAccountRepository.findByCustomerIdOrderByPrimaryWithdrawalDescCreatedAtDesc(customer.getId());
        existingAccounts.forEach(LinkedBankAccount::clearPrimary);
        target.makePrimary();

        auditLogService.logCurrentActor(
                AuditActionType.LINKED_BANK_ACCOUNT_PRIMARY_SET,
                "LINKED_BANK_ACCOUNT",
                target.getId().toString(),
                "Set linked bank account as primary withdrawal " + target.getBankName() + " / " + target.getAccountAlias()
        );
        return target;
    }

    @Transactional
    public LinkedBankAccount block(UUID linkedBankAccountId) {
        LinkedBankAccount target = linkedBankAccountRepository.findById(linkedBankAccountId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Linked bank account not found"));

        if (target.getStatus() == LinkedBankAccountStatus.BLOCKED) {
            return target;
        }

        boolean wasPrimary = target.isPrimaryWithdrawal();
        target.block(LinkedBankAccountBlockReasonCode.OPS_BLOCKED);

        if (wasPrimary) {
            linkedBankAccountRepository.findByCustomerIdOrderByPrimaryWithdrawalDescCreatedAtDesc(target.getCustomerId()).stream()
                    .filter(account -> !account.getId().equals(target.getId()))
                    .filter(account -> account.getStatus() == LinkedBankAccountStatus.ACTIVE)
                    .findFirst()
                    .ifPresent(LinkedBankAccount::makePrimary);
        }

        customerRepository.findById(target.getCustomerId())
                .map(Customer::getEndUserId)
                .ifPresent(endUserId -> notificationService.notifyUser(
                        "USER-LINKED-BANK-BLOCKED:" + target.getId(),
                        endUserId,
                        NotificationCategory.FUNDING,
                        NotificationSeverity.WARNING,
                        "연결 계좌 차단",
                        target.getBankName() + " / " + target.getAccountAlias() + " 계좌가 출금 목적지에서 제외되었습니다.",
                        "/linked-bank-accounts",
                        "LINKED_BANK_ACCOUNT",
                        target.getId()
                ));

        auditLogService.logCurrentActor(
                AuditActionType.LINKED_BANK_ACCOUNT_BLOCKED,
                "LINKED_BANK_ACCOUNT",
                target.getId().toString(),
                "Blocked linked bank account " + target.getBankName() + " / " + target.getAccountAlias()
        );
        return target;
    }

    @Transactional(readOnly = true)
    public LinkedBankAccount getOwnedActiveLinkedBankAccount(UUID customerId, UUID linkedBankAccountId) {
        LinkedBankAccount account = getOwnedLinkedBankAccount(customerId, linkedBankAccountId);
        if (account.getStatus() != LinkedBankAccountStatus.ACTIVE) {
            throw new ResponseStatusException(CONFLICT, "Linked bank account is not active");
        }
        return account;
    }

    @Transactional(readOnly = true)
    public LinkedBankAccount getOwnedLinkedBankAccount(UUID customerId, UUID linkedBankAccountId) {
        LinkedBankAccount account = linkedBankAccountRepository.findById(linkedBankAccountId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Linked bank account not found"));
        if (!account.getCustomerId().equals(customerId)) {
            throw new ResponseStatusException(FORBIDDEN, "Linked bank account does not belong to current user");
        }
        return account;
    }

    private void activateInternal(LinkedBankAccount target) {
        List<LinkedBankAccount> existingAccounts = linkedBankAccountRepository.findByCustomerIdOrderByPrimaryWithdrawalDescCreatedAtDesc(target.getCustomerId());
        boolean hasActivePrimary = existingAccounts.stream()
                .filter(account -> !account.getId().equals(target.getId()))
                .filter(account -> account.getStatus() == LinkedBankAccountStatus.ACTIVE)
                .anyMatch(LinkedBankAccount::isPrimaryWithdrawal);
        boolean shouldBecomePrimary = target.isPrimaryWithdrawal() || !hasActivePrimary;

        if (shouldBecomePrimary) {
            existingAccounts.stream()
                    .filter(account -> !account.getId().equals(target.getId()))
                    .forEach(LinkedBankAccount::clearPrimary);
            target.makePrimary();
        } else {
            target.clearPrimary();
        }
        target.activate();
    }

    private String generateVerificationReference() {
        return "MVP-" + ThreadLocalRandom.current().nextInt(1000, 10_000);
    }

    private String normalizeVerificationReference(String verificationReference) {
        if (verificationReference == null) {
            return null;
        }
        return verificationReference.trim().toUpperCase();
    }

    private Comparator<LinkedBankAccount> adminQueueComparator(Instant now) {
        return Comparator
                .comparingInt((LinkedBankAccount account) -> adminQueuePriority(account, now))
                .thenComparing(LinkedBankAccount::getCreatedAt, Comparator.reverseOrder());
    }

    private int adminQueuePriority(LinkedBankAccount account, Instant now) {
        if (account.getStatus() == LinkedBankAccountStatus.PENDING_VERIFICATION && account.isVerificationExpired(now)) {
            return 0;
        }
        if (account.getStatus() == LinkedBankAccountStatus.PENDING_VERIFICATION) {
            return 1;
        }
        if (account.getStatus() == LinkedBankAccountStatus.BLOCKED) {
            return 2;
        }
        return 3;
    }
}
