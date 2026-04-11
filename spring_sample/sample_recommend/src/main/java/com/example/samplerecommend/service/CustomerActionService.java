package com.example.samplerecommend.service;

import com.example.samplerecommend.domain.CustomerAction;
import com.example.samplerecommend.domain.Product;
import com.example.samplerecommend.dto.CustomerActionRequest;
import com.example.samplerecommend.dto.CustomerActionResponse;
import com.example.samplerecommend.repository.CustomerActionRepository;
import com.example.samplerecommend.repository.CustomerProfileRepository;
import com.example.samplerecommend.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 고객 행동 이력 적재를 담당한다.
 */
@Service
@Transactional
public class CustomerActionService {

    private final CustomerActionRepository customerActionRepository;
    private final CustomerProfileRepository customerProfileRepository;
    private final ProductRepository productRepository;

    public CustomerActionService(CustomerActionRepository customerActionRepository,
                                 CustomerProfileRepository customerProfileRepository,
                                 ProductRepository productRepository) {
        this.customerActionRepository = customerActionRepository;
        this.customerProfileRepository = customerProfileRepository;
        this.productRepository = productRepository;
    }

    /**
     * 고객 행동 이력을 검증 후 저장한다.
     */
    public CustomerActionResponse record(String customerId, CustomerActionRequest request) {
        customerProfileRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new EntityNotFoundException("고객 프로필을 찾을 수 없습니다. customerId=" + customerId));
        Product product = productRepository.findByProductCode(request.productCode())
                .orElseThrow(() -> new EntityNotFoundException("상품을 찾을 수 없습니다. productCode=" + request.productCode()));

        CustomerAction action = customerActionRepository.save(new CustomerAction(
                customerId,
                product.getProductCode(),
                request.actionType().toUpperCase(),
                (double) request.weight(),
                LocalDateTime.now()
        ));

        return new CustomerActionResponse(
                action.getId(),
                action.getCustomerId(),
                action.getProductCode(),
                action.getActionType(),
                action.getWeight().intValue(),
                action.getActedAt()
        );
    }
}

