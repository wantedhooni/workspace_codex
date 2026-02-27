package com.tradeauto.controller;

import com.tradeauto.dto.ApiResponse;
import com.tradeauto.model.JobRun;
import com.tradeauto.service.JobRunService;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/jobs")
@CrossOrigin
public class JobController {
    private final Scheduler scheduler;
    private final JobRunService jobRunService;

    public JobController(Scheduler scheduler, JobRunService jobRunService) {
        this.scheduler = scheduler;
        this.jobRunService = jobRunService;
    }

    @PostMapping("/daily/run")
    public ApiResponse<String> runDaily() throws SchedulerException {
        scheduler.triggerJob(JobKey.jobKey("dailyJob"));
        return ApiResponse.ok("triggered dailyJob");
    }

    @GetMapping("/daily/runs")
    public ApiResponse<List<JobRun>> recentRuns() {
        return ApiResponse.ok(jobRunService.recent("daily-scan"));
    }

    @GetMapping("/runs")
    public ApiResponse<List<JobRun>> runsByJob(@RequestParam String jobName) {
        return ApiResponse.ok(jobRunService.recent(jobName));
    }
}
