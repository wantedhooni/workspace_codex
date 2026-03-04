package com.example.samplewebsocketrealtime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.example.samplewebsocketrealtime.realtime.PriceUpdateRequest;
import com.example.samplewebsocketrealtime.realtime.RealtimePublisherService;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.simp.SimpMessagingTemplate;

class RealtimePublisherServiceTests {

    @Test
    void publishesPriceTopic() {
        SimpMessagingTemplate template = mock(SimpMessagingTemplate.class);
        RealtimePublisherService service = new RealtimePublisherService(template);

        var response = service.publishPrice(new PriceUpdateRequest("AAPL", new BigDecimal("198.25")));

        assertThat(response.instrument()).isEqualTo("AAPL");
        verify(template).convertAndSend(eq("/topic/prices"), eq(response));
    }
}
