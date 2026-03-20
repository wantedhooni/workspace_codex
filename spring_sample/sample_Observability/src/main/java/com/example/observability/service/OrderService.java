package com.example.observability.service;

import com.example.observability.domain.CustomerOrder;
import com.example.observability.domain.OrderStatus;
import com.example.observability.domain.UserRole;
import com.example.observability.repository.CustomerOrderRepository;
import com.example.observability.security.AuthenticatedUser;
import com.example.observability.web.dto.CreateOrderRequest;
import com.example.observability.web.dto.DashboardResponse;
import com.example.observability.web.dto.OrderResponse;
import com.example.observability.web.dto.UpdateOrderStatusRequest;
import io.micrometer.observation.annotation.Observed;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 테넌트별 주문 운영 조회와 상태 변경을 처리한다.
 */
@Service
public class OrderService {

    private final CustomerOrderRepository customerOrderRepository;
    private final CurrentUserService currentUserService;
    private final ObservabilityMetricsService observabilityMetricsService;

    public OrderService(
            CustomerOrderRepository customerOrderRepository,
            CurrentUserService currentUserService,
            ObservabilityMetricsService observabilityMetricsService
    ) {
        this.customerOrderRepository = customerOrderRepository;
        this.currentUserService = currentUserService;
        this.observabilityMetricsService = observabilityMetricsService;
    }

    /**
     * 현재 로그인한 테넌트의 주문 목록을 최신순으로 조회한다.
     */
    @Transactional(readOnly = true)
    @Observed(name = "sample.orders.list", contextualName = "order-list")
    public List<OrderResponse> getOrders() {
        AuthenticatedUser currentUser = currentUserService.getCurrentUser();
        return customerOrderRepository.findByTenantIdOrderByCreatedAtDesc(currentUser.tenantId()).stream()
                .map(OrderResponse::from)
                .toList();
    }

    /**
     * 현재 로그인한 테넌트의 운영 지표 요약을 계산한다.
     */
    @Transactional(readOnly = true)
    @Observed(name = "sample.orders.dashboard", contextualName = "order-dashboard")
    public DashboardResponse getDashboard() {
        AuthenticatedUser currentUser = currentUserService.getCurrentUser();
        String tenantId = currentUser.tenantId();

        long pendingCount = customerOrderRepository.countByTenantIdAndStatus(tenantId, OrderStatus.PENDING);
        long inProgressCount = customerOrderRepository.countByTenantIdAndStatus(tenantId, OrderStatus.IN_PROGRESS);
        long completedCount = customerOrderRepository.countByTenantIdAndStatus(tenantId, OrderStatus.COMPLETED);
        long failedCount = customerOrderRepository.countByTenantIdAndStatus(tenantId, OrderStatus.FAILED);
        BigDecimal totalAmount = customerOrderRepository.sumAmountByTenantId(tenantId);

        return new DashboardResponse(
                tenantId,
                pendingCount,
                inProgressCount,
                completedCount,
                failedCount,
                totalAmount
        );
    }

    /**
     * 관리자 권한 사용자가 신규 주문을 생성하고 생성 메트릭을 기록한다.
     */
    @Transactional
    @Observed(name = "sample.orders.create", contextualName = "order-create")
    public OrderResponse createOrder(CreateOrderRequest request) {
        AuthenticatedUser currentUser = currentUserService.getCurrentUser();
        requireAdmin(currentUser);

        OffsetDateTime now = OffsetDateTime.now();
        CustomerOrder order = new CustomerOrder(
                currentUser.tenantId(),
                request.orderNumber(),
                request.title(),
                request.customerName(),
                OrderStatus.PENDING,
                request.priority(),
                request.amount(),
                now,
                request.dueAt()
        );
        CustomerOrder savedOrder = customerOrderRepository.save(order);

        Duration expectedLeadTime = request.dueAt() == null
                ? Duration.ZERO
                : Duration.between(now, request.dueAt()).abs();
        observabilityMetricsService.recordOrderCreated(savedOrder, expectedLeadTime);
        return OrderResponse.from(savedOrder);
    }

    /**
     * 관리자 권한 사용자가 주문 상태를 변경하고 상태 전이 메트릭을 기록한다.
     */
    @Transactional
    @Observed(name = "sample.orders.update-status", contextualName = "order-update-status")
    public OrderResponse updateStatus(Long orderId, UpdateOrderStatusRequest request) {
        AuthenticatedUser currentUser = currentUserService.getCurrentUser();
        requireAdmin(currentUser);

        CustomerOrder order = customerOrderRepository.findByIdAndTenantId(orderId, currentUser.tenantId())
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다."));

        OrderStatus previousStatus = order.getStatus();
        order.updateStatus(request.status());
        CustomerOrder updatedOrder = customerOrderRepository.save(order);
        observabilityMetricsService.recordOrderStatusChanged(updatedOrder, previousStatus);
        return OrderResponse.from(updatedOrder);
    }

    private void requireAdmin(AuthenticatedUser currentUser) {
        if (!UserRole.ADMIN.name().equals(currentUser.role())) {
            throw new IllegalArgumentException("관리자 권한이 필요합니다.");
        }
    }
}
