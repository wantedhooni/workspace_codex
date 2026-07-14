package com.revy.application.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/app-1/test")
class TestController {

    @GetMapping
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("app_1 Hello World");
    }
}
