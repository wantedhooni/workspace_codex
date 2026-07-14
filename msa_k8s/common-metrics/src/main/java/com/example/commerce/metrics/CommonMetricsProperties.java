package com.example.commerce.metrics;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 모든 애플리케이션에 공통으로 적용할 메트릭 태그와 카디널리티 제한을 정의한다.
 */
@ConfigurationProperties("commerce.observability.metrics")
public class CommonMetricsProperties {

    private boolean enabled = true;
    private String environment = "local";
    private int maxUriTags = 200;
    private int maxOperationTags = 100;

    /**
     * 운영에 안전한 기본값으로 공통 메트릭 설정을 생성한다.
     */
    public CommonMetricsProperties() {
    }

    /**
     * 공통 메트릭 자동 구성 활성화 여부를 반환한다.
     *
     * @return 활성화 여부
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * 공통 메트릭 자동 구성 활성화 여부를 변경한다.
     *
     * @param enabled 활성화 여부
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * 메트릭에 기록할 배포 환경을 반환한다.
     *
     * @return 배포 환경
     */
    public String getEnvironment() {
        return environment;
    }

    /**
     * 메트릭에 기록할 배포 환경을 변경한다.
     *
     * @param environment local, dev, staging, production 같은 배포 환경
     */
    public void setEnvironment(String environment) {
        this.environment = environment == null || environment.isBlank()
                ? "local"
                : environment.strip();
    }

    /**
     * HTTP URI 태그에 허용할 최대 고유 값 수를 반환한다.
     *
     * @return URI 태그 제한
     */
    public int getMaxUriTags() {
        return maxUriTags;
    }

    /**
     * HTTP URI 태그의 최대 고유 값 수를 변경한다.
     *
     * @param maxUriTags URI 태그 제한
     */
    public void setMaxUriTags(int maxUriTags) {
        if (maxUriTags < 1) {
            throw new IllegalArgumentException("max-uri-tags는 1 이상이어야 합니다.");
        }
        this.maxUriTags = maxUriTags;
    }

    /**
     * 업무 작업 태그에 허용할 최대 고유 값 수를 반환한다.
     *
     * @return 업무 작업 태그 제한
     */
    public int getMaxOperationTags() {
        return maxOperationTags;
    }

    /**
     * 업무 작업 태그의 최대 고유 값 수를 변경한다.
     *
     * @param maxOperationTags 업무 작업 태그 제한
     */
    public void setMaxOperationTags(int maxOperationTags) {
        if (maxOperationTags < 1) {
            throw new IllegalArgumentException("max-operation-tags는 1 이상이어야 합니다.");
        }
        this.maxOperationTags = maxOperationTags;
    }
}
