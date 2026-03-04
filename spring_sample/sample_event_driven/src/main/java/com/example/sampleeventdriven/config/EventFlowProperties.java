package com.example.sampleeventdriven.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "app.events")
public class EventFlowProperties {

    @NotBlank
    private String ordersTopic;

    @NotBlank
    private String paymentsTopic;

    @Min(1)
    private int publishBatchSize = 50;

    public String getOrdersTopic() {
        return ordersTopic;
    }

    public void setOrdersTopic(String ordersTopic) {
        this.ordersTopic = ordersTopic;
    }

    public String getPaymentsTopic() {
        return paymentsTopic;
    }

    public void setPaymentsTopic(String paymentsTopic) {
        this.paymentsTopic = paymentsTopic;
    }

    public int getPublishBatchSize() {
        return publishBatchSize;
    }

    public void setPublishBatchSize(int publishBatchSize) {
        this.publishBatchSize = publishBatchSize;
    }
}
