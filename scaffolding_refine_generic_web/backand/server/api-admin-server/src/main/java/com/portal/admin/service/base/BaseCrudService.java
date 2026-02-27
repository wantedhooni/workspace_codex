package com.portal.admin.service.base;

import com.portal.admin.api.base.PagedResponse;
import com.portal.admin.domain.ServiceAuditLog;
import com.portal.admin.repo.ServiceAuditLogRepository;
import com.portal.admin.repo.search.FieldSearchableRepository;
import com.portal.admin.repo.search.SearchableRepository;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class BaseCrudService<E, ID, C, U, R> implements CrudService<ID, C, U, R> {

    private static final Logger log = LoggerFactory.getLogger(BaseCrudService.class);
    private static final int MAX_PAGE_SIZE = 100;

    protected final JpaRepository<E, ID> repository;
    protected final ServiceAuditLogRepository auditLogs;

    protected BaseCrudService(JpaRepository<E, ID> repository, ServiceAuditLogRepository auditLogs) {
        this.repository = repository;
        this.auditLogs = auditLogs;
    }

    @Override
    public PagedResponse<R> list(String searchParam, Map<String, String> requestParams) {
        Map<String, String> fieldFilters = resolveFieldFilters(requestParams);
        validateFilterKeys(fieldFilters);

        int page = parsePositiveInt(requestParams.get("page"), 1);
        int perPage = parsePageSize(requestParams.get("perPage"), 20);
        String sortField = requestParams.getOrDefault("sort", "id");
        boolean ascending = !"DESC".equalsIgnoreCase(requestParams.get("order"));

        Pageable pageable = PageRequest.of(page - 1, perPage, buildSort(sortField, ascending));

        Page<E> pageResult = resolvePage(searchParam, fieldFilters, pageable);
        List<R> pagedItems = pageResult.getContent().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return new PagedResponse<>(pagedItems, Math.toIntExact(pageResult.getTotalElements()));
    }

    @Override
    public R get(ID id) {
        return toResponse(findByIdOrThrow(id));
    }

    @Override
    public R create(@Valid C request) {
        E entity = createEntity(request);
        E saved = repository.save(entity);
        writeAudit("CREATE", extractEntityId(saved));
        return toResponse(saved);
    }

    @Override
    public R update(ID id, U request) {
        E entity = findByIdOrThrow(id);
        applyUpdate(entity, request);
        E saved = repository.save(entity);
        writeAudit("UPDATE", extractEntityId(saved));
        return toResponse(saved);
    }

    @Override
    public void delete(ID id) {
        E entity = findByIdOrThrow(id);
        Long entityId = extractEntityId(entity);
        repository.delete(entity);
        writeAudit("DELETE", entityId);
    }

    protected abstract E createEntity(C request);

    protected abstract void applyUpdate(E entity, U request);

    protected abstract R toResponse(E entity);

    protected E findByIdOrThrow(ID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Resource not found: " + id
                ));
    }

    protected boolean shouldAudit(String action) {
        return true;
    }

    protected String auditDomainType() {
        return getClass().getSimpleName().replace("Service", "").toUpperCase(Locale.ROOT);
    }

    private void writeAudit(String action, Long domainId) {
        if (!shouldAudit(action)) {
            return;
        }

        String username = "anonymous";
        String detail = action;

        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs) {
            Object authUsername = attrs.getRequest().getAttribute("auth.username");
            if (authUsername instanceof String raw && !raw.isBlank()) {
                username = raw;
            }
            detail = attrs.getRequest().getMethod() + " " + attrs.getRequest().getRequestURI();
        }

        try {
            auditLogs.save(new ServiceAuditLog(
                    auditDomainType(),
                    domainId == null ? 0L : domainId,
                    action,
                    username,
                    detail
            ));
        } catch (Exception exception) {
            log.warn("Failed to persist service audit log: domain={}, action={}", auditDomainType(), action, exception);
        }
    }

    private Long extractEntityId(E entity) {
        if (entity == null) {
            return null;
        }

        try {
            Object value = entity.getClass().getMethod("getId").invoke(entity);
            if (value instanceof Number number) {
                return number.longValue();
            }
        } catch (ReflectiveOperationException ignored) {
            // audit still runs with domainId=0 when entity has no accessible id getter
        }

        return null;
    }

    private Map<String, String> resolveFieldFilters(Map<String, String> requestParams) {
        return requestParams.entrySet().stream()
                .filter(entry -> !isReservedQueryParam(entry.getKey()))
                .filter(entry -> entry.getValue() != null && !entry.getValue().isBlank())
                .collect(java.util.stream.Collectors.toMap(Map.Entry::getKey, entry -> entry.getValue().trim()));
    }

    @SuppressWarnings("unchecked")
    private Page<E> resolvePage(String searchParam, Map<String, String> fieldFilters, Pageable pageable) {
        if (!fieldFilters.isEmpty() && repository instanceof FieldSearchableRepository<?> fieldSearchable) {
            return ((FieldSearchableRepository<E>) fieldSearchable).searchByFields(fieldFilters, pageable);
        }

        if (searchParam != null && !searchParam.isBlank() && repository instanceof SearchableRepository<?> searchable) {
            return ((SearchableRepository<E>) searchable).search(searchParam.trim(), pageable);
        }

        return repository.findAll(pageable);
    }

    private boolean isReservedQueryParam(String key) {
        return switch (key) {
            case "page", "perPage", "sort", "order", "ids", "searchParam" -> true;
            default -> false;
        };
    }

    private int parsePositiveInt(String rawValue, int defaultValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return defaultValue;
        }
        try {
            int value = Integer.parseInt(rawValue.trim());
            return value > 0 ? value : defaultValue;
        } catch (NumberFormatException ignored) {
            return defaultValue;
        }
    }

    private int parsePageSize(String rawValue, int defaultValue) {
        int parsed = parsePositiveInt(rawValue, defaultValue);
        return Math.min(parsed, MAX_PAGE_SIZE);
    }

    private Sort buildSort(String sortField, boolean ascending) {
        String normalized = sortField == null || sortField.isBlank() ? "id" : sortField.trim();
        Set<String> allowedSortFields = resolveAllowedSortFields();
        if (!allowedSortFields.contains(normalized)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Unsupported sort field: " + normalized + ", allowed: " + String.join(", ", allowedSortFields)
            );
        }

        return Sort.by(ascending ? Sort.Direction.ASC : Sort.Direction.DESC, normalized);
    }

    @SuppressWarnings("unchecked")
    private Set<String> resolveAllowedSortFields() {
        if (repository instanceof SearchableRepository<?> searchable) {
            Set<String> allowed = ((SearchableRepository<E>) searchable).allowedSortFields();
            if (allowed != null && !allowed.isEmpty()) {
                return allowed;
            }
        }
        return Set.of("id");
    }

    @SuppressWarnings("unchecked")
    private void validateFilterKeys(Map<String, String> fieldFilters) {
        if (fieldFilters.isEmpty()) {
            return;
        }

        if (!(repository instanceof FieldSearchableRepository<?> fieldSearchable)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Field filters are not supported for this resource");
        }

        Set<String> allowedFilterFields = ((FieldSearchableRepository<E>) fieldSearchable).allowedFilterFields();
        if (allowedFilterFields == null || allowedFilterFields.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Field filters are not configured for this resource");
        }

        List<String> unsupported = fieldFilters.keySet().stream()
                .filter(key -> !allowedFilterFields.contains(key))
                .sorted()
                .toList();
        if (!unsupported.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Unsupported filter fields: " + String.join(", ", unsupported)
            );
        }
    }
}
