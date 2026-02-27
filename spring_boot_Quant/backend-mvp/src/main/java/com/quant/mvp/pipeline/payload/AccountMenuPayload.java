package com.quant.mvp.pipeline.payload;

import java.util.List;

public final class AccountMenuPayload {

    private AccountMenuPayload() {
    }

    public record Item(
            Long menuId,
            String menuKey,
            String menuLabel,
            String path,
            String icon,
            Integer sortOrder,
            boolean canRead,
            boolean canCreate,
            boolean canUpdate,
            boolean canDelete
    ) {
    }

    public record Res(
            List<Item> items
    ) {
    }
}
