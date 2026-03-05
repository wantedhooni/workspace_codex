package com.example.samplefilestream.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public final class FileKeyGenerator {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    private FileKeyGenerator() {
    }

    public static String generate(String originalFilename) {
        String sanitized = sanitizeFilename(originalFilename);
        String datePrefix = LocalDate.now().format(DATE_FORMATTER);
        return datePrefix + "/" + UUID.randomUUID() + "-" + sanitized;
    }

    private static String sanitizeFilename(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            return "unnamed.bin";
        }

        String sanitized = originalFilename
            .replace("\\", "_")
            .replace("/", "_")
            .replace("..", "_")
            .replaceAll("[^a-zA-Z0-9._-]", "_");

        if (sanitized.isBlank()) {
            return "unnamed.bin";
        }
        return sanitized;
    }
}
