package com.tradeauto.dto;

public class QuartzJobCreateRequest {
    public String name;
    public String group;
    public String className;
    public String description;
    public boolean durable = true;
}
