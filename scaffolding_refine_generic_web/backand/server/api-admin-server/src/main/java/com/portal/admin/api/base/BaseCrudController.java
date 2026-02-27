package com.portal.admin.api.base;

import com.portal.admin.service.base.CrudService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

public abstract class BaseCrudController<ID, C, U, R> {

    protected final CrudService<ID, C, U, R> service;

    protected BaseCrudController(CrudService<ID, C, U, R> service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "List items")
    @ApiResponse(responseCode = "200", description = "Success")
    public PagedResponse<R> list(
            @RequestParam(name = "searchParam", required = false) String searchParam,
            @RequestParam Map<String, String> requestParams
    ) {
        return service.list(searchParam, requestParams);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get item by id")
    @ApiResponse(responseCode = "200", description = "Success")
    public R get(@PathVariable ID id) {
        return service.get(id);
    }

    @PostMapping
    @Operation(summary = "Create item")
    @ApiResponse(responseCode = "200", description = "Success")
    public R create(@Valid @RequestBody C request) {
        return service.create(request);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update item")
    @ApiResponse(responseCode = "200", description = "Success")
    public R update(@PathVariable ID id, @RequestBody U request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete item")
    @ApiResponse(responseCode = "204", description = "Deleted")
    public void delete(@PathVariable ID id) {
        service.delete(id);
    }
}
