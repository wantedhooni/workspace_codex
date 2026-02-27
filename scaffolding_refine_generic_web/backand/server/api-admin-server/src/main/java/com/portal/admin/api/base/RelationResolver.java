package com.portal.admin.api.base;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class RelationResolver {

    private RelationResolver() {}

    public static <E> Set<E> resolveRequired(
            JpaRepository<E, Long> repository,
            List<Long> ids,
            String fieldName
    ) {
        if (ids == null || ids.isEmpty()) {
            return Set.of();
        }

        Set<Long> uniqueIds = new HashSet<>(ids);
        List<E> entities = repository.findAllById(uniqueIds);
        if (entities.size() != uniqueIds.size()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Some " + fieldName + " were not found"
            );
        }

        return new LinkedHashSet<>(entities);
    }
}
