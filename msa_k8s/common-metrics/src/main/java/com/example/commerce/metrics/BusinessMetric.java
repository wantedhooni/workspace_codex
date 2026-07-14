package com.example.commerce.metrics;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 서비스 메서드의 처리시간과 성공·실패를 공통 업무 메트릭으로 기록한다.
 */
@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface BusinessMetric {

    /**
     * 낮은 카디널리티로 관리할 고정 업무 작업 이름을 지정한다.
     *
     * @return 업무 작업 이름
     */
    String value();
}
