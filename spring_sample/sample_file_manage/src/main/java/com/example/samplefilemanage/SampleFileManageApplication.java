package com.example.samplefilemanage;

import com.example.samplefilemanage.config.FileStorageProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * 파일 업로드와 다운로드 이력 관리를 제공하는 샘플 애플리케이션이다.
 */
@SpringBootApplication
@EnableConfigurationProperties(FileStorageProperties.class)
public class SampleFileManageApplication {

    public static void main(String[] args) {
        SpringApplication.run(SampleFileManageApplication.class, args);
    }
}
