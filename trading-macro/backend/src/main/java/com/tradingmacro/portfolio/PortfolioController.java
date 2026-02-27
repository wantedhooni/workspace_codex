package com.tradingmacro.portfolio;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/portfolios")
public class PortfolioController {
    private final PortfolioRepository repository;

    public PortfolioController(PortfolioRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','TRADER')")
    public List<Portfolio> list() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TRADER')")
    public Portfolio get(@PathVariable Long id) {
        return repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Portfolio not found"));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','TRADER')")
    public Portfolio create(@Valid @RequestBody Portfolio portfolio) {
        return repository.save(portfolio);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','TRADER')")
    public Portfolio update(@PathVariable Long id, @Valid @RequestBody Portfolio updated) {
        Portfolio current = get(id);
        current.setName(updated.getName());
        current.setBaseCurrency(updated.getBaseCurrency());
        current.setTotalValue(updated.getTotalValue());
        current.setUpdatedAt(updated.getUpdatedAt());
        return repository.save(current);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) {
        repository.deleteById(id);
    }
}
