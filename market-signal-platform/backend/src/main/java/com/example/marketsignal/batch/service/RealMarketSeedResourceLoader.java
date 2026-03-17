package com.example.marketsignal.batch;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

/**
 * 클래스패스에 저장된 실제 시장 시드 데이터를 읽어온다.
 */
@Component
@RequiredArgsConstructor
public class RealMarketSeedResourceLoader {

    private static final String SEED_RESOURCE_PATH = "seed/real-market-seed.json";

    private final ObjectMapper objectMapper;

    /**
     * JSON 리소스에서 실제 시장 시드 데이터를 역직렬화한다.
     */
    public RealMarketSeedData load() {
        try {
            return objectMapper.readValue(new ClassPathResource(SEED_RESOURCE_PATH).getInputStream(), RealMarketSeedData.class);
        } catch (IOException exception) {
            throw new IllegalStateException("실제 시장 시드 데이터를 불러오지 못했습니다.", exception);
        }
    }
}
