package com.portal.admin.api;

import com.portal.admin.api.base.BaseCrudController;
import static com.portal.admin.dto.PermissionDtos.*;
import com.portal.admin.service.PermissionCrudService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Permissions", description = "Permission management")
@RestController
@RequestMapping("/permissions")
public class PermissionController extends BaseCrudController<Long, CreatePermissionRequest, UpdatePermissionRequest, PermissionResponse> {

    public PermissionController(PermissionCrudService service) {
        super(service);
    }
}
