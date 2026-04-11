package com.example.samplerecommend.web;

import com.example.samplerecommend.dto.CustomerActionRequest;
import com.example.samplerecommend.dto.CustomerActionResponse;
import com.example.samplerecommend.service.CustomerActionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 고객 행동 적재 API를 제공한다.
 */
@Validated
@RestController
@RequestMapping("/api/customers")
public class CustomerActionController {

    private final CustomerActionService customerActionService;

    public CustomerActionController(CustomerActionService customerActionService) {
        this.customerActionService = customerActionService;
    }

    /**
     * 특정 고객의 행동 이력을 저장한다.
     */
    @PostMapping("/{customerId}/actions")
    public CustomerActionResponse recordAction(@PathVariable @NotBlank String customerId,
                                               @Valid @RequestBody CustomerActionRequest request) {
        return customerActionService.record(customerId, request);
    }
}

