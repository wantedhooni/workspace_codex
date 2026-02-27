package com.portal.admin.repo;

import java.time.Instant;
import java.util.List;

public interface AccessLogStatisticsRepository {

    long countSince(Instant from);

    long countSuccessfulActionSince(Instant from, String action);

    List<PathCountResult> findTopPathCounts(int limit);

    List<ActionCountResult> findTopActionCounts(int limit);

    record PathCountResult(String path, long total) {
    }

    record ActionCountResult(String action, long total) {
    }
}
