package com.portal.admin.menu.api;

import com.portal.admin.api.base.BaseCrudController;
import static com.portal.admin.menu.dto.MenuDtos.*;
import com.portal.admin.menu.service.MenuCrudService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@Tag(name = "Menus", description = "Menu management")
@RestController
@RequestMapping("/menus")
public class MenuController extends BaseCrudController<Long, CreateMenuRequest, UpdateMenuRequest, MenuResponse> {

    private final MenuCrudService menuService;

    public MenuController(MenuCrudService menuService) {
        super(menuService);
        this.menuService = menuService;
    }

    @GetMapping("/tree")
    public List<TreeMenuResponse> tree(
            @RequestParam(name = "searchParam", required = false) String searchParam,
            @RequestParam Map<String, String> requestParams
    ) {
        return menuService.tree(searchParam, requestParams);
    }
}
