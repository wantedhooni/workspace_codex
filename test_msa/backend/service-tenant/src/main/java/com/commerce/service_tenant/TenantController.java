package com.commerce.service_tenant;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tenant")
public class TenantController {

    @GetMapping("/status")
    public String status() {
        return "service-tenant up";
    }
}
