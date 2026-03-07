package com.example.securities.information;

import com.example.securities.information.InformationDtos.InformationEventResponse;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/information/events")
public class InformationController {

    private final InformationService informationService;

    public InformationController(InformationService informationService) {
        this.informationService = informationService;
    }

    @PreAuthorize("hasAnyRole('VIEWER','OPERATOR')")
    @GetMapping
    public List<InformationEventResponse> getEvents() {
        return informationService.getEvents();
    }
}
