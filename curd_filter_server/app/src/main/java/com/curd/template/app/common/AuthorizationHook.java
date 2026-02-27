package com.curd.template.app.common;

@FunctionalInterface
public interface AuthorizationHook {

    void authorize(String operation, Object context);

    static AuthorizationHook noop() {
        return (operation, context) -> {
        };
    }
}
