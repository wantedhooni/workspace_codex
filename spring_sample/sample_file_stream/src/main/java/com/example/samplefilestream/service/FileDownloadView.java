package com.example.samplefilestream.service;

import com.example.samplefilestream.file.domain.FileMetadata;
import com.example.samplefilestream.storage.DownloadedContent;

public record FileDownloadView(FileMetadata metadata, DownloadedContent downloadedContent) {
}
