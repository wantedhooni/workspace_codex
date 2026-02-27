package com.tradingmacro.macro;

import com.tradingmacro.strategy.StrategyRepository;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/macros")
public class MacroJobController {
    private final MacroJobRepository repository;
    private final StrategyRepository strategyRepository;

    public MacroJobController(MacroJobRepository repository, StrategyRepository strategyRepository) {
        this.repository = repository;
        this.strategyRepository = strategyRepository;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<MacroJob> list() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public MacroJob get(@PathVariable Long id) {
        return repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Macro job not found"));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public MacroJob create(@Valid @RequestBody MacroJobRequest request) {
        MacroJob job = new MacroJob();
        applyRequest(job, request);
        return repository.save(job);
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public MacroJob update(@PathVariable Long id, @Valid @RequestBody MacroJobRequest request) {
        MacroJob job = get(id);
        applyRequest(job, request);
        return repository.save(job);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public void delete(@PathVariable Long id) {
        repository.deleteById(id);
    }

    private void applyRequest(MacroJob job, MacroJobRequest request) {
        job.setName(request.name());
        job.setSchedule(request.schedule());
        job.setStatus(request.status());
        job.setLastRun(request.lastRun());
        job.setNextRun(request.nextRun());
        if (request.strategyId() != null) {
            job.setStrategy(strategyRepository.findById(request.strategyId())
                .orElseThrow(() -> new IllegalArgumentException("Strategy not found")));
        } else {
            job.setStrategy(null);
        }
    }
}
