package com.commerce.service_metadata;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/metadata")
public class MetadataController {

    @GetMapping("/status")
    public String status() {
        return "service-metadata up";
    }
}
