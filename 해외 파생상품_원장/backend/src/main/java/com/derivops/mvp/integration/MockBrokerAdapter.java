package com.derivops.mvp.integration;

import com.derivops.mvp.common.BadRequestException;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class MockBrokerAdapter implements BrokerAdapter {

    @Override
    public void submitCashInstruction(UUID requestId) {
        maybeFail(requestId, "cash");
    }

    @Override
    public void submitFxInstruction(UUID requestId) {
        maybeFail(requestId, "fx");
    }

    private void maybeFail(UUID requestId, String type) {
        // Deterministic transient failure simulation for retry scenarios.
        if (Math.abs(requestId.hashCode()) % 11 == 0) {
            throw new BadRequestException("Mock broker " + type + " submission failed");
        }
    }
}
