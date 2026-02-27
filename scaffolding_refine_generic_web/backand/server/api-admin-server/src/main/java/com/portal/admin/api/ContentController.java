package com.portal.admin.api;

import com.portal.admin.api.base.BaseCrudController;
import static com.portal.admin.dto.ContentDtos.*;
import com.portal.admin.service.ContentCrudService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Contents", description = "Content management")
@RestController
@RequestMapping("/contents")
public class ContentController extends BaseCrudController<Long, CreateContentRequest, UpdateContentRequest, ContentResponse> {

    public ContentController(ContentCrudService service) {
        super(service);
    }
}
