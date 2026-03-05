package com.example.samplefilestream.config;

import com.example.samplefilestream.file.domain.FileStorageType;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.storage")
public class StorageProperties {

    private FileStorageType defaultType = FileStorageType.LOCAL;

    private final Local local = new Local();

    private final S3 s3 = new S3();

    public FileStorageType getDefaultType() {
        return defaultType;
    }

    public void setDefaultType(FileStorageType defaultType) {
        this.defaultType = defaultType;
    }

    public Local getLocal() {
        return local;
    }

    public S3 getS3() {
        return s3;
    }

    public static class Local {

        private String basePath = "./storage/files";

        public String getBasePath() {
            return basePath;
        }

        public void setBasePath(String basePath) {
            this.basePath = basePath;
        }
    }

    public static class S3 {

        private boolean enabled;
        private String bucket = "sample-file-stream";
        private String region = "ap-northeast-2";
        private String endpoint = "";
        private boolean pathStyleAccess = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getBucket() {
            return bucket;
        }

        public void setBucket(String bucket) {
            this.bucket = bucket;
        }

        public String getRegion() {
            return region;
        }

        public void setRegion(String region) {
            this.region = region;
        }

        public String getEndpoint() {
            return endpoint;
        }

        public void setEndpoint(String endpoint) {
            this.endpoint = endpoint;
        }

        public boolean isPathStyleAccess() {
            return pathStyleAccess;
        }

        public void setPathStyleAccess(boolean pathStyleAccess) {
            this.pathStyleAccess = pathStyleAccess;
        }
    }
}
