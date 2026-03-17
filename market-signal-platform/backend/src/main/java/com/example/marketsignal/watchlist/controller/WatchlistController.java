package com.example.marketsignal.watchlist;

import com.example.marketsignal.user.CurrentUser;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * 관심 종목 API를 제공한다.
 */
@RestController
@RequestMapping("/api/watchlists")
@RequiredArgsConstructor
public class WatchlistController {

    private final WatchlistService watchlistService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WatchlistResponse create(
            CurrentUser currentUser,
            @Valid @RequestBody WatchlistCreateRequest request
    ) {
        return watchlistService.create(currentUser, request);
    }

    @GetMapping
    public List<WatchlistResponse> getAll(CurrentUser currentUser, @RequestParam(required = false) String query) {
        return watchlistService.getAll(currentUser, query);
    }

    @GetMapping("/coverage")
    public List<WatchlistCoverageResponse> getCoverage(
            CurrentUser currentUser,
            @RequestParam(required = false) String query
    ) {
        return watchlistService.getCoverage(currentUser, query);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(CurrentUser currentUser, @PathVariable Long id) {
        watchlistService.delete(currentUser, id);
    }
}
