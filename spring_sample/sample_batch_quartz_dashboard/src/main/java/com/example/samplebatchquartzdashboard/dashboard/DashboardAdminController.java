package com.example.samplebatchquartzdashboard.dashboard;

import com.example.samplebatchquartzdashboard.batch.TaskImportJobService;
import com.example.samplebatchquartzdashboard.config.QuartzConfig;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.batch.core.JobExecution;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Controller
public class DashboardAdminController {

    private final TaskDatasetService taskDatasetService;
    private final TaskImportJobService taskImportJobService;
    private final DashboardMetricsRepository dashboardMetricsRepository;
    private final Scheduler scheduler;

    public DashboardAdminController(
            TaskDatasetService taskDatasetService,
            TaskImportJobService taskImportJobService,
            DashboardMetricsRepository dashboardMetricsRepository,
            Scheduler scheduler
    ) {
        this.taskDatasetService = taskDatasetService;
        this.taskImportJobService = taskImportJobService;
        this.dashboardMetricsRepository = dashboardMetricsRepository;
        this.scheduler = scheduler;
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
    public TaskSeedResponse seed(@RequestBody TaskSeedRequest request) {
        return taskDatasetService.seed(request);
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
        try {
            scheduler.triggerJob(QuartzConfig.jobKey());
            return new QuartzTriggerResponse("TRIGGERED");
        } catch (SchedulerException exception) {
            throw new IllegalStateException("Quartz 수동 트리거에 실패했습니다.", exception);
        }
    }
}
