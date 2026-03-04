package com.example.samplespringadmin.ops;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdminChecklistController {

    @GetMapping("/api/admin/checklists")
    public List<String> checklists() {
        return List.of(
                "health 상태 확인",
                "thread dump 급증 여부 확인",
                "http.server.requests latency 점검",
                "disk / memory 사용량 확인"
        );
    }
}
