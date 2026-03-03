package com.example.samplebatch.quartz;

import com.example.samplebatch.batch.TradeSettlementJobService;
import java.time.Instant;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;

@DisallowConcurrentExecution
public class TradeSettlementQuartzJob extends QuartzJobBean {

    @Autowired
    private TradeSettlementJobService tradeSettlementJobService;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        try {
            Instant scheduledAt = context.getScheduledFireTime() == null
                    ? Instant.now()
                    : context.getScheduledFireTime().toInstant();
            tradeSettlementJobService.launchFromQuartz(scheduledAt);
        } catch (Exception exception) {
            throw new JobExecutionException("Quartz에서 거래 정산 배치 실행 중 오류가 발생했습니다.", exception);
        }
    }
}
