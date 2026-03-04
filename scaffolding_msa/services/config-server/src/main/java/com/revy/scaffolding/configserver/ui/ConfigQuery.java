package com.revy.scaffolding.configserver.ui;

public record ConfigQuery(
    String application,
    String profile,
    String label
) {
    public static ConfigQuery defaults() {
        return new ConfigQuery("", "", "");
    }
}

