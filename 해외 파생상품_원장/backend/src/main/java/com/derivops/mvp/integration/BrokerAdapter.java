package com.derivops.mvp.integration;

import java.util.UUID;

public interface BrokerAdapter {
    void submitCashInstruction(UUID requestId);

    void submitFxInstruction(UUID requestId);
}
