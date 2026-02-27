package com.curd.template.app.filter;

import com.curd.template.core.error.ApiException;
import com.curd.template.core.error.AppErrorCode;
import com.curd.template.core.filter.FilterOperation;
import com.curd.template.core.filter.FilterPolicy;
import com.curd.template.core.filter.FilterPolicyRegistry;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class InMemoryFilterPolicyRegistry implements FilterPolicyRegistry {

    private final Map<String, FilterPolicy> policies = new ConcurrentHashMap<>();

    public void register(String resource, FilterOperation operation, FilterPolicy policy) {
        policies.put(key(resource, operation), policy);
    }

    @Override
    public FilterPolicy policyFor(String resource, FilterOperation operation) {
        FilterPolicy policy = policies.get(key(resource, operation));
        if (policy == null) {
            throw new ApiException(
                AppErrorCode.FILTER_POLICY_VIOLATION,
                "No filter policy registered for resource=" + resource + ", operation=" + operation
            );
        }
        return policy;
    }

    private String key(String resource, FilterOperation operation) {
        return resource + "::" + operation;
    }
}
