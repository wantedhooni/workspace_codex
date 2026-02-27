package com.quant.mvp.api;

import com.quant.mvp.pipeline.domain.PermissionAction;
import com.quant.mvp.pipeline.payload.GlobalSearchPayload;
import com.quant.mvp.pipeline.service.GlobalSearchService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/search")
public class GlobalSearchController {

    private final GlobalSearchService globalSearchService;
    private final PermissionGuard permissionGuard;

    public GlobalSearchController(
            GlobalSearchService globalSearchService,
            PermissionGuard permissionGuard
    ) {
        this.globalSearchService = globalSearchService;
        this.permissionGuard = permissionGuard;
    }

    @GetMapping("/global")
    public GlobalSearchPayload.Res search(
            @RequestHeader(name = PermissionGuard.USER_HEADER, required = false) String userEmail,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Integer limit
    ) {
        String actor = permissionGuard.resolveUserEmail(userEmail);
        permissionGuard.require(actor, "accountProfile", PermissionAction.READ);

        GlobalSearchService.GlobalSearchSnapshot snapshot = globalSearchService.search(actor, q, limit);
        return new GlobalSearchPayload.Res(
                snapshot.query(),
                snapshot.limit(),
                snapshot.sections().stream()
                        .map(section -> new GlobalSearchPayload.Section(
                                section.resourceKey(),
                                section.resourceLabel(),
                                section.totalCount(),
                                section.items().stream()
                                        .map(item -> new GlobalSearchPayload.Item(
                                                item.resourceKey(),
                                                item.itemKey(),
                                                item.title(),
                                                item.subtitle(),
                                                item.path(),
                                                item.score()
                                        ))
                                        .toList()
                        ))
                        .toList()
        );
    }
}
