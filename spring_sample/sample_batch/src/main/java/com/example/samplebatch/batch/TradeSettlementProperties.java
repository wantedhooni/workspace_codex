package com.example.samplebatch.batch;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.trade-settlement")
public class TradeSettlementProperties {

    @NotBlank
    private String cron;

    @Min(100)
    private int chunkSize = 1000;

    @Min(100)
    private int pageSize = 1000;

    @Min(100)
    private int defaultSeedBatchSize = 5000;

    public String getCron() {
        return cron;
    }

    public void setCron(String cron) {
        this.cron = cron;
    }

    public int getChunkSize() {
        return chunkSize;
    }

    public void setChunkSize(int chunkSize) {
        this.chunkSize = chunkSize;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getDefaultSeedBatchSize() {
        return defaultSeedBatchSize;
    }

    public void setDefaultSeedBatchSize(int defaultSeedBatchSize) {
        this.defaultSeedBatchSize = defaultSeedBatchSize;
    }
}
