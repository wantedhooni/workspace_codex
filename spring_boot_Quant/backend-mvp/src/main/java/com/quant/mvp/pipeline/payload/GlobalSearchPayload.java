package com.quant.mvp.pipeline.payload;

import java.util.List;

public final class GlobalSearchPayload {

    private GlobalSearchPayload() {
    }

    public record Item(
            String resourceKey,
            String itemKey,
            String title,
            String subtitle,
            String path,
            Integer score
    ) {
    }

    public record Section(
            String resourceKey,
            String resourceLabel,
            Integer totalCount,
            List<Item> items
    ) {
    }

    public record Res(
            String query,
            Integer limit,
            List<Section> sections
    ) {
    }
}
