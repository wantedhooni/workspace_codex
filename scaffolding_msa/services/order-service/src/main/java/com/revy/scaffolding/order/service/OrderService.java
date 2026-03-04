package com.revy.scaffolding.order.service;

import feign.FeignException;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.revy.scaffolding.order.client.UserServiceClient;
import com.revy.scaffolding.order.domain.OrderStatus;
import com.revy.scaffolding.order.domain.PurchaseOrder;
import com.revy.scaffolding.order.domain.repository.PurchaseOrderRepository;
import com.revy.scaffolding.order.dto.OrderCreateRequest;
import com.revy.scaffolding.order.dto.OrderDetailResponse;
import com.revy.scaffolding.order.dto.OrderSummaryResponse;
import com.revy.scaffolding.order.dto.UserLookupResponse;
import com.revy.scaffolding.order.exception.OrderNotFoundException;
import com.revy.scaffolding.order.exception.OrderUserNotFoundException;
import com.revy.scaffolding.order.exception.UpstreamUserServiceException;

@Service
@Transactional(readOnly = true)
public class OrderService {
    private final PurchaseOrderRepository orderRepository;
    private final UserServiceClient userServiceClient;

    public OrderService(PurchaseOrderRepository orderRepository, UserServiceClient userServiceClient) {
        this.orderRepository = orderRepository;
        this.userServiceClient = userServiceClient;
    }

    @Transactional
    public OrderDetailResponse create(OrderCreateRequest request) {
        UserLookupResponse user = getUser(request.userId());
        PurchaseOrder order = orderRepository.save(
            new PurchaseOrder(
                request.userId(),
                "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                request.amount(),
                OrderStatus.CREATED
            )
        );
        return OrderDetailResponse.of(order, user);
    }

    public OrderDetailResponse get(Long orderId) {
        PurchaseOrder order = orderRepository.findById(orderId).orElseThrow(() -> new OrderNotFoundException(orderId));
        return OrderDetailResponse.of(order, getUser(order.getUserId()));
    }

    public List<OrderSummaryResponse> list(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    private UserLookupResponse getUser(Long userId) {
        try {
            return userServiceClient.getUser(userId);
        } catch (FeignException.NotFound exception) {
            throw new OrderUserNotFoundException(userId);
        } catch (FeignException exception) {
            throw new UpstreamUserServiceException();
        }
    }
}

