package com.example.samplesecurebff.config;

import com.example.samplesecurebff.downstream.CustomerDirectoryClient;
import com.example.samplesecurebff.downstream.PositionServiceClient;
import com.example.samplesecurebff.downstream.RiskServiceClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Configuration
@EnableConfigurationProperties(DownstreamProperties.class)
public class DownstreamClientConfig {

    @Bean
    public CustomerDirectoryClient customerDirectoryClient(DownstreamProperties properties) {
        return createClient(properties, CustomerDirectoryClient.class);
    }

    @Bean
    public PositionServiceClient positionServiceClient(DownstreamProperties properties) {
        return createClient(properties, PositionServiceClient.class);
    }

    @Bean
    public RiskServiceClient riskServiceClient(DownstreamProperties properties) {
        return createClient(properties, RiskServiceClient.class);
    }

    private <T> T createClient(DownstreamProperties properties, Class<T> clientType) {
        RestClient restClient = RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .build();

        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(RestClientAdapter.create(restClient)).build();
        return factory.createClient(clientType);
    }
}
