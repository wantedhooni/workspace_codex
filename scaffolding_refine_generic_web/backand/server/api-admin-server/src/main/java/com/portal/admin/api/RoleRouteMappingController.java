package com.portal.admin.api;

import com.portal.admin.api.base.BaseCrudController;
import static com.portal.admin.dto.RoleRouteMappingDtos.*;
import com.portal.admin.service.RoleRouteMappingCrudService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "RoleRoutes", description = "Role URL mapping management")
@RestController
@RequestMapping("/role-routes")
public class RoleRouteMappingController extends BaseCrudController<Long, CreateRoleRouteMappingRequest, UpdateRoleRouteMappingRequest, RoleRouteMappingResponse> {

    public RoleRouteMappingController(RoleRouteMappingCrudService service) {
        super(service);
    }
}
