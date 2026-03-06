package com.example.samplebatchquartzdashboard.batchsample;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.batch-sample")
public class BatchSampleProperties {

    private int chunkSize = 200;
    private int pageSize = 200;
    private int defaultSeedSize = 1_000;

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

    public int getDefaultSeedSize() {
        return defaultSeedSize;
    }

    public void setDefaultSeedSize(int defaultSeedSize) {
        this.defaultSeedSize = defaultSeedSize;
    }
}
