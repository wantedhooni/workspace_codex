package com.portal.admin.api;

import com.portal.admin.api.base.BaseCrudController;
import static com.portal.admin.dto.AdminUserDtos.*;
import com.portal.admin.service.AdminUserCrudService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admins", description = "Admin user management")
@RestController
@RequestMapping("/admins")
public class AdminUserController extends BaseCrudController<Long, CreateAdminRequest, UpdateAdminRequest, AdminUserResponse> {

    public AdminUserController(AdminUserCrudService service) {
        super(service);
    }
}
