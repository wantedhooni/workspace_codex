package com.quant.mvp.pipeline.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.Map;
import org.junit.jupiter.api.Test;

class SavedViewServiceTest {

    @Test
    void searchReturnsSharedAndOwnViews() {
        SavedViewService service = new SavedViewService();

        var traderViews = service.search("orders", "trader@quant.io");
        assertTrue(traderViews.stream().anyMatch(view -> Boolean.TRUE.equals(view.shared())));
        assertTrue(traderViews.stream().anyMatch(view -> "trader@quant.io".equalsIgnoreCase(view.ownerEmail())));
    }

    @Test
    void createAndDeleteView() {
        SavedViewService service = new SavedViewService();

        var created = service.create(
                "orders",
                "JUnit View",
                "test view",
                false,
                "admin@quant.io",
                Map.of("symbol", "NVDA", "status", "NEW")
        );

        assertEquals("orders", created.resourceKey());
        assertEquals("NVDA", created.filters().get("symbol"));

        assertThrows(
                IllegalArgumentException.class,
                () -> service.delete(created.viewId(), "trader@quant.io", false)
        );

        var deleted = service.delete(created.viewId(), "admin@quant.io", true);
        assertEquals(created.viewId(), deleted.viewId());
    }

    @Test
    void pinDefaultRequiresAccessibleView() {
        SavedViewService service = new SavedViewService();

        var pinned = service.pinDefault("orders", 1L, "viewer@quant.io");
        assertEquals(1L, pinned.viewId());
        assertTrue(service.getDefault("orders", "viewer@quant.io").isPresent());

        assertThrows(
                IllegalArgumentException.class,
                () -> service.pinDefault("orders", 3L, "viewer@quant.io")
        );

        assertFalse(service.getDefault("trades", "viewer@quant.io").isPresent());
    }
}
