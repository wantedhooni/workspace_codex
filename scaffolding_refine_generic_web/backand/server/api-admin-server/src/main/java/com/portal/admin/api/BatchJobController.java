package com.portal.admin.api;

import com.portal.admin.api.base.BaseCrudController;
import static com.portal.admin.dto.BatchJobDtos.*;
import com.portal.admin.service.BatchJobCrudService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "BatchJobs", description = "Batch job management")
@RestController
@RequestMapping("/batch-jobs")
public class BatchJobController extends BaseCrudController<Long, CreateBatchJobRequest, UpdateBatchJobRequest, BatchJobResponse> {

    public BatchJobController(BatchJobCrudService service) {
        super(service);
    }
}
