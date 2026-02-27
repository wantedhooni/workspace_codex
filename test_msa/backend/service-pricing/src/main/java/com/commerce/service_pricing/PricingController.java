package com.commerce.service_pricing;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/pricing")
public class PricingController {

    @GetMapping("/status")
    public String status() {
        return "service-pricing up";
    }
}
