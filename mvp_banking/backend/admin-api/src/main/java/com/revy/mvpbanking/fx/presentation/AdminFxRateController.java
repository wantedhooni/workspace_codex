package com.revy.mvpbanking.fx.presentation;

import com.revy.mvpbanking.audit.application.AuditLogService;
import com.revy.mvpbanking.audit.domain.AuditActionType;
import com.revy.mvpbanking.common.api.ApiResponse;
import com.revy.mvpbanking.fx.application.FxRateService;
import java.util.List;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Profile("admin-api")
@RequestMapping("/api/admin/fx-rates")
public class AdminFxRateController {

    private final FxRateService fxRateService;
    private final AuditLogService auditLogService;

    public AdminFxRateController(FxRateService fxRateService, AuditLogService auditLogService) {
        this.fxRateService = fxRateService;
        this.auditLogService = auditLogService;
    }

    @GetMapping
    public ApiResponse<List<FxRateResponse>> list() {
        auditLogService.logCurrentActor(AuditActionType.FX_RATE_VIEWED, "FX_RATE", "all", "Viewed FX rates");
        return ApiResponse.ok(fxRateService.getRates().stream().map(FxRateResponse::from).toList());
    }
}
