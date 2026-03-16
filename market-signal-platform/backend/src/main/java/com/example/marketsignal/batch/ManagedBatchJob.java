package com.example.marketsignal.batch;

import com.example.marketsignal.common.BusinessException;
import java.util.Arrays;
import org.quartz.JobKey;
import org.quartz.TriggerKey;
import org.springframework.http.HttpStatus;

/**
 * 운영 화면과 Quartz 스케줄러가 공통으로 참조하는 관리 대상 배치 작업을 정의한다.
 */
public enum ManagedBatchJob {

    MARKET_SEED_LOAD(
            "market-seed-load",
            "marketSeedLoadJob",
            "실제 시장 시드 적재",
            "실제 시장 스냅샷 JSON과 기본 관심 종목을 데이터베이스에 다시 적재합니다.",
            null,
            null
    ),
    DAILY_REPORT_GENERATION(
            "daily-report-generate",
            "dailyReportGenerationJob",
            "오늘의 리포트 생성",
            "최신 스냅샷을 기준으로 시장 레짐과 종목 시그널을 계산하고 일간 리포트를 생성합니다.",
            "dailyReportQuartzJob",
            "dailyReportQuartzTrigger"
    );

    private static final String QUARTZ_GROUP = "market-signal";

    private final String apiName;
    private final String springBatchJobName;
    private final String title;
    private final String description;
    private final String quartzJobName;
    private final String quartzTriggerName;

    ManagedBatchJob(
            String apiName,
            String springBatchJobName,
            String title,
            String description,
            String quartzJobName,
            String quartzTriggerName
    ) {
        this.apiName = apiName;
        this.springBatchJobName = springBatchJobName;
        this.title = title;
        this.description = description;
        this.quartzJobName = quartzJobName;
        this.quartzTriggerName = quartzTriggerName;
    }

    public String apiName() {
        return apiName;
    }

    public String springBatchJobName() {
        return springBatchJobName;
    }

    public String title() {
        return title;
    }

    public String description() {
        return description;
    }

    public boolean schedulable() {
        return quartzJobName != null && quartzTriggerName != null;
    }

    public JobKey quartzJobKey() {
        if (!schedulable()) {
            return null;
        }
        return JobKey.jobKey(quartzJobName, QUARTZ_GROUP);
    }

    public TriggerKey quartzTriggerKey() {
        if (!schedulable()) {
            return null;
        }
        return TriggerKey.triggerKey(quartzTriggerName, QUARTZ_GROUP);
    }

    /**
     * 외부 API 경로에서 전달한 작업 이름을 관리 대상 배치 정의로 변환한다.
     */
    public static ManagedBatchJob fromApiName(String apiName) {
        return Arrays.stream(values())
                .filter(job -> job.apiName.equals(apiName))
                .findFirst()
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "알 수 없는 배치 작업입니다: " + apiName));
    }
}
