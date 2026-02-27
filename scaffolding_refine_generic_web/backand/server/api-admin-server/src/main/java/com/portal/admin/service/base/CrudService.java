package com.portal.admin.service.base;

import com.portal.admin.api.base.PagedResponse;

import java.util.Map;

public interface CrudService<ID, C, U, R> {

    PagedResponse<R> list(String searchParam, Map<String, String> requestParams);

    R get(ID id);

    R create(C request);

    R update(ID id, U request);

    void delete(ID id);
}
