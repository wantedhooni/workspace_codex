package com.example.r2dbcauditlog.auditlog;

import static org.mockito.BDDMockito.given;

import com.example.r2dbcauditlog.support.ApiExceptionHandler;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Import(ApiExceptionHandler.class)
@WebFluxTest(controllers = ApprovalWorkflowController.class)
class ApprovalWorkflowControllerTests {

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private ApprovalWorkflowService approvalWorkflowService;

    @Test
    void createsApprovalRequest() {
        ApprovalRequestResponse response = new ApprovalRequestResponse(
                "APR-2026-001",
                "platform-team",
                "billing-api",
                ApprovalStatus.REQUESTED,
                "월말 정산 배치 권한 오픈",
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        given(approvalWorkflowService.createApprovalRequest(new CreateApprovalRequest(
                "APR-2026-001",
                "platform-team",
                "billing-api",
                "월말 정산 배치 권한 오픈"
        ))).willReturn(Mono.just(response));

        webTestClient.post()
                .uri("/api/approval-requests")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                          "requestNumber": "APR-2026-001",
                          "requester": "platform-team",
                          "targetSystem": "billing-api",
                          "reason": "월말 정산 배치 권한 오픈"
                        }
                        """)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.status").isEqualTo("REQUESTED");
    }

    @Test
    void returnsAuditLogs() {
        AuditLogResponse response = new AuditLogResponse(
                "APPROVAL_REQUEST",
                "APR-2026-001",
                "REQUEST_APPROVED",
                "ops-manager",
                "배포 윈도우 확인 후 승인",
                LocalDateTime.now()
        );

        given(approvalWorkflowService.getAuditLogs("APR-2026-001")).willReturn(Flux.just(response));

        webTestClient.get()
                .uri("/api/audit-logs/APR-2026-001")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].action").isEqualTo("REQUEST_APPROVED");
    }
}
