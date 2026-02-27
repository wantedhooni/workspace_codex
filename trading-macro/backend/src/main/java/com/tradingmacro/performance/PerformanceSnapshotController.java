package com.tradingmacro.performance;

import com.tradingmacro.portfolio.PortfolioRepository;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/performance")
public class PerformanceSnapshotController {
    private final PerformanceSnapshotRepository repository;
    private final PortfolioRepository portfolioRepository;

    public PerformanceSnapshotController(PerformanceSnapshotRepository repository, PortfolioRepository portfolioRepository) {
        this.repository = repository;
        this.portfolioRepository = portfolioRepository;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<PerformanceSnapshot> list() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public PerformanceSnapshot get(@PathVariable Long id) {
        return repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Performance snapshot not found"));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public PerformanceSnapshot create(@Valid @RequestBody PerformanceSnapshotRequest request) {
        PerformanceSnapshot snapshot = new PerformanceSnapshot();
        applyRequest(snapshot, request);
        return repository.save(snapshot);
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public PerformanceSnapshot update(@PathVariable Long id, @Valid @RequestBody PerformanceSnapshotRequest request) {
        PerformanceSnapshot snapshot = get(id);
        applyRequest(snapshot, request);
        return repository.save(snapshot);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public void delete(@PathVariable Long id) {
        repository.deleteById(id);
    }

    private void applyRequest(PerformanceSnapshot snapshot, PerformanceSnapshotRequest request) {
        snapshot.setSnapshotDate(request.snapshotDate());
        snapshot.setPnl(request.pnl());
        snapshot.setReturnPct(request.returnPct());
        snapshot.setDrawdownPct(request.drawdownPct());
        snapshot.setSharpeRatio(request.sharpeRatio());
        snapshot.setVolatilityPct(request.volatilityPct());
        if (request.portfolioId() != null) {
            snapshot.setPortfolio(portfolioRepository.findById(request.portfolioId())
                .orElseThrow(() -> new IllegalArgumentException("Portfolio not found")));
        } else {
            snapshot.setPortfolio(null);
        }
    }
}
