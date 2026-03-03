package com.example.sampleredisson.counter;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/counters")
public class CounterController {

    private final CounterService counterService;

    public CounterController(CounterService counterService) {
        this.counterService = counterService;
    }

    @GetMapping("/{name}")
    public CounterResponse get(@PathVariable String name) {
        return counterService.get(name);
    }

    @PostMapping("/{name}/increment")
    public CounterResponse increment(
            @PathVariable String name,
            @RequestParam(defaultValue = "1") long delta
    ) {
        return counterService.increment(name, delta);
    }
}
