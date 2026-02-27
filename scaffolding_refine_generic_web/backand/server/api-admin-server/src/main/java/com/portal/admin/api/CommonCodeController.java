package com.portal.admin.api;

import com.portal.admin.api.base.BaseCrudController;
import static com.portal.admin.dto.CommonCodeDtos.*;
import com.portal.admin.service.CommonCodeCrudService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "CommonCodes", description = "Common code management")
@RestController
@RequestMapping("/common-codes")
public class CommonCodeController extends BaseCrudController<Long, CreateCommonCodeRequest, UpdateCommonCodeRequest, CommonCodeResponse> {

    public CommonCodeController(CommonCodeCrudService service) {
        super(service);
    }
}
