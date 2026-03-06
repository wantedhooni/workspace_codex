package com.example.dynamicjob.dto;

import java.time.LocalDate;

public class ReportRequest {
    private LocalDate bizDate;
    private String type;
    private Integer retryCount;

    public LocalDate getBizDate() { return bizDate; }
    public void setBizDate(LocalDate bizDate) { this.bizDate = bizDate; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Integer getRetryCount() { return retryCount; }
    public void setRetryCount(Integer retryCount) { this.retryCount = retryCount; }

    @Override
    public String toString() {
        return "ReportRequest{" +
                "bizDate=" + bizDate +
                ", type='" + type + '\'' +
                ", retryCount=" + retryCount +
                '}';
    }
}
