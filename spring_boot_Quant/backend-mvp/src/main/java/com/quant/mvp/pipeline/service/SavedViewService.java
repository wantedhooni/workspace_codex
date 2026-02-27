package com.quant.mvp.pipeline.service;

import java.time.Instant;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Service;

@Service
public class SavedViewService {

    private final AtomicLong viewSeq = new AtomicLong(1000);
    private final Map<Long, SavedView> viewStore = new ConcurrentHashMap<>();
    private final Map<String, DefaultPinnedView> defaultViewStore = new ConcurrentHashMap<>();

    public SavedViewService() {
        seed();
    }

    public List<SavedView> search(String resourceKey, String ownerEmail) {
        String normalizedResourceKey = normalizeText(resourceKey);
        String normalizedOwnerEmail = normalizeText(ownerEmail);

        return viewStore.values().stream()
                .filter(view -> normalizedResourceKey == null || view.resourceKey().equalsIgnoreCase(normalizedResourceKey))
                .filter(view -> view.shared() || (normalizedOwnerEmail != null && view.ownerEmail().equalsIgnoreCase(normalizedOwnerEmail)))
                .sorted(Comparator.comparing(SavedView::resourceKey)
                        .thenComparing(SavedView::viewName)
                        .thenComparing(SavedView::viewId))
                .toList();
    }

    public SavedView require(Long viewId) {
        if (viewId == null || viewId <= 0) {
            throw new IllegalArgumentException("viewId is required");
        }
        SavedView view = viewStore.get(viewId);
        if (view == null) {
            throw new IllegalArgumentException("saved view not found: " + viewId);
        }
        return view;
    }

    public Optional<DefaultPinnedView> getDefault(String resourceKey, String actorEmail) {
        String normalizedResourceKey = requireText(resourceKey, "resourceKey").toLowerCase(Locale.ROOT);
        String normalizedActorEmail = requireText(actorEmail, "actorEmail").toLowerCase(Locale.ROOT);
        DefaultPinnedView pinned = defaultViewStore.get(defaultKey(normalizedActorEmail, normalizedResourceKey));
        if (pinned == null) {
            return Optional.empty();
        }

        SavedView target = viewStore.get(pinned.viewId());
        if (target == null) {
            defaultViewStore.remove(defaultKey(normalizedActorEmail, normalizedResourceKey));
            return Optional.empty();
        }
        if (!canAccess(target, normalizedActorEmail)) {
            return Optional.empty();
        }
        return Optional.of(pinned.withView(target));
    }

    public synchronized DefaultPinnedView pinDefault(
            String resourceKey,
            Long viewId,
            String actorEmail
    ) {
        String normalizedResourceKey = requireText(resourceKey, "resourceKey").toLowerCase(Locale.ROOT);
        String normalizedActorEmail = requireText(actorEmail, "actorEmail").toLowerCase(Locale.ROOT);
        SavedView target = require(viewId);

        if (!target.resourceKey().equalsIgnoreCase(normalizedResourceKey)) {
            throw new IllegalArgumentException("view resourceKey mismatch: " + target.resourceKey());
        }
        if (!canAccess(target, normalizedActorEmail)) {
            throw new IllegalArgumentException("cannot pin inaccessible saved view");
        }

        DefaultPinnedView pinned = new DefaultPinnedView(
                normalizedResourceKey,
                target.viewId(),
                target.viewName(),
                target.shared(),
                target.ownerEmail(),
                Instant.now()
        );
        defaultViewStore.put(defaultKey(normalizedActorEmail, normalizedResourceKey), pinned);
        return pinned;
    }

    public synchronized SavedView create(
            String resourceKey,
            String viewName,
            String description,
            Boolean shared,
            String ownerEmail,
            Map<String, String> filters
    ) {
        String normalizedResourceKey = requireText(resourceKey, "resourceKey").toLowerCase(Locale.ROOT);
        String normalizedViewName = requireText(viewName, "viewName");
        String normalizedOwnerEmail = requireText(ownerEmail, "ownerEmail").toLowerCase(Locale.ROOT);
        String normalizedDescription = normalizeText(description);
        Instant now = Instant.now();

        Long viewId = viewSeq.incrementAndGet();
        SavedView view = new SavedView(
                viewId,
                normalizedResourceKey,
                normalizedViewName,
                normalizedDescription,
                Boolean.TRUE.equals(shared),
                normalizedOwnerEmail,
                normalizeFilters(filters),
                now,
                now
        );
        viewStore.put(viewId, view);
        return view;
    }

    public synchronized SavedView delete(Long viewId, String actorEmail, boolean adminOverride) {
        SavedView current = require(viewId);
        String normalizedActorEmail = requireText(actorEmail, "actorEmail").toLowerCase(Locale.ROOT);

        if (!adminOverride && !current.ownerEmail().equalsIgnoreCase(normalizedActorEmail)) {
            throw new IllegalArgumentException("cannot delete another user's saved view");
        }

        SavedView deleted = viewStore.remove(viewId);
        if (deleted == null) {
            throw new IllegalArgumentException("saved view not found: " + viewId);
        }
        defaultViewStore.entrySet().removeIf(entry -> Objects.equals(entry.getValue().viewId(), viewId));
        return deleted;
    }

    private Map<String, String> normalizeFilters(Map<String, String> filters) {
        if (filters == null || filters.isEmpty()) {
            return Map.of();
        }

        Map<String, String> compact = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : filters.entrySet()) {
            String key = normalizeText(entry.getKey());
            if (key == null) {
                continue;
            }
            String value = normalizeText(entry.getValue());
            if (value == null) {
                continue;
            }
            compact.put(key, value);
        }
        return Map.copyOf(compact);
    }

    private void seed() {
        seedView(
                1L,
                "orders",
                "Open Orders",
                "미체결 중심 주문 모니터링",
                true,
                "admin@quant.io",
                Map.of("status", "NEW")
        );
        seedView(
                2L,
                "orders",
                "Partial Fill Focus",
                "부분체결 주문 추적",
                true,
                "admin@quant.io",
                Map.of("status", "PARTIAL")
        );
        seedView(
                3L,
                "orders",
                "AAPL Desk",
                "AAPL 전용 운용 뷰",
                false,
                "trader@quant.io",
                Map.of("symbol", "AAPL")
        );
    }

    private void seedView(
            Long viewId,
            String resourceKey,
            String viewName,
            String description,
            boolean shared,
            String ownerEmail,
            Map<String, String> filters
    ) {
        viewSeq.updateAndGet(current -> Math.max(current, viewId));
        Instant now = Instant.now();
        viewStore.put(
                viewId,
                new SavedView(
                        viewId,
                        resourceKey.toLowerCase(Locale.ROOT),
                        viewName,
                        description,
                        shared,
                        ownerEmail.toLowerCase(Locale.ROOT),
                        normalizeFilters(filters),
                        now,
                        now
                )
        );
    }

    private String requireText(String value, String fieldName) {
        String normalized = normalizeText(value);
        if (normalized == null) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return normalized;
    }

    private String normalizeText(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private boolean canAccess(SavedView view, String actorEmail) {
        return Boolean.TRUE.equals(view.shared()) || view.ownerEmail().equalsIgnoreCase(actorEmail);
    }

    private String defaultKey(String actorEmail, String resourceKey) {
        return actorEmail.toLowerCase(Locale.ROOT) + "::" + resourceKey.toLowerCase(Locale.ROOT);
    }

    public record SavedView(
            Long viewId,
            String resourceKey,
            String viewName,
            String description,
            Boolean shared,
            String ownerEmail,
            Map<String, String> filters,
            Instant createdAt,
            Instant updatedAt
    ) {
        public SavedView {
            Objects.requireNonNull(viewId, "viewId");
            Objects.requireNonNull(resourceKey, "resourceKey");
            Objects.requireNonNull(viewName, "viewName");
            Objects.requireNonNull(shared, "shared");
            Objects.requireNonNull(ownerEmail, "ownerEmail");
            Objects.requireNonNull(filters, "filters");
            Objects.requireNonNull(createdAt, "createdAt");
            Objects.requireNonNull(updatedAt, "updatedAt");
        }
    }

    public record DefaultPinnedView(
            String resourceKey,
            Long viewId,
            String viewName,
            Boolean shared,
            String ownerEmail,
            Instant pinnedAt
    ) {
        public DefaultPinnedView {
            Objects.requireNonNull(resourceKey, "resourceKey");
            Objects.requireNonNull(viewId, "viewId");
            Objects.requireNonNull(viewName, "viewName");
            Objects.requireNonNull(shared, "shared");
            Objects.requireNonNull(ownerEmail, "ownerEmail");
            Objects.requireNonNull(pinnedAt, "pinnedAt");
        }

        private DefaultPinnedView withView(SavedView savedView) {
            return new DefaultPinnedView(
                    savedView.resourceKey(),
                    savedView.viewId(),
                    savedView.viewName(),
                    savedView.shared(),
                    savedView.ownerEmail(),
                    pinnedAt
            );
        }
    }
}
