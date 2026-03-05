package com.revy.mvpbanking.linkedaccount.presentation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record VerifyLinkedBankAccountRequest(
        @NotBlank
        @Size(max = 20)
        String verificationReference
) {
}
