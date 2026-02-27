package com.portal.admin.api;

import com.portal.admin.api.base.BaseCrudController;
import static com.portal.admin.dto.RoleDtos.*;
import com.portal.admin.service.RoleCrudService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Roles", description = "Role management")
@RestController
@RequestMapping("/roles")
public class RoleController extends BaseCrudController<Long, CreateRoleRequest, UpdateRoleRequest, RoleResponse> {

    public RoleController(RoleCrudService service) {
        super(service);
    }
}
