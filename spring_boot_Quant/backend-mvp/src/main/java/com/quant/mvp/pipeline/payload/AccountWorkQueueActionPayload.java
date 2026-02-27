package com.quant.mvp.pipeline.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;

public final class AccountWorkQueueActionPayload {

    private AccountWorkQueueActionPayload() {
    }

    public static final class RemediateStaleOrders {
        private RemediateStaleOrders() {
        }

        public record Req(
                @NotNull Long portfolioId,
                Integer staleMinutes,
                @NotBlank String reason
        ) {
        }

        public record Res(
                Long portfolioId,
                Integer staleThresholdMinutes,
                Integer canceledCount,
                Instant executedAt
        ) {
        }
    }

    public static final class RevokeOtherSessions {
        private RevokeOtherSessions() {
        }

        public record Req(
                Long portfolioId,
                String reason
        ) {
        }

        public record Res(
                Integer revokedCount,
                List<Long> revokedSessionIds,
                Instant executedAt
        ) {
        }
    }

    public static final class PostApprovedVouchers {
        private PostApprovedVouchers() {
        }

        public record Req(
                @NotNull Long portfolioId,
                Integer limit,
                String reason
        ) {
        }

        public record Res(
                Long portfolioId,
                Integer attemptedCount,
                Integer postedCount,
                List<Long> postedVoucherIds,
                List<String> failedReasons,
                Instant executedAt
        ) {
        }
    }

    public static final class ApproveDraftVouchers {
        private ApproveDraftVouchers() {
        }

        public record Req(
                @NotNull Long portfolioId,
                Integer limit,
                String reason
        ) {
        }

        public record Res(
                Long portfolioId,
                Integer attemptedCount,
                Integer approvedCount,
                List<Long> approvedVoucherIds,
                List<String> failedReasons,
                Instant executedAt
        ) {
        }
    }

    public static final class PauseTrading {
        private PauseTrading() {
        }

        public record Req(
                @NotNull Long portfolioId,
                @NotBlank String reason
        ) {
        }

        public record Res(
                Long portfolioId,
                Boolean tradingEnabled,
                String killSwitchReason,
                Instant killSwitchUpdatedAt,
                String killSwitchUpdatedBy
        ) {
        }
    }

    public static final class EmergencyRiskResponse {
        private EmergencyRiskResponse() {
        }

        public record Req(
                @NotNull Long portfolioId,
                @NotBlank String reason,
                Boolean cancelOpenOrders
        ) {
        }

        public record Res(
                Long portfolioId,
                Boolean tradingEnabled,
                String killSwitchReason,
                Integer canceledCount,
                List<Long> canceledOrderIds,
                Instant executedAt,
                String executedBy
        ) {
        }
    }

    public static final class ResumeTrading {
        private ResumeTrading() {
        }

        public record Req(
                @NotNull Long portfolioId,
                @NotBlank String reason,
                Boolean force
        ) {
        }

        public record Res(
                Long portfolioId,
                Boolean tradingEnabled,
                Boolean resumed,
                Integer blockedCriticalCount,
                List<String> blockedCodes,
                List<String> blockedMessages,
                Instant executedAt,
                String executedBy
        ) {
        }
    }
}
