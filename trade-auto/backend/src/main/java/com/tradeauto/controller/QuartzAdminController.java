package com.tradeauto.controller;

import com.tradeauto.dto.ApiResponse;
import com.tradeauto.dto.QuartzJobDTO;
import com.tradeauto.dto.QuartzJobCreateRequest;
import com.tradeauto.dto.QuartzTriggerCreateRequest;
import com.tradeauto.dto.QuartzTriggerDTO;
import com.tradeauto.dto.QuartzTriggerUpdateRequest;
import com.tradeauto.model.QuartzAudit;
import com.tradeauto.service.QuartzAdminService;
import com.tradeauto.service.QuartzAuditService;
import org.quartz.SchedulerException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/quartz")
@CrossOrigin
public class QuartzAdminController {
    private final QuartzAdminService service;
    private final QuartzAuditService auditService;

    public QuartzAdminController(QuartzAdminService service, QuartzAuditService auditService) {
        this.service = service;
        this.auditService = auditService;
    }

    @GetMapping("/jobs")
    public ApiResponse<List<QuartzJobDTO>> jobs() throws SchedulerException {
        return ApiResponse.ok(service.listJobs());
    }

    @PostMapping("/jobs")
    public ApiResponse<QuartzJobDTO> createJob(@RequestBody QuartzJobCreateRequest request) throws SchedulerException {
        return ApiResponse.ok(service.createJob(request.name, request.group, request.className, request.description, request.durable));
    }

    @DeleteMapping("/jobs/{group}/{name}")
    public ApiResponse<String> deleteJob(@PathVariable String group, @PathVariable String name) throws SchedulerException {
        service.deleteJob(name, group);
        return ApiResponse.ok("deleted");
    }

    @GetMapping("/triggers")
    public ApiResponse<List<QuartzTriggerDTO>> triggers() throws SchedulerException {
        return ApiResponse.ok(service.listTriggers());
    }

    @PutMapping("/triggers/{group}/{name}")
    public ApiResponse<QuartzTriggerDTO> updateTrigger(@PathVariable String group,
                                                       @PathVariable String name,
                                                       @RequestBody QuartzTriggerUpdateRequest request) throws SchedulerException {
        return ApiResponse.ok(service.updateCronTrigger(name, group, request));
    }

    @PostMapping("/triggers")
    public ApiResponse<QuartzTriggerDTO> createTrigger(@RequestBody QuartzTriggerCreateRequest request) throws SchedulerException {
        return ApiResponse.ok(service.createCronTrigger(request));
    }

    @DeleteMapping("/triggers/{group}/{name}")
    public ApiResponse<String> deleteTrigger(@PathVariable String group, @PathVariable String name) throws SchedulerException {
        service.deleteTrigger(name, group);
        return ApiResponse.ok("deleted");
    }

    @PostMapping("/triggers/{group}/{name}/pause")
    public ApiResponse<String> pauseTrigger(@PathVariable String group, @PathVariable String name) throws SchedulerException {
        service.pauseTrigger(name, group);
        return ApiResponse.ok("paused");
    }

    @PostMapping("/triggers/{group}/{name}/resume")
    public ApiResponse<String> resumeTrigger(@PathVariable String group, @PathVariable String name) throws SchedulerException {
        service.resumeTrigger(name, group);
        return ApiResponse.ok("resumed");
    }

    @PostMapping("/jobs/{group}/{name}/run")
    public ApiResponse<String> runJob(@PathVariable String group, @PathVariable String name) throws SchedulerException {
        service.triggerJob(name, group);
        return ApiResponse.ok("triggered");
    }

    @GetMapping("/audit")
    public ApiResponse<List<QuartzAudit>> audit() {
        return ApiResponse.ok(auditService.recent());
    }
}
