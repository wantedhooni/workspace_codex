package com.example.marketsignal.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.marketsignal.macro.MarketRegime;
import com.example.marketsignal.report.DailyReportResponse;
import com.example.marketsignal.report.SignalSummaryResponse;
import com.example.marketsignal.signal.SignalAction;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;

class RedisConfigTest {

    @Test
    void dailyReportResponseCanBeSerializedWithJavaTimeFields() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.activateDefaultTyping(
                BasicPolymorphicTypeValidator.builder()
                        .allowIfSubType(Object.class)
                        .build(),
                ObjectMapper.DefaultTyping.EVERYTHING,
                JsonTypeInfo.As.PROPERTY
        );

        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(objectMapper);
        DailyReportResponse response = new DailyReportResponse(
                LocalDate.of(2026, 3, 16),
                LocalDate.of(2026, 3, 13),
                java.time.LocalDateTime.of(2026, 3, 16, 8, 45),
                MarketRegime.GROWTH,
                List.of("SOFTWARE", "SEMICONDUCTOR"),
                List.of(new SignalSummaryResponse("NVDA", 7, SignalAction.BUY, List.of("20DMA 상회", "상대 강도 우위"))),
                "성장주 선호 흐름이 유지됩니다."
        );

        byte[] serialized = serializer.serialize(response);
        Object restored = serializer.deserialize(serialized);

        assertThat(serialized).isNotNull();
        assertThat(restored).isEqualTo(response);
    }
}
