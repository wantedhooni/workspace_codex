package com.example.marketsignal.watchlist;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 관심 종목 생성 요청 모델이다.
 */
public record WatchlistCreateRequest(
        @NotBlank(message = "티커는 필수입니다.")
        @Size(max = 20, message = "티커는 20자 이하여야 합니다.")
        String ticker,
        @NotBlank(message = "종목명은 필수입니다.")
        @Size(max = 120, message = "종목명은 120자 이하여야 합니다.")
        String companyName
) {
}
