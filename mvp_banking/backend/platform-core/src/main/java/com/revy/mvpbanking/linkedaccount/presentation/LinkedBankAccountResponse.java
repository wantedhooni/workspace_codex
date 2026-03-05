package com.revy.mvpbanking.linkedaccount.presentation;

import com.revy.mvpbanking.common.support.MaskingUtils;
import com.revy.mvpbanking.linkedaccount.domain.LinkedBankAccount;
import java.time.Instant;
import java.util.UUID;

public record LinkedBankAccountResponse(
        UUID id,
        UUID customerId,
        String customerEmail,
        String bankName,
        String accountAlias,
        String accountHolderName,
        String maskedAccountNumber,
        String status,
        boolean primaryWithdrawal,
        Instant verifiedAt,
        String verificationReference,
        Instant verificationRequestedAt,
        Instant verificationExpiresAt,
        boolean verificationExpired,
        Instant lastVerificationResentAt,
        Instant verificationResendAvailableAt,
        boolean verificationResendAllowed,
        int verificationAttemptCount,
        String blockReasonCode,
        Instant blockedAt,
        Instant createdAt
) {
    public static LinkedBankAccountResponse from(LinkedBankAccount account) {
        Instant now = Instant.now();
        return new LinkedBankAccountResponse(
                account.getId(),
                account.getCustomerId(),
                account.getCustomerEmail(),
                account.getBankName(),
                account.getAccountAlias(),
                account.getAccountHolderName(),
                MaskingUtils.maskAccountNumber(account.getAccountNumber()),
                account.getStatus().name(),
                account.isPrimaryWithdrawal(),
                account.getVerifiedAt(),
                account.getVerificationReference(),
                account.getVerificationRequestedAt(),
                account.getVerificationExpiresAt(),
                account.isVerificationExpired(now),
                account.getLastVerificationResentAt(),
                account.getVerificationResendAvailableAt(),
                account.isVerificationResendAllowed(now),
                account.getVerificationAttemptCount(),
                account.getBlockReasonCode() != null ? account.getBlockReasonCode().name() : null,
                account.getBlockedAt(),
                account.getCreatedAt()
        );
    }
}
