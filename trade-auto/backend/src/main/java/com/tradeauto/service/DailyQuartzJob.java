package com.tradeauto.service;

import com.tradeauto.model.JobRun;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@DisallowConcurrentExecution
public class DailyQuartzJob implements Job {
    private final MarketDataService marketDataService;
    private final SignalService signalService;
    private final JobRunService jobRunService;

    @Autowired
    public DailyQuartzJob(MarketDataService marketDataService, SignalService signalService, JobRunService jobRunService) {
        this.marketDataService = marketDataService;
        this.signalService = signalService;
        this.jobRunService = jobRunService;
    }

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobRun run = jobRunService.start("daily-scan");
        try {
            marketDataService.refreshAllTickers(400);
            int count = signalService.generateLatestSignals().size();
            jobRunService.success(run, "signals=" + count);
        } catch (IllegalStateException ex) {
            jobRunService.failure(run, ex.getMessage());
        } catch (Exception ex) {
            jobRunService.failure(run, "unexpected");
        }
    }
}
