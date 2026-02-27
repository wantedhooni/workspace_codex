package com.curd.template.core.error;

import java.util.List;
import org.springframework.http.ProblemDetail;

public final class ProblemDetailsFactory {

    private ProblemDetailsFactory() {
    }

    public static ProblemDetail from(ApiException exception, String traceId) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(exception.status(), exception.getMessage());
        detail.setProperty("orgCode", exception.orgCode());
        detail.setProperty("appCode", exception.appCode());
        detail.setProperty("traceId", traceId == null ? "n/a" : traceId);
        detail.setProperty("errors", exception.errors());
        return detail;
    }

    public static ProblemDetail fromUnexpected(Throwable throwable, String traceId) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(AppErrorCode.INTERNAL_ERROR.status(), "Unexpected server error");
        detail.setProperty("orgCode", AppErrorCode.INTERNAL_ERROR.orgCode());
        detail.setProperty("appCode", AppErrorCode.INTERNAL_ERROR.appCode());
        detail.setProperty("traceId", traceId == null ? "n/a" : traceId);
        detail.setProperty("errors", List.of());
        return detail;
    }
}
