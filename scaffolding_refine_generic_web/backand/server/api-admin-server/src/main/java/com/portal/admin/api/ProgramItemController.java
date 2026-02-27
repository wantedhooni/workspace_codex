package com.portal.admin.api;

import com.portal.admin.api.base.BaseCrudController;
import static com.portal.admin.dto.ProgramItemDtos.*;
import com.portal.admin.service.ProgramItemCrudService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Programs", description = "Program management")
@RestController
@RequestMapping("/programs")
public class ProgramItemController extends BaseCrudController<Long, CreateProgramItemRequest, UpdateProgramItemRequest, ProgramItemResponse> {

    public ProgramItemController(ProgramItemCrudService service) {
        super(service);
    }
}
