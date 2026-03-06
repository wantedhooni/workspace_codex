package com.example.dynamicjob.dto;

public class JobDefinitionRequest {
    private String jobName;
    private String cronExpr;
    private String beanName;
    private String methodName;
    private String argTypesJson;
    private String argsJson;
    private boolean enabled;

    public String getJobName() { return jobName; }
    public void setJobName(String jobName) { this.jobName = jobName; }
    public String getCronExpr() { return cronExpr; }
    public void setCronExpr(String cronExpr) { this.cronExpr = cronExpr; }
    public String getBeanName() { return beanName; }
    public void setBeanName(String beanName) { this.beanName = beanName; }
    public String getMethodName() { return methodName; }
    public void setMethodName(String methodName) { this.methodName = methodName; }
    public String getArgTypesJson() { return argTypesJson; }
    public void setArgTypesJson(String argTypesJson) { this.argTypesJson = argTypesJson; }
    public String getArgsJson() { return argsJson; }
    public void setArgsJson(String argsJson) { this.argsJson = argsJson; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}
