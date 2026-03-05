package com.example.samplefilestream.storage;

import org.springframework.core.io.Resource;

public record DownloadedContent(Resource resource, long contentLength, String contentType) {
}
