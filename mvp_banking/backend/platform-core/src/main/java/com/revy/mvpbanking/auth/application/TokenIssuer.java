package com.revy.mvpbanking.auth.application;

import com.revy.mvpbanking.auth.domain.AuthenticatedPrincipal;
import com.revy.mvpbanking.auth.domain.RefreshTokenRecord;
import com.revy.mvpbanking.auth.domain.TokenPair;
import com.revy.mvpbanking.auth.infrastructure.JwtTokenProvider;
import com.revy.mvpbanking.auth.infrastructure.RefreshTokenStore;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class TokenIssuer {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenStore refreshTokenStore;

    public TokenIssuer(JwtTokenProvider jwtTokenProvider, RefreshTokenStore refreshTokenStore) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.refreshTokenStore = refreshTokenStore;
    }

    public TokenPair issue(AuthenticatedPrincipal principal) {
        String accessToken = jwtTokenProvider.generateAccessToken(principal);
        String refreshToken = UUID.randomUUID().toString();
        String tokenId = UUID.randomUUID().toString();

        refreshTokenStore.save(
                refreshToken,
                new RefreshTokenRecord(
                        tokenId,
                        principal.id(),
                        principal.email(),
                        principal.displayName(),
                        principal.principalType(),
                        principal.roles(),
                        Instant.now().plusSeconds(jwtTokenProvider.getRefreshTokenExpirationSeconds())
                ),
                jwtTokenProvider.getRefreshTokenExpirationSeconds()
        );

        return new TokenPair(
                accessToken,
                refreshToken,
                jwtTokenProvider.getAccessTokenExpirationSeconds(),
                jwtTokenProvider.getRefreshTokenExpirationSeconds()
        );
    }

    public void revoke(String refreshToken) {
        refreshTokenStore.delete(refreshToken);
    }
}
