package com.curd.template.api.item;

import com.curd.template.app.item.CreateItemCommand;
import com.curd.template.app.item.ItemCrudService;
import com.curd.template.app.item.ItemResult;
import com.curd.template.app.item.ItemSearchQuery;
import com.curd.template.app.item.PatchItemCommand;
import com.curd.template.app.item.UpdateItemCommand;
import com.curd.template.core.filter.FilterExpression;
import com.curd.template.core.filter.FilterOperation;
import com.curd.template.core.filter.FilterPolicy;
import com.curd.template.core.filter.FilterPolicyRegistry;
import com.curd.template.core.filter.FilterPolicyValidator;
import com.curd.template.core.filter.FilterQueryParser;
import com.curd.template.core.filter.FilterUsageGuard;
import com.curd.template.core.filter.PageablePolicy;
import com.curd.template.core.sort.SortParser;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/items")
public class ItemController {

    private static final String RESOURCE_NAME = "items";

    private final ItemCrudService service;
    private final FilterQueryParser filterQueryParser;
    private final FilterPolicyValidator filterPolicyValidator;
    private final FilterPolicyRegistry filterPolicyRegistry;
    private final SortParser sortParser;
    private final PageablePolicy pageablePolicy;

    public ItemController(
        ItemCrudService service,
        FilterQueryParser filterQueryParser,
        FilterPolicyValidator filterPolicyValidator,
        FilterPolicyRegistry filterPolicyRegistry,
        SortParser sortParser,
        PageablePolicy pageablePolicy
    ) {
        this.service = service;
        this.filterQueryParser = filterQueryParser;
        this.filterPolicyValidator = filterPolicyValidator;
        this.filterPolicyRegistry = filterPolicyRegistry;
        this.sortParser = sortParser;
        this.pageablePolicy = pageablePolicy;
    }

    @GetMapping
    public ResponseEntity<PagedResponse<ItemResponse>> search(
        @RequestParam(name = "and", required = false) List<String> andParams,
        @RequestParam(name = "sort", required = false) List<String> sortParams,
        @RequestParam(name = "page", defaultValue = "0") int page,
        @RequestParam(name = "size", defaultValue = "20") int size
    ) {
        FilterPolicy policy = filterPolicyRegistry.policyFor(RESOURCE_NAME, FilterOperation.READ);

        FilterExpression expression = filterQueryParser.parse(andParams);
        filterPolicyValidator.validate(expression, policy);

        Sort sort = sortParser.parse(sortParams, policy.sortableFields());
        Pageable pageable = pageablePolicy.build(page, size, policy.maxPageSize(), sort);

        Page<ItemResponse> result = service.search(new ItemSearchQuery(expression), pageable).map(ItemResponseMapper::from);
        PagedResponse<ItemResponse> body = new PagedResponse<>(
            result.getContent(),
            result.getNumber(),
            result.getSize(),
            result.getTotalElements(),
            result.getTotalPages(),
            result.isFirst(),
            result.isLast()
        );

        return ResponseEntity.ok(body);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemResponse> getById(@PathVariable("id") String id) {
        return ResponseEntity.ok(ItemResponseMapper.from(service.get(id)));
    }

    @PostMapping
    public ResponseEntity<ItemResponse> create(
        @Valid @RequestBody CreateItemRequest request,
        @RequestParam MultiValueMap<String, String> queryParams
    ) {
        FilterUsageGuard.assertNoFilterParameters(queryParams);

        ItemResult created = service.create(new CreateItemCommand(
            request.name(),
            request.description(),
            request.price(),
            request.status()
        ));
        return ResponseEntity.created(URI.create("/api/v1/items/" + created.id())).body(ItemResponseMapper.from(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ItemResponse> update(
        @PathVariable("id") String id,
        @Valid @RequestBody UpdateItemRequest request,
        @RequestParam MultiValueMap<String, String> queryParams
    ) {
        FilterUsageGuard.assertNoFilterParameters(queryParams);

        ItemResult updated = service.update(id, new UpdateItemCommand(
            request.name(),
            request.description(),
            request.price(),
            request.status()
        ));
        return ResponseEntity.ok(ItemResponseMapper.from(updated));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ItemResponse> patch(
        @PathVariable("id") String id,
        @Valid @RequestBody PatchItemRequest request,
        @RequestParam MultiValueMap<String, String> queryParams
    ) {
        FilterUsageGuard.assertNoFilterParameters(queryParams);

        ItemResult updated = service.patch(id, new PatchItemCommand(
            request.name(),
            request.description(),
            request.price(),
            request.status()
        ));
        return ResponseEntity.ok(ItemResponseMapper.from(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
        @PathVariable("id") String id,
        @RequestParam Map<String, String> queryParams
    ) {
        FilterUsageGuard.assertNoFilterParameters(queryParams);
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
