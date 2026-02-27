package com.tradingmacro.strategy;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/strategies")
public class StrategyController {
    private final StrategyRepository repository;

    public StrategyController(StrategyRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','TRADER')")
    public List<Strategy> list() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TRADER')")
    public Strategy get(@PathVariable Long id) {
        return repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Strategy not found"));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','TRADER')")
    public Strategy create(@Valid @RequestBody Strategy strategy) {
        return repository.save(strategy);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TRADER')")
    public Strategy update(@PathVariable Long id, @Valid @RequestBody Strategy updated) {
        Strategy current = get(id);
        current.setName(updated.getName());
        current.setDescription(updated.getDescription());
        current.setAssetClass(updated.getAssetClass());
        current.setTimeframe(updated.getTimeframe());
        current.setRiskLevel(updated.getRiskLevel());
        current.setStatus(updated.getStatus());
        return repository.save(current);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) {
        repository.deleteById(id);
    }
}
