package com.example.securities.information;

import com.example.securities.information.InformationDtos.InformationEventResponse;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InformationService {

    private final InformationEventRepository informationEventRepository;

    public InformationService(InformationEventRepository informationEventRepository) {
        this.informationEventRepository = informationEventRepository;
    }

    @Transactional
    public void storeConfirmedEvent(String eventType, String aggregateId, String payload) {
        informationEventRepository.save(new InformationEvent(eventType, aggregateId, payload));
    }

    @Transactional(readOnly = true)
    public List<InformationEventResponse> getEvents() {
        return informationEventRepository.findAll().stream()
                .map(event -> new InformationEventResponse(
                        event.getId(),
                        event.getEventType(),
                        event.getAggregateId(),
                        event.getPayload(),
                        event.getConfirmedAt()
                ))
                .toList();
    }
}
