package com.example.webfluxsample.operations;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.springframework.stereotype.Service;

@Service
public class OperationsService {

    public Mono<OperationsDashboardResponse> getDashboard(String domain) {
        return Mono.zip(
                        lookupRequestsPerSecond(domain),
                        lookupErrorRate(domain),
                        lookupLatency(domain)
                )
                .map(tuple -> new OperationsDashboardResponse(
                        domain,
                        tuple.getT1(),
                        tuple.getT2(),
                        tuple.getT3(),
                        tuple.getT2() < 1.0d && tuple.getT3() < 160 ? "STABLE" : "ATTENTION"
                ));
    }

    public Flux<OperationsEventResponse> streamEvents(String domain) {
        List<String> messages = List.of(
                "consumer lag 정상 범위 유지",
                "외부 결제 승인 응답 지연 감지",
                "재시도 큐 적체 해소",
                "에러율 하락 추세 확인",
                "배치 윈도우 진입 준비 완료"
        );

        return Flux.interval(Duration.ofMillis(400))
                .take(messages.size())
                .map(index -> new OperationsEventResponse(
                        domain,
                        index == 1 ? "WARN" : "INFO",
                        messages.get(index.intValue()),
                        OffsetDateTime.now()
                ));
    }

    public Mono<LatencyCheckResponse> checkLatency(LatencyCheckRequest request) {
        return lookupLatency(request.domain())
                .map(latency -> new LatencyCheckResponse(
                        request.domain(),
                        latency,
                        request.thresholdMillis(),
                        latency <= request.thresholdMillis()
                ));
    }

    private Mono<Integer> lookupRequestsPerSecond(String domain) {
        return Mono.delay(Duration.ofMillis(40))
                .map(ignore -> Math.abs(domain.hashCode() % 500) + 150);
    }

    private Mono<Double> lookupErrorRate(String domain) {
        return Mono.delay(Duration.ofMillis(65))
                .map(ignore -> (Math.abs(domain.hashCode() % 7) + 2) / 10.0d);
    }

    private Mono<Integer> lookupLatency(String domain) {
        return Mono.delay(Duration.ofMillis(90))
                .map(ignore -> Math.abs(domain.hashCode() % 120) + 60);
    }
}
