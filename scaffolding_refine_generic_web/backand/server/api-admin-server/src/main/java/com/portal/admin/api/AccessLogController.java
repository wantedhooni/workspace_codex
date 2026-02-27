package com.portal.admin.api;

import com.portal.admin.api.base.BaseCrudController;
import static com.portal.admin.dto.AccessLogDtos.*;
import com.portal.admin.service.AccessLogCrudService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "AccessLogs", description = "Access log management")
@RestController
@RequestMapping("/access-logs")
public class AccessLogController extends BaseCrudController<Long, CreateAccessLogRequest, UpdateAccessLogRequest, AccessLogResponse> {

    public AccessLogController(AccessLogCrudService service) {
        super(service);
    }
}
