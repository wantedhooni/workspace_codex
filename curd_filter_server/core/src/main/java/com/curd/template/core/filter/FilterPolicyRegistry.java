package com.curd.template.core.filter;

public interface FilterPolicyRegistry {
    FilterPolicy policyFor(String resource, FilterOperation operation);
}
