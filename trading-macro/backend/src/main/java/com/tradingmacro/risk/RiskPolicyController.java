package com.tradingmacro.risk;

import com.tradingmacro.portfolio.PortfolioRepository;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/risk-policies")
public class RiskPolicyController {
    private final RiskPolicyRepository repository;
    private final PortfolioRepository portfolioRepository;

    public RiskPolicyController(RiskPolicyRepository repository, PortfolioRepository portfolioRepository) {
        this.repository = repository;
        this.portfolioRepository = portfolioRepository;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<RiskPolicy> list() {
        return repository.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public RiskPolicy get(@PathVariable Long id) {
        return repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Risk policy not found"));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public RiskPolicy create(@Valid @RequestBody RiskPolicyRequest request) {
        RiskPolicy policy = new RiskPolicy();
        applyRequest(policy, request);
        return repository.save(policy);
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public RiskPolicy update(@PathVariable Long id, @Valid @RequestBody RiskPolicyRequest request) {
        RiskPolicy policy = get(id);
        applyRequest(policy, request);
        return repository.save(policy);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public void delete(@PathVariable Long id) {
        repository.deleteById(id);
    }

    private void applyRequest(RiskPolicy policy, RiskPolicyRequest request) {
        policy.setName(request.name());
        policy.setMaxDailyLoss(request.maxDailyLoss());
        policy.setMaxPositionSize(request.maxPositionSize());
        policy.setMaxLeverage(request.maxLeverage());
        policy.setAllowedAssetClasses(request.allowedAssetClasses());
        policy.setStatus(request.status());
        if (request.updatedAt() != null) {
            policy.setUpdatedAt(request.updatedAt());
        }
        if (request.portfolioId() != null) {
            policy.setPortfolio(portfolioRepository.findById(request.portfolioId())
                .orElseThrow(() -> new IllegalArgumentException("Portfolio not found")));
        } else {
            policy.setPortfolio(null);
        }
    }
}
