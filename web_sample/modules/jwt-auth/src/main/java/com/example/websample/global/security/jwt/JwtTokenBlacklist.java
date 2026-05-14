package com.example.websample.global.security.jwt;

/** 서버별 저장소에 등록된 무효화된 액세스 토큰 여부를 조회하는 계약입니다. */
public interface JwtTokenBlacklist {

    boolean contains(String tokenId);
}
