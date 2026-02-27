package com.tradingmacro.menu;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/menus")
public class MenuController {
    private final MenuRepository repository;

    public MenuController(MenuRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','TRADER')")
    public List<Menu> list() {
        return repository.findAll();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Menu create(@Valid @RequestBody Menu menu) {
        return repository.save(menu);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Menu update(@PathVariable Long id, @Valid @RequestBody Menu updated) {
        Menu current = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Menu not found"));
        current.setCode(updated.getCode());
        current.setName(updated.getName());
        current.setPath(updated.getPath());
        current.setParentId(updated.getParentId());
        current.setSortOrder(updated.getSortOrder());
        current.setStatus(updated.getStatus());
        return repository.save(current);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) {
        repository.deleteById(id);
    }
}
