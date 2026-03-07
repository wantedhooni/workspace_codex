package com.example.securities.external;

import com.example.securities.common.BusinessException;
import com.example.securities.external.ExternalDtos.ExternalMessageResponse;
import com.example.securities.external.ExternalDtos.SendMessageRequest;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExternalService {

    private final ExternalMessageRepository externalMessageRepository;

    public ExternalService(ExternalMessageRepository externalMessageRepository) {
        this.externalMessageRepository = externalMessageRepository;
    }

    @Transactional
    public ExternalMessageResponse send(SendMessageRequest request) {
        ExternalMessage message = externalMessageRepository.save(new ExternalMessage(request.messageType(), request.payload()));
        simulateSend(message);
        return toResponse(message);
    }

    @Transactional
    public ExternalMessageResponse retry(String messageId) {
        ExternalMessage message = externalMessageRepository.findById(messageId)
                .orElseThrow(() -> new BusinessException("MESSAGE_NOT_FOUND", "전문을 찾을 수 없습니다."));
        simulateSend(message);
        return toResponse(message);
    }

    @Transactional(readOnly = true)
    public List<ExternalMessageResponse> list() {
        return externalMessageRepository.findAll().stream().map(this::toResponse).toList();
    }

    private void simulateSend(ExternalMessage message) {
        if (message.getPayload().contains("FAIL")) {
            message.markFailed("원격기관 통신 실패(시뮬레이션)");
        } else {
            message.markSent();
        }
    }

    private ExternalMessageResponse toResponse(ExternalMessage message) {
        return new ExternalMessageResponse(
                message.getId(),
                message.getMessageType(),
                message.getPayload(),
                message.getStatus(),
                message.getRetryCount(),
                message.getLastError(),
                message.getCreatedAt()
        );
    }
}
