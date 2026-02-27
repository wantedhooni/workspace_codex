package com.tradingmacro.org;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teams")
public class TeamController {
    private final TeamRepository repository;

    public TeamController(TeamRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','TRADER')")
    public List<Team> list() {
        return repository.findAll();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Team create(@Valid @RequestBody Team team) {
        return repository.save(team);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Team update(@PathVariable Long id, @Valid @RequestBody Team updated) {
        Team current = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Team not found"));
        current.setName(updated.getName());
        return repository.save(current);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) {
        repository.deleteById(id);
    }
}
