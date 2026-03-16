package com.example.marketsignal.batch;

import com.example.marketsignal.config.AppProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 애플리케이션 시작 시 필요한 초기 배치 작업을 실행한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BatchStartupInitializer implements ApplicationRunner {

    private final AppProperties appProperties;
    private final BatchOperationsService batchOperationsService;

    /**
     * 시드 사용 설정이 켜져 있으면 실제 시장 시드 적재 배치를 1회 실행한다.
     */
    @Override
    public void run(ApplicationArguments args) {
        if (!appProperties.seed().enabled()) {
            return;
        }

        try {
            batchOperationsService.launchStartupJob(ManagedBatchJob.MARKET_SEED_LOAD.apiName());
        } catch (Exception exception) {
            log.warn("시작 시 배치 초기화에 실패했습니다.", exception);
        }
    }
}
