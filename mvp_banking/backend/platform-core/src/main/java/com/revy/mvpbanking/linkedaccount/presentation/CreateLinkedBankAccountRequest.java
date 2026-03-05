package com.revy.mvpbanking.linkedaccount.presentation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateLinkedBankAccountRequest(
        @NotBlank @Size(max = 80) String bankName,
        @NotBlank @Size(max = 80) String accountAlias,
        @NotBlank @Size(max = 120) String accountHolderName,
        @NotBlank @Size(max = 40) String accountNumber,
        boolean primaryWithdrawal
) {
}
