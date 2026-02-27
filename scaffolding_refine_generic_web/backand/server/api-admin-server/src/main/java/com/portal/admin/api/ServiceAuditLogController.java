package com.portal.admin.api;

import com.portal.admin.api.base.BaseCrudController;
import static com.portal.admin.dto.ServiceAuditLogDtos.*;
import com.portal.admin.service.ServiceAuditLogCrudService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "ServiceAuditLogs", description = "Service audit log management")
@RestController
@RequestMapping("/service-audit-logs")
public class ServiceAuditLogController extends BaseCrudController<Long, CreateServiceAuditLogRequest, UpdateServiceAuditLogRequest, ServiceAuditLogResponse> {

    public ServiceAuditLogController(ServiceAuditLogCrudService service) {
        super(service);
    }
}
