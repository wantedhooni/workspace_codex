package com.derivops.mvp.menu.api;
import com.derivops.mvp.menu.*;
import com.derivops.mvp.menu.application.*;
import com.derivops.mvp.menu.dto.*;
import com.derivops.mvp.menu.infrastructure.*;


import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/menus")
public class MenuController {

    private final MenuService menuService;

    @PreAuthorize("hasAnyRole('OPS_ADMIN','OPS_VIEWER','AUDITOR')")
    @GetMapping("/my")
    public java.util.List<MenuResponse> myMenus() {
        return menuService.listMyMenus();
    }

    @PreAuthorize("hasAnyRole('OPS_ADMIN','AUDITOR')")
    @GetMapping
    public Page<MenuResponse> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size
    ) {
        return menuService.list(keyword, filter, PageRequest.of(page, size));
    }
}
