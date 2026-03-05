package com.example.samplebatchquartzdashboard.dashboard;

import com.example.samplebatchquartzdashboard.batch.TaskImportJobService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.batch.core.JobExecution;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
public class DashboardAdminController {

    private final TaskDatasetService taskDatasetService;
    private final TaskImportJobService taskImportJobService;
    private final DashboardMetricsRepository dashboardMetricsRepository;
    private final QuartzControlService quartzControlService;

    public DashboardAdminController(
            TaskDatasetService taskDatasetService,
            TaskImportJobService taskImportJobService,
            DashboardMetricsRepository dashboardMetricsRepository,
            QuartzControlService quartzControlService
    ) {
        this.taskDatasetService = taskDatasetService;
        this.taskImportJobService = taskImportJobService;
        this.dashboardMetricsRepository = dashboardMetricsRepository;
        this.quartzControlService = quartzControlService;
    }

    @GetMapping("/dashboard")
    public String dashboardPage() {
        return "redirect:/dashboard/index.html";
    }

    @GetMapping("/api/dashboard/overview")
    @ResponseBody
    public DashboardOverviewResponse overview() {
        return dashboardMetricsRepository.fetchOverview();
    }

    @PostMapping("/api/dashboard/tasks/seed")
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public TaskSeedResponse seed(@Valid @RequestBody TaskSeedRequest request) {
        return taskDatasetService.seed(request);
    }

    @PostMapping("/api/dashboard/tasks/seed/real")
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public TaskSeedResponse seedRealSample(@RequestParam(defaultValue = "true") boolean truncateBeforeLoad) {
        return taskDatasetService.seedRealSample(truncateBeforeLoad);
    }

    @PostMapping("/api/dashboard/jobs/import/run")
    @ResponseBody
    public TaskJobLaunchResponse runNow() {
        JobExecution execution = taskImportJobService.launchNow("MANUAL");
        return new TaskJobLaunchResponse(execution.getId(), execution.getStatus().name());
    }

    @PostMapping("/api/dashboard/jobs/import/quartz/trigger")
    @ResponseBody
    public QuartzTriggerResponse triggerQuartz() {
        QuartzActionResponse response = quartzControlService.triggerNow();
        return new QuartzTriggerResponse(response.action());
    }

    @PostMapping("/api/dashboard/examples/real/run")
    @ResponseBody
    public RealBatchRunExampleResponse runRealDataBatchExample() {
        TaskSeedResponse seedResponse = taskDatasetService.seedRealSample(true);
        JobExecution execution = taskImportJobService.launchNow("REAL_DATA_EXAMPLE");
        TaskJobLaunchResponse jobResponse = new TaskJobLaunchResponse(execution.getId(), execution.getStatus().name());
        DashboardOverviewResponse overview = dashboardMetricsRepository.fetchOverview();
        List<RecentAuditResponse> recentAudits = dashboardMetricsRepository.fetchRecentAudits(10);
        return new RealBatchRunExampleResponse(seedResponse, jobResponse, overview, recentAudits);
    }

    @GetMapping("/api/dashboard/audits/recent")
    @ResponseBody
    public List<RecentAuditResponse> recentAudits(@RequestParam(defaultValue = "20") int limit) {
        int boundedLimit = Math.max(1, Math.min(limit, 200));
        return dashboardMetricsRepository.fetchRecentAudits(boundedLimit);
    }

    @GetMapping("/api/dashboard/quartz/status")
    @ResponseBody
    public QuartzStatusResponse quartzStatus() {
        return quartzControlService.status();
    }

    @PostMapping("/api/dashboard/quartz/pause")
    @ResponseBody
    public QuartzActionResponse pauseQuartzTrigger() {
        return quartzControlService.pauseTrigger();
    }

    @PostMapping("/api/dashboard/quartz/resume")
    @ResponseBody
    public QuartzActionResponse resumeQuartzTrigger() {
        return quartzControlService.resumeTrigger();
    }

    @PostMapping("/api/dashboard/quartz/standby")
    @ResponseBody
    public QuartzActionResponse standbyQuartzScheduler() {
        return quartzControlService.standbyScheduler();
    }

    @PostMapping("/api/dashboard/quartz/start")
    @ResponseBody
    public QuartzActionResponse startQuartzScheduler() {
        return quartzControlService.startScheduler();
    }

    @PutMapping("/api/dashboard/quartz/cron")
    @ResponseBody
    public QuartzActionResponse updateQuartzCron(@Valid @RequestBody QuartzCronUpdateRequest request) {
        return quartzControlService.updateCron(request.cronExpression());
    }
}
