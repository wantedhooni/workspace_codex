package com.example.marketsignal.signal;

/**
 * 시그널 계산 결과 모델이다.
 */
public record SignalResult(
        String ticker,
        int score,
        SignalAction action,
        String reasons
) {
}
