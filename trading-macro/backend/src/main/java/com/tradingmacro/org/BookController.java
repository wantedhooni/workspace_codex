package com.tradingmacro.org;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/books")
public class BookController {
    private final BookRepository repository;
    private final DeskRepository deskRepository;

    public BookController(BookRepository repository, DeskRepository deskRepository) {
        this.repository = repository;
        this.deskRepository = deskRepository;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<Book> list() {
        return repository.findAll();
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public Book create(@Valid @RequestBody BookRequest request) {
        Book book = new Book();
        applyRequest(book, request);
        return repository.save(book);
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public Book update(@PathVariable Long id, @Valid @RequestBody BookRequest request) {
        Book book = repository.findById(id).orElseThrow(() -> new IllegalArgumentException("Book not found"));
        applyRequest(book, request);
        return repository.save(book);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public void delete(@PathVariable Long id) {
        repository.deleteById(id);
    }

    private void applyRequest(Book book, BookRequest request) {
        book.setName(request.name());
        if (request.deskId() != null) {
            book.setDesk(deskRepository.findById(request.deskId())
                .orElseThrow(() -> new IllegalArgumentException("Desk not found")));
        } else {
            book.setDesk(null);
        }
    }
}
