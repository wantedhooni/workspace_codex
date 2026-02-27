package com.portal.admin.api;

import com.portal.admin.api.base.BaseCrudController;
import static com.portal.admin.dto.LoginPolicyDtos.*;
import com.portal.admin.service.LoginPolicyCrudService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "LoginPolicies", description = "Login policy management")
@RestController
@RequestMapping("/login-policies")
public class LoginPolicyController extends BaseCrudController<Long, CreateLoginPolicyRequest, UpdateLoginPolicyRequest, LoginPolicyResponse> {

    public LoginPolicyController(LoginPolicyCrudService service) {
        super(service);
    }
}
