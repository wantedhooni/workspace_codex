package com.example.marketsignal.report;

import com.example.marketsignal.signal.Signal;
import com.example.marketsignal.signal.SignalAction;
import java.util.Arrays;
import java.util.List;

/**
 * 리포트 내 시그널 응답 모델이다.
 */
public record SignalSummaryResponse(
        String ticker,
        int score,
        SignalAction action,
        List<String> reasons
) {

    public static SignalSummaryResponse from(Signal signal) {
        return new SignalSummaryResponse(
                signal.getTicker(),
                signal.getScore(),
                signal.getAction(),
                Arrays.stream(signal.getReasons().split(","))
                        .map(String::trim)
                        .filter(reason -> !reason.isBlank())
                        .toList()
        );
    }
}
