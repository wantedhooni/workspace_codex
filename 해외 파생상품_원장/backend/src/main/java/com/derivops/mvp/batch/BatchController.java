package com.derivops.mvp.batch;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/batches")
public class BatchController {

    private final BatchService batchService;

    @PreAuthorize("hasAnyRole('OPS_ADMIN','OPS_VIEWER')")
    @GetMapping("/runs")
    public Page<BatchRunResponse> list(
            @RequestParam(required = false) LocalDate date,
            @RequestParam(required = false) BatchStatus status,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return batchService.list(date, status, keyword, filter, PageRequest.of(page, size));
    }

    @PreAuthorize("hasAnyRole('OPS_ADMIN','OPS_VIEWER')")
    @GetMapping("/runs/{runId}")
    public BatchRunResponse get(@PathVariable Long runId) {
        return batchService.get(runId);
    }
}
