package com.example.sampleredisson.kv;

public record KeyValueResponse(
        String key,
        String value,
        Long ttlSeconds
) {
}
