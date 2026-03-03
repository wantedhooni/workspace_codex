package com.example.sampleredisson.counter;

public record CounterResponse(
        String name,
        long value
) {
}
