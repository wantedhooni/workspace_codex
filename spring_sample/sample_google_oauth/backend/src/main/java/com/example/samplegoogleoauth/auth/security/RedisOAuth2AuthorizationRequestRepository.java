package com.example.samplegoogleoauth.auth.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.SerializationUtils;

/**
 * OAuth authorization request를 Redis에 저장해 서버 세션 없이 콜백을 처리한다.
 */
@Component
public class RedisOAuth2AuthorizationRequestRepository implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    private static final String KEY_PREFIX = "sample_google_oauth:oauth2:request:";
    private static final Duration TTL = Duration.ofMinutes(5);

    private final RedissonClient redissonClient;

    public RedisOAuth2AuthorizationRequestRepository(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        String state = request.getParameter("state");
        if (state == null || state.isBlank()) {
            return null;
        }

        byte[] payload = redissonClient.<byte[]>getBucket(KEY_PREFIX + state).get();
        if (payload == null) {
            return null;
        }

        Object restored = SerializationUtils.deserialize(payload);
        return restored instanceof OAuth2AuthorizationRequest authorizationRequest ? authorizationRequest : null;
    }

    @Override
    public void saveAuthorizationRequest(
        OAuth2AuthorizationRequest authorizationRequest,
        HttpServletRequest request,
        HttpServletResponse response
    ) {
        if (authorizationRequest == null) {
            removeByState(request.getParameter("state"));
            return;
        }

        byte[] payload = SerializationUtils.serialize(authorizationRequest);
        RBucket<byte[]> bucket = redissonClient.getBucket(KEY_PREFIX + authorizationRequest.getState());
        bucket.set(payload, TTL);
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(
        HttpServletRequest request,
        HttpServletResponse response
    ) {
        String state = request.getParameter("state");
        OAuth2AuthorizationRequest authorizationRequest = loadAuthorizationRequest(request);
        removeByState(state);
        return authorizationRequest;
    }

    private void removeByState(String state) {
        if (state == null || state.isBlank()) {
            return;
        }
        redissonClient.<byte[]>getBucket(KEY_PREFIX + state).delete();
    }
}
