package com.example.securities.channel;

import com.example.securities.channel.ChannelDtos.ApplicationResponse;
import com.example.securities.channel.ChannelDtos.CreateApplicationRequest;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/channel/applications")
public class ChannelController {

    private final ChannelService channelService;

    public ChannelController(ChannelService channelService) {
        this.channelService = channelService;
    }

    @PreAuthorize("hasRole('OPERATOR')")
    @PostMapping
    public ApplicationResponse create(
            @RequestBody CreateApplicationRequest request,
            @RequestHeader(value = "Idempotency-Key", required = false) String headerIdempotencyKey
    ) {
        String key = headerIdempotencyKey != null ? headerIdempotencyKey
                : request.idempotencyKey() != null ? request.idempotencyKey() : UUID.randomUUID().toString();

        return channelService.createApplication(new CreateApplicationRequest(
                request.customerId(),
                request.productCode(),
                key
        ));
    }
}
