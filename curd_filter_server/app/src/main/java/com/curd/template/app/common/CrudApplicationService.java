package com.curd.template.app.common;

import com.curd.template.core.error.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public abstract class CrudApplicationService<T, ID, CREATE_CMD, UPDATE_CMD, PATCH_CMD, QUERY, RESULT> {

    private final AuthorizationHook authorizationHook;

    protected CrudApplicationService(AuthorizationHook authorizationHook) {
        this.authorizationHook = authorizationHook == null ? AuthorizationHook.noop() : authorizationHook;
    }

    public RESULT create(CREATE_CMD command) {
        validateCreate(command);
        authorizationHook.authorize("create", command);
        return mapToResult(doCreate(command));
    }

    public RESULT get(ID id) {
        authorizationHook.authorize("get", id);
        return doGet(id).map(this::mapToResult)
            .orElseThrow(() -> new ResourceNotFoundException("Resource not found: " + id));
    }

    public Page<RESULT> search(QUERY query, Pageable pageable) {
        validateSearch(query, pageable);
        authorizationHook.authorize("search", query);
        return doSearch(query, pageable).map(this::mapToResult);
    }

    public RESULT update(ID id, UPDATE_CMD command) {
        validateUpdate(id, command);
        authorizationHook.authorize("update", id);
        return mapToResult(doUpdate(id, command));
    }

    public RESULT patch(ID id, PATCH_CMD command) {
        validatePatch(id, command);
        authorizationHook.authorize("patch", id);
        return mapToResult(doPatch(id, command));
    }

    public void delete(ID id) {
        authorizationHook.authorize("delete", id);
        doDelete(id);
    }

    protected void validateCreate(CREATE_CMD command) {
    }

    protected void validateUpdate(ID id, UPDATE_CMD command) {
    }

    protected void validatePatch(ID id, PATCH_CMD command) {
    }

    protected void validateSearch(QUERY query, Pageable pageable) {
    }

    protected abstract T doCreate(CREATE_CMD command);

    protected abstract java.util.Optional<T> doGet(ID id);

    protected abstract Page<T> doSearch(QUERY query, Pageable pageable);

    protected abstract T doUpdate(ID id, UPDATE_CMD command);

    protected abstract T doPatch(ID id, PATCH_CMD command);

    protected abstract void doDelete(ID id);

    protected abstract RESULT mapToResult(T model);
}
