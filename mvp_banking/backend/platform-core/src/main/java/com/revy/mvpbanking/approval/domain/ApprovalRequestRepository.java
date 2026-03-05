package com.revy.mvpbanking.approval.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApprovalRequestRepository extends JpaRepository<ApprovalRequest, UUID> {
    List<ApprovalRequest> findAllByOrderByCreatedAtDesc();
    Optional<ApprovalRequest> findByTitle(String title);
    Optional<ApprovalRequest> findByTargetTypeAndTargetIdAndStatus(ApprovalTargetType targetType, UUID targetId, ApprovalStatus status);
}
