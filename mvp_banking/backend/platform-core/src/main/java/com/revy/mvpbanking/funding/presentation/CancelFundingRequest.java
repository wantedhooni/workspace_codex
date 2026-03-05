package com.revy.mvpbanking.funding.presentation;

import jakarta.validation.constraints.Size;

public record CancelFundingRequest(
        @Size(max = 255) String reason
) {
}
