package com.example.securities.eod;

import com.example.securities.eod.EodDtos.EodSnapshotResponse;
import com.example.securities.eod.EodDtos.RunEodRequest;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/eod")
public class EodController {

    private final EodService eodService;

    public EodController(EodService eodService) {
        this.eodService = eodService;
    }

    @PreAuthorize("hasRole('OPERATOR')")
    @PostMapping("/run")
    public EodSnapshotResponse run(@RequestBody(required = false) RunEodRequest request) {
        return eodService.run(request == null ? null : request.businessDate());
    }

    @PreAuthorize("hasAnyRole('VIEWER','OPERATOR')")
    @GetMapping("/snapshots")
    public List<EodSnapshotResponse> snapshots() {
        return eodService.getSnapshots();
    }
}
