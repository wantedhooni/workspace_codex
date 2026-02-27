package com.portal.admin.api;

import com.portal.admin.api.base.BaseCrudController;
import static com.portal.admin.dto.BatchScheduleDtos.*;
import com.portal.admin.service.BatchScheduleCrudService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "BatchSchedules", description = "Batch schedule management")
@RestController
@RequestMapping("/batch-schedules")
public class BatchScheduleController extends BaseCrudController<Long, CreateBatchScheduleRequest, UpdateBatchScheduleRequest, BatchScheduleResponse> {

    public BatchScheduleController(BatchScheduleCrudService service) {
        super(service);
    }
}
