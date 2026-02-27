package com.tradingmacro.org;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/desks")
public class DeskController {
    private final DeskRepository repository;
    private final TeamRepository teamRepository;

    public DeskController(DeskRepository repository, TeamRepository teamRepository) {
        this.repository = repository;
        this.teamRepository = teamRepository;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<Desk> list() {
        return repository.findAll();
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public Desk create(@Valid @RequestBody DeskRequest request) {
        Desk desk = new Desk();
        applyRequest(desk, request);
        return repository.save(desk);
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Desk update(@PathVariable Long id, @Valid @RequestBody DeskRequest request) {
        Desk desk = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Desk not found"));
        applyRequest(desk, request);
        return repository.save(desk);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public void delete(@PathVariable Long id) {
        repository.deleteById(id);
    }

    private void applyRequest(Desk desk, DeskRequest request) {
        desk.setName(request.name());
        if (request.teamId() != null) {
            desk.setTeam(teamRepository.findById(request.teamId())
                .orElseThrow(() -> new IllegalArgumentException("Team not found")));
        } else {
            desk.setTeam(null);
        }
    }
}
