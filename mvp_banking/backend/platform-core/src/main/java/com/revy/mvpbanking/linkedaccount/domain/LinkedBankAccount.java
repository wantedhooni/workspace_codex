package com.revy.mvpbanking.linkedaccount.domain;

import com.revy.mvpbanking.common.domain.BaseJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "linked_bank_accounts")
public class LinkedBankAccount extends BaseJpaEntity {

    private static final Duration VERIFICATION_WINDOW = Duration.ofMinutes(10);
    private static final Duration VERIFICATION_RESEND_COOLDOWN = Duration.ofSeconds(30);

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "customer_email", nullable = false, length = 120)
    private String customerEmail;

    @Column(name = "bank_name", nullable = false, length = 80)
    private String bankName;

    @Column(name = "account_alias", nullable = false, length = 80)
    private String accountAlias;

    @Column(name = "account_holder_name", nullable = false, length = 120)
    private String accountHolderName;

    @Column(name = "account_number", nullable = false, length = 40)
    private String accountNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private LinkedBankAccountStatus status;

    @Column(name = "primary_withdrawal", nullable = false)
    private boolean primaryWithdrawal;

    @Column(name = "verified_at")
    private Instant verifiedAt;

    @Column(name = "verification_reference", length = 20)
    private String verificationReference;

    @Column(name = "verification_requested_at")
    private Instant verificationRequestedAt;

    @Column(name = "verification_attempt_count", nullable = false)
    private int verificationAttemptCount;

    @Column(name = "last_verification_resent_at")
    private Instant lastVerificationResentAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "block_reason_code", length = 50)
    private LinkedBankAccountBlockReasonCode blockReasonCode;

    @Column(name = "blocked_at")
    private Instant blockedAt;

    protected LinkedBankAccount() {
    }

    public LinkedBankAccount(
            UUID customerId,
            String customerEmail,
            String bankName,
            String accountAlias,
            String accountHolderName,
            String accountNumber,
            boolean primaryWithdrawal
    ) {
        this(
                customerId,
                customerEmail,
                bankName,
                accountAlias,
                accountHolderName,
                accountNumber,
                LinkedBankAccountStatus.ACTIVE,
                primaryWithdrawal,
                Instant.now(),
                null,
                null,
                0,
                null,
                null,
                null
        );
    }

    private LinkedBankAccount(
            UUID customerId,
            String customerEmail,
            String bankName,
            String accountAlias,
            String accountHolderName,
            String accountNumber,
            LinkedBankAccountStatus status,
            boolean primaryWithdrawal,
            Instant verifiedAt,
            String verificationReference,
            Instant verificationRequestedAt,
            int verificationAttemptCount,
            Instant lastVerificationResentAt,
            LinkedBankAccountBlockReasonCode blockReasonCode,
            Instant blockedAt
    ) {
        this.customerId = customerId;
        this.customerEmail = customerEmail;
        this.bankName = bankName;
        this.accountAlias = accountAlias;
        this.accountHolderName = accountHolderName;
        this.accountNumber = accountNumber;
        this.status = status;
        this.primaryWithdrawal = primaryWithdrawal;
        this.verifiedAt = verifiedAt;
        this.verificationReference = verificationReference;
        this.verificationRequestedAt = verificationRequestedAt;
        this.verificationAttemptCount = verificationAttemptCount;
        this.lastVerificationResentAt = lastVerificationResentAt;
        this.blockReasonCode = blockReasonCode;
        this.blockedAt = blockedAt;
    }

    public static LinkedBankAccount pendingVerification(
            UUID customerId,
            String customerEmail,
            String bankName,
            String accountAlias,
            String accountHolderName,
            String accountNumber,
            boolean primaryWithdrawal,
            String verificationReference
    ) {
        return new LinkedBankAccount(
                customerId,
                customerEmail,
                bankName,
                accountAlias,
                accountHolderName,
                accountNumber,
                LinkedBankAccountStatus.PENDING_VERIFICATION,
                primaryWithdrawal,
                null,
                verificationReference,
                Instant.now(),
                0,
                null,
                null,
                null
        );
    }

    public void makePrimary() {
        this.primaryWithdrawal = true;
    }

    public void clearPrimary() {
        this.primaryWithdrawal = false;
    }

    public void block(LinkedBankAccountBlockReasonCode blockReasonCode) {
        this.status = LinkedBankAccountStatus.BLOCKED;
        this.primaryWithdrawal = false;
        this.verificationReference = null;
        this.verificationRequestedAt = null;
        this.lastVerificationResentAt = null;
        this.blockReasonCode = blockReasonCode;
        this.blockedAt = Instant.now();
    }

    public void activate() {
        this.status = LinkedBankAccountStatus.ACTIVE;
        this.verifiedAt = Instant.now();
        this.verificationReference = null;
        this.verificationRequestedAt = null;
        this.lastVerificationResentAt = null;
        this.blockReasonCode = null;
        this.blockedAt = null;
    }

    public void registerVerificationAttempt() {
        this.verificationAttemptCount += 1;
    }

    public boolean matchesVerificationReference(String submittedReference) {
        if (verificationReference == null || submittedReference == null) {
            return false;
        }
        return verificationReference.equalsIgnoreCase(submittedReference.trim());
    }

    public void refreshVerificationChallenge(String newVerificationReference) {
        this.verificationReference = newVerificationReference;
        this.verificationRequestedAt = Instant.now();
        this.verificationAttemptCount = 0;
        this.lastVerificationResentAt = Instant.now();
    }

    public Instant getVerificationExpiresAt() {
        if (verificationRequestedAt == null) {
            return null;
        }
        return verificationRequestedAt.plus(VERIFICATION_WINDOW);
    }

    public boolean isVerificationExpired(Instant now) {
        Instant verificationExpiresAt = getVerificationExpiresAt();
        return verificationExpiresAt != null && now.isAfter(verificationExpiresAt);
    }

    public Instant getVerificationResendAvailableAt() {
        if (lastVerificationResentAt == null) {
            return Instant.now();
        }
        return lastVerificationResentAt.plus(VERIFICATION_RESEND_COOLDOWN);
    }

    public boolean isVerificationResendAllowed(Instant now) {
        if (lastVerificationResentAt == null) {
            return true;
        }
        return !now.isBefore(lastVerificationResentAt.plus(VERIFICATION_RESEND_COOLDOWN));
    }

    public UUID getId() {
        return id;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public String getBankName() {
        return bankName;
    }

    public String getAccountAlias() {
        return accountAlias;
    }

    public String getAccountHolderName() {
        return accountHolderName;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public LinkedBankAccountStatus getStatus() {
        return status;
    }

    public boolean isPrimaryWithdrawal() {
        return primaryWithdrawal;
    }

    public Instant getVerifiedAt() {
        return verifiedAt;
    }

    public String getVerificationReference() {
        return verificationReference;
    }

    public Instant getVerificationRequestedAt() {
        return verificationRequestedAt;
    }

    public int getVerificationAttemptCount() {
        return verificationAttemptCount;
    }

    public Instant getLastVerificationResentAt() {
        return lastVerificationResentAt;
    }

    public LinkedBankAccountBlockReasonCode getBlockReasonCode() {
        return blockReasonCode;
    }

    public Instant getBlockedAt() {
        return blockedAt;
    }
}
