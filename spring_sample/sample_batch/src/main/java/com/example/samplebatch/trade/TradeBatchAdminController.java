package com.example.samplebatch.trade;

import com.example.samplebatch.batch.TradeSettlementJobService;
import com.example.samplebatch.config.QuartzConfig;
import jakarta.validation.Valid;
import java.time.Instant;
import org.quartz.Scheduler;
import org.quartz.Trigger;
import org.springframework.batch.core.JobExecution;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class TradeBatchAdminController {

    private final TradeDatasetService tradeDatasetService;
    private final TradeSettlementJobService tradeSettlementJobService;
    private final TradeMetricsRepository tradeMetricsRepository;
    private final Scheduler scheduler;

    public TradeBatchAdminController(
            TradeDatasetService tradeDatasetService,
            TradeSettlementJobService tradeSettlementJobService,
            TradeMetricsRepository tradeMetricsRepository,
            Scheduler scheduler
    ) {
        this.tradeDatasetService = tradeDatasetService;
        this.tradeSettlementJobService = tradeSettlementJobService;
        this.tradeMetricsRepository = tradeMetricsRepository;
        this.scheduler = scheduler;
    }

    @PostMapping("/datasets/trades")
    public TradeSeedResponse seedTrades(@Valid @RequestBody TradeSeedRequest request) {
        return tradeDatasetService.seed(request);
    }

    @GetMapping("/datasets/trades/metrics")
    public TradeMetricsResponse metrics() {
        return tradeMetricsRepository.fetchMetrics();
    }

    @PostMapping("/jobs/trade-settlement/run")
    public TradeJobLaunchResponse runTradeSettlementJob() {
        JobExecution execution = tradeSettlementJobService.launchNow("MANUAL");
        return new TradeJobLaunchResponse(execution.getId(), execution.getStatus().name(), "MANUAL");
    }

    @PostMapping("/jobs/trade-settlement/quartz/trigger")
    public TradeJobLaunchResponse triggerQuartzJob() throws Exception {
        scheduler.triggerJob(QuartzConfig.jobKey());
        return new TradeJobLaunchResponse(null, "TRIGGERED", "QUARTZ");
    }

    @GetMapping("/jobs/trade-settlement/scheduler")
    public QuartzSchedulerStatusResponse schedulerStatus() throws Exception {
        Trigger trigger = scheduler.getTrigger(QuartzConfig.triggerKey());
        Trigger.TriggerState triggerState = scheduler.getTriggerState(QuartzConfig.triggerKey());

        return new QuartzSchedulerStatusResponse(
                QuartzConfig.JOB_NAME,
                QuartzConfig.TRIGGER_NAME,
                triggerState.name(),
                trigger.getPreviousFireTime() == null ? null : trigger.getPreviousFireTime().toInstant(),
                trigger.getNextFireTime() == null ? null : trigger.getNextFireTime().toInstant()
        );
    }
}
