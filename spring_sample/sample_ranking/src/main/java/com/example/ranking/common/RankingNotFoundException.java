package com.example.ranking.common;

/**
 * 시즌 또는 플레이어 랭킹 정보를 찾을 수 없을 때 발생한다.
 */
public class RankingNotFoundException extends RuntimeException {

    public RankingNotFoundException(String message) {
        super(message);
    }
}
