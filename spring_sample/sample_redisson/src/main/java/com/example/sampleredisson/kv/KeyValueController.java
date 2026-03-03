package com.example.sampleredisson.kv;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/kv")
public class KeyValueController {

    private final KeyValueService keyValueService;

    public KeyValueController(KeyValueService keyValueService) {
        this.keyValueService = keyValueService;
    }

    @PutMapping("/{key}")
    public KeyValueResponse upsert(
            @PathVariable String key,
            @Valid @RequestBody KeyValueUpsertRequest request
    ) {
        return keyValueService.save(key, request.value(), request.ttlSeconds());
    }

    @GetMapping("/{key}")
    public KeyValueResponse get(@PathVariable String key) {
        return keyValueService.find(key)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "데이터를 찾을 수 없습니다."));
    }

    @DeleteMapping("/{key}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String key) {
        if (!keyValueService.delete(key)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "삭제할 데이터가 없습니다.");
        }
    }
}
