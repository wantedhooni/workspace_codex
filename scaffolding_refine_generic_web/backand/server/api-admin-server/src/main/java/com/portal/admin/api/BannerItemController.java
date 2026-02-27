package com.portal.admin.api;

import com.portal.admin.api.base.BaseCrudController;
import static com.portal.admin.dto.BannerItemDtos.*;
import com.portal.admin.service.BannerItemCrudService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Banners", description = "Banner and popup management")
@RestController
@RequestMapping("/banners")
public class BannerItemController extends BaseCrudController<Long, CreateBannerItemRequest, UpdateBannerItemRequest, BannerItemResponse> {

    public BannerItemController(BannerItemCrudService service) {
        super(service);
    }
}
