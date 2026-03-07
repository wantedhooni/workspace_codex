package com.example.securities.channel;

import com.example.securities.account.AccountService;
import com.example.securities.channel.ChannelDtos.ApplicationResponse;
import com.example.securities.channel.ChannelDtos.CreateApplicationRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChannelService {

    private final ChannelApplicationRepository applicationRepository;
    private final AccountService accountService;

    public ChannelService(ChannelApplicationRepository applicationRepository, AccountService accountService) {
        this.applicationRepository = applicationRepository;
        this.accountService = accountService;
    }

    @Transactional
    public ApplicationResponse createApplication(CreateApplicationRequest request) {
        return applicationRepository.findByIdempotencyKey(request.idempotencyKey())
                .map(existing -> new ApplicationResponse(
                        existing.getId(),
                        existing.getCustomerId(),
                        existing.getProductCode(),
                        existing.getStatus(),
                        null,
                        existing.getCreatedAt()))
                .orElseGet(() -> {
                    ChannelApplication application = new ChannelApplication(
                            request.customerId(),
                            request.productCode(),
                            request.idempotencyKey()
                    );
                    application.approve();
                    ChannelApplication saved = applicationRepository.save(application);
                    String accountId = accountService.openAccount(saved.getCustomerId(), saved.getId());
                    return new ApplicationResponse(
                            saved.getId(),
                            saved.getCustomerId(),
                            saved.getProductCode(),
                            saved.getStatus(),
                            accountId,
                            saved.getCreatedAt()
                    );
                });
    }
}
